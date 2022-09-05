package network.packet

import network.opcode.SendOpcode
import tools.Pair
import tools.data.output.MaplePacketLittleEndianWriter

class UserLocal {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        /**
         * onSitResult
         * @param id either the chair itemid or -1 (to remove chair)
         */
        fun onSitResult(id: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SIT_RESULT.value)
            if (id < 0) {
                mplew.write(0)
            } else {
                mplew.write(1)
                mplew.writeShort(id)
            }
            return mplew.packet
        }

        // TODO handle onEffect

        fun onTeleport(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TELEPORT.value)
            mplew.write(0)
            mplew.write(6)

            return mplew.packet
        }

        /**
         * onBalloonMsg
         *
         * @param hint The hint it's going to send.
         * @param width How tall the box is going to be.
         * @param height How long the box is going to be.
         */
        fun onBalloonMessage(hint: String, width: Int, height: Int): ByteArray? {
            var msgWidth = width
            var msgHeight = height

            if (width < 1) {
                msgWidth = hint.length * 10
                if (width < 40) { msgWidth = 40 }
            }

            if (height < 5) { msgHeight = 5 }

            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.BALLOON_MSG.value)
            mplew.writeMapleAsciiString(hint)
            mplew.writeShort(msgWidth)
            mplew.writeShort(msgHeight)
            mplew.write(1)

            return mplew.packet
        }

        // MAKER_RESULT packets thanks to Arnah (Vertisy)
        fun makerResult(success: Boolean, itemMade: Int, itemCount: Int, mesos: Int, itemsLost: List<Pair<Int?, Int?>>, catalystID: Int, INCBuffGems: List<Int?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAKER_RESULT.value)
            mplew.writeInt(if (success) 0 else 1) // 0 = success, 1 = fail
            mplew.writeInt(1) // 1 or 2 doesn't matter, same methods
            mplew.writeBool(!success)
            if (success) {
                mplew.writeInt(itemMade)
                mplew.writeInt(itemCount)
            }
            mplew.writeInt(itemsLost.size) // Loop
            for (item in itemsLost) {
                mplew.writeInt(item.getLeft()!!)
                mplew.writeInt(item.getRight()!!)
            }
            mplew.writeInt(INCBuffGems.size)
            for (gem in INCBuffGems) {
                mplew.writeInt(gem!!)
            }
            if (catalystID != -1) {
                mplew.write(1) // stimulator
                mplew.writeInt(catalystID)
            } else {
                mplew.write(0)
            }

            mplew.writeInt(mesos)
            return mplew.packet
        }

        fun makerResultCrystal(itemIdGained: Int, itemIdLost: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAKER_RESULT.value)
            mplew.writeInt(0) // Always successful!
            mplew.writeInt(3) // Monster Crystal
            mplew.writeInt(itemIdGained)
            mplew.writeInt(itemIdLost)

            return mplew.packet
        }

        fun makerResultDesynth(itemId: Int, mesos: Int, itemsGained: List<Pair<Int?, Int?>>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAKER_RESULT.value)
            mplew.writeInt(0) // Always successful!
            mplew.writeInt(4) // Mode Desynth
            mplew.writeInt(itemId) // Item desynthed
            mplew.writeInt(itemsGained.size) // Loop of items gained, (int, int)
            for (item in itemsGained) {
                mplew.writeInt(item.getLeft()!!)
                mplew.writeInt(item.getRight()!!)
            }

            mplew.writeInt(mesos) // Mesos spent.
            return mplew.packet
        }

        fun makerEnableActions(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.MAKER_RESULT.value)
            mplew.writeInt(0) // Always successful!
            mplew.writeInt(0) // Monster Crystal
            mplew.writeInt(0)
            mplew.writeInt(0)

            return mplew.packet
        }

        /**
         * Sends a UI utility.
         * 0x01 - Equipment Inventory.
         * 0x02 - Stat Window.
         * 0x03 - Skill Window.
         * 0x05 - Keyboard Settings.
         * 0x06 - Quest window.
         * 0x09 - Monsterbook Window.
         * 0x0A - Char Info
         * 0x0B - Guild BBS
         * 0x12 - Monster Carnival Window
         * 0x16 - Party Search.
         * 0x17 - Item Creation Window.
         * 0x1A - My Ranking O.O
         * 0x1B - Family Window
         * 0x1C - Family Pedigree
         * 0x1D - GM Story Board /funny shet
         * 0x1E - Envelop saying you got mail from an admin.
         * 0x1F - Medal Window
         * 0x20 - Maple Event (???)
         * 0x21 - Invalid Pointer Crash
         *
         * @param ui
         * @return
         */
        // TODO handle these ui bytes with an enum
        fun onOpenUI(ui: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.OPEN_UI.value)
            mplew.write(ui)

            return mplew.packet
        }

        /**
         * locks the UI
         * Example -> Aran tutorial
         */
        fun setDirectionMode(enable: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.SET_DIRECTION_MODE.value)
            mplew.write(if (enable) 1 else 0)

            return mplew.packet
        }

        fun onDisableUI(enable: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DISABLE_UI.value)
            mplew.write(if (enable) 1 else 0)

            return mplew.packet
        }

        fun hireTutor(spawn: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.HIRE_TUTOR.value)
            if (spawn) {
                mplew.write(1)
            } else {
                mplew.write(0)
            }

            return mplew.packet
        }

        fun tutorMessage(talk: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TUTOR_MSG.value)
            mplew.write(0)
            mplew.writeMapleAsciiString(talk)
            mplew.write(byteArrayOf(0xC8.toByte(), 0, 0, 0, 0xA0.toByte(), 0x0F.toByte(), 0, 0))

            return mplew.packet
        }

        fun tutorHint(hint: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(11)
            mplew.writeShort(SendOpcode.TUTOR_MSG.value)
            mplew.write(1)
            mplew.writeInt(hint)
            mplew.writeInt(7000)

            return mplew.packet
        }

        fun showComboResponse(count: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.COMBO_RESPONSE.value)
            mplew.writeInt(count)

            return mplew.packet
        }

        /**
         * @param sid skill id
         * @param time duration of cooldown
         */
        fun skillCooldown(sid: Int, time: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SKILL_COOLDOWN.value)
            mplew.writeInt(sid)
            mplew.writeShort(time) //Int in v97

            return mplew.packet
        }

        /**
         * unused enum values
         *
         * MESO_GIVE_SUCCEED
         * MESO_GIVE_FAIL
         * NOTIFY_HP_DEC_BY_FIELD
         * CUserLocal::OnPlayEventSound
         * CUserLocal::OnPlayMinigameSound
         * OnRandomEmotion(226)
         * OnResignQuestReturn(227)
         * OnPassMateName
         * OnRadioSchedule
         * open_skill_guide
         * chat_msg
         * onSayImage
         **/
    }
}