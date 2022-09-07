package network.packet

import enums.UserEffectType
import network.opcode.SendOpcode
import tools.Pair
import tools.data.output.MaplePacketLittleEndianWriter

/**
 * @Author Connor (Conchlin)
 */

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


        /**
         * onEffect
         *
         * @param effect reference the UserEffectType enum
         * @param pathway used for hard references to SHOW_INTRO and SHOW_INFO requests
         * @param args for any additional packet write handling
         */
        fun onEffect(effect: Byte, pathway: String, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.USER_LOCAL_EFFECT.value)
            mplew.write(effect)
            when (effect) {
                UserEffectType.LEVEL_UP.effect -> {}
                UserEffectType.PORTAL_SE.effect -> {}
                UserEffectType.MONSTERBOOK_PICKUP.effect -> {}
                UserEffectType.QUEST_COMPLETE.effect -> {}
                UserEffectType.EQUIP_LEVEL_UP.effect -> {}
                UserEffectType.JOB_CHANGE.effect -> {}
                UserEffectType.EXP_CARD.effect -> {}
                UserEffectType.QUEST_COMPLETE.effect -> {}
                UserEffectType.SPIRIT_STONE.effect -> {}
                UserEffectType.PET_LEVEL_UP.effect -> {
                    mplew.write(0)
                    mplew.write(args[0]) // Pet Index

                }
                UserEffectType.BUYBACK.effect -> {
                    mplew.writeInt(0)
                }
                UserEffectType.RECOVERY.effect -> {
                    mplew.write(args[0]) // heal amount
                }
                UserEffectType.MAKER.effect -> {
                    mplew.writeInt(if (args[0] == 0) 0 else 1) // 0 succeed 1 fail
                }
                UserEffectType.WHEEL_DESTINY.effect -> {
                    mplew.write(args[0]) // amount left
                }
                UserEffectType.SHOW_INTRO.effect -> {
                    mplew.writeMapleAsciiString(pathway) // filepath
                }
                UserEffectType.SHOW_INFO.effect -> {
                    mplew.writeMapleAsciiString(pathway) // filepath
                    mplew.writeInt(1)
                }
            }

            return mplew.packet
        }

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

        /**
        * MESO_GIVE_SUCCEED
        * MESO_GIVE_FAIL
        **/


        fun updateQuestInfo(quest: Short, npc: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.QUEST_RESULT.value)
            mplew.write(8) //0x0A in v95
            mplew.writeShort(quest.toInt())
            mplew.writeInt(npc)
            mplew.writeInt(0)

            return mplew.packet
        }

        fun addQuestTimeLimit(quest: Short, time: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.QUEST_RESULT.value)
            mplew.write(6)
            mplew.writeShort(1) //Size but meh, when will there be 2 at the same time? And it won't even replace the old one :)
            mplew.writeShort(quest.toInt())
            mplew.writeInt(time)

            return mplew.packet
        }

        fun removeQuestTimeLimit(quest: Short): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.QUEST_RESULT.value)
            mplew.write(7)
            mplew.writeShort(1) //Position
            mplew.writeShort(quest.toInt())

            return mplew.packet
        }

        // not sure if there is an actual visible effect associated with these so it remains unimplemented
        fun onNotifyHpDec(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NOTIFY_HP_DEC_BY_FIELD.value)
            mplew.writeInt(0) // decode4

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
         * list of UI windows can be found in UIType.kt
         */
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