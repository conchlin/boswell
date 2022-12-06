package network.packet

import enums.FieldEffectType
import enums.WhisperResultType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

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
            mplew.writeShort(SendOpcode.WHISPER.value)
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
    }
}