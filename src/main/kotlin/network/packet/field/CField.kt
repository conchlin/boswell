package network.packet.field

import enums.FieldEffectType
import enums.WhisperResultType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class CField {

    companion object Packet {

        /**
         * returns error message packet for certain field transfer requests
         *
         * Possible values for <code>type</code>:
         * <br> 1: The portal is closed for now.
         * <br> 2: You cannot go to that place.
         * <br> 3: Unable to approach due to the force of the ground.
         * <br> 4: You cannot teleport to or on this map.
         * <br> 5: Unable to approach due to the force of the ground.
         * <br> 6: This map can only be entered by party members.
         * <br> 7: The Cash Shop is currently not available. Stay tuned...<br>
         */
        fun onTransferFieldRequestIgnored(type: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TransferFieldRequestIgnored.value)
            mplew.write(type)

            return mplew.packet
        }

        /**
         * returns error message packet for certain channel transfer requests
         *
         * Possible values for <code>type</code>:
         * <br> 1: You cannot move that channel. Please try again later.
         * <br> 2: You cannot go into the cash shop. Please try again later.
         * <br> 3: The Item-Trading Shop is currently unavailable. Please try again later.
         * <br> 4: You cannot go into the trade shop, due to limitation of user count.
         * <br> 5: You do not meet the minimum level requirement to access the Trade Shop.<br>
         */
        fun onTransferChannelReqIgnored(type: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TransferChannelReqIgnored.value)
            mplew.write(type)

            return mplew.packet
        }

        fun onFieldSpecificData(team: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FieldSpecificData.value)
            if (team > -1) {
                mplew.write(team) // 00 = red, 01 = blue
            }
            return mplew.packet
        }

        /**
         * sends packet that broadcasts the different message modes
         *
         * @param name
         * @param chatText
         * @param mode 0 buddy chat, 1 party chat, 2 guild chat
         */
        fun onGroupMessage(name: String?, chatText: String?, mode: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GroupMessage.value)
            mplew.write(mode)
            mplew.writeMapleAsciiString(name)
            mplew.writeMapleAsciiString(chatText)

            return mplew.packet
        }

        /**
         * Packet for /find and whispering actions
         *
         * @param target user you are whispering or finding
         *              (or the text of the message in some cases like WhisperSend)
         * @param sender user doing action
         * @param result @see WhisperResultType
         * @param
         */
        fun onWhisper(target: String, sender: String, result: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Whisper.value)
            mplew.write(result)
            when (result) {
                WhisperResultType.WhisperReply.result -> {
                    mplew.writeMapleAsciiString(target)
                    mplew.write(args[0]) // error code: 0x0 = cannot find char, 0x1 = success
                }
                WhisperResultType.WhisperSend.result -> {
                    mplew.writeMapleAsciiString(sender)
                    mplew.writeShort(args[0] - 1) // channel
                    mplew.writeMapleAsciiString(target) // the text of the message
                }
                WhisperResultType.FindReply.result,
                WhisperResultType.FindBuddy.result -> {
                    mplew.writeMapleAsciiString(target)
                    mplew.write(args[1]) // 0: mts 1: map 2: cs 3: not sure
                    mplew.writeInt(args[0]) // map id -> -1 if mts, cs
                    if (args[1] == 1) {
                        mplew.write(ByteArray(8))
                    }
                }
            }

            return mplew.packet
        }

        fun onCoupleMessage(fiance: String?, text: String?, spouse: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.CoupleMessage.value)
            mplew.write(if (spouse) 5 else 4) // v2 = CInPacket::Decode1(a1) - 4;
            if (spouse) { // if ( v2 ) {
                mplew.writeMapleAsciiString(fiance)
            }
            mplew.write(if (spouse) 5 else 1)
            mplew.writeMapleAsciiString(text)

            return mplew.packet
        }

        /**
         * onFieldEffect sends packet responsible for the various field related effects
         *
         * @param result see FieldEffectType.kt
         * @param path the pathway to the effect being broadcast
         */
        fun onFieldEffect(result: Int, path: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FieldEffect.value)
            mplew.write(result)
            when (result) {
                FieldEffectType.Tremble.mode,
                FieldEffectType.Effect.mode,
                FieldEffectType.Sound.mode,
                FieldEffectType.Music.mode -> {
                    mplew.writeMapleAsciiString(path)
                }
            }
            return mplew.packet
        }

        /**
         * onFieldEffect sends packet that broadcasts the BossHP bar
         *
         * @param result see FieldEffectType.kt
         * @param args the data needed to build the hp bar (see comments)
         */
        fun onFieldEffect(result: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FieldEffect.value)
            mplew.write(result)
            when (result) {
                FieldEffectType.BossHp.mode -> {
                    mplew.writeInt(args[0]) //object id
                    mplew.writeInt(args[1]) //current hp
                    mplew.writeInt(args[2]) //max hp
                    mplew.write(args[3].toByte()) // tag color byte
                    mplew.write(args[4].toByte()) // tag color background byte
                }
            }
            return mplew.packet
        }

        /**
         * onFieldObstacleOnOff
         *
         * @param mode  0: stop and back to start, 1: move
         * @param env
         */
        fun onFieldObstacleOnOff(env: String?, mode: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FieldObstacleOnOff.value)
            mplew.writeMapleAsciiString(env)
            mplew.writeInt(mode)
            return mplew.packet
        }

        fun onFieldObstacleOnOffReset(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FieldObstacleOnOffReset.value)
            return mplew.packet
        }

        /**
         * onBlowWeather returns packet that handles the full map weather message items
         *
         * @param itemId 0 for remove
         * @param msg
         * @param active 0 remove 1 broadcast
         */
        fun onBlowWeather(itemId: Int, msg: String?, active: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.BlowWeather.value)
            mplew.write(if (active) 0 else 1)
            mplew.writeInt(itemId)
            if (active) {
                mplew.writeMapleAsciiString(msg)
            }
            return mplew.packet
        }

        /**
         * onAdminResult the gm effect packet
         * responsible for both gm actions (hide, ban, etc) and action messages
         *
         * @param result Possible values:
         * <br> 0x04: You have successfully blocked access.
         * <br> 0x05: The unblocking has been successful.
         * <br> 0x06 with Mode 0: You have successfully removed the name from the ranks.
         * <br> 0x06 with Mode 1: You have entered an invalid character name.
         * <br> 0x10: GM Hide, toggles on or off.
         * <br> 0x1E: Mode 0: Failed to send warning Mode 1: Sent warning
         * @param mode see above for details
         */
        fun onAdminResult(result: Int, mode: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AdminResult.value)
            mplew.write(result)
            mplew.write(mode)
            return mplew.packet
        }

        /**
         * onQuiz returns and handles the packet pertaining to the ox quiz map event
         *
         * @param questionSet
         * @param questionId
         * @param askQuestion
         */
        fun onQuiz(questionSet: Int, questionId: Int, askQuestion: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Quiz.value)
            mplew.write(if (askQuestion) 1 else 0)
            mplew.write(questionSet)
            mplew.writeShort(questionId)

            return mplew.packet
        }

        /**
         * onDesc packet that broadcast GM event descriptions
         *
         * have not looked into the proper structure
         */
        fun onDesc(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Desc.value)
            mplew.write(0)

            return mplew.packet
        }

        /**
         * onClock packet responsible for displaying a timer on the map
         *
         * @param create true create new timer or false get old time remaining
         * @param args specifying the amount of time remaining (hour, minute, second)
         */
        fun onClock(create: Boolean, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.Clock.value)
            if (create) {
                //if you want to create a new clock instance you need to specify time in seconds
                mplew.write(2)
                mplew.writeInt(args[0]) //seconds for new timer
            } else {
                mplew.write(1)
                mplew.write(args[0]) //hours
                mplew.write(args[1]) // min
                mplew.write(args[2]) //seconds
            }
            return mplew.packet
        }

        /**
         * onDestroyClock
         * packet that removes clock/timer from map
         */
        fun onDestroyClock(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DestroyClock.value)
            mplew.write(0)
            return mplew.packet
        }

        /**
         * onShowArenaResult
         * packet that broadcasts the arena result for the ariant arena
         */
        fun onShowArenaResult(): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ShowArenaResult.value)
            return mplew.packet
        }
    }
}