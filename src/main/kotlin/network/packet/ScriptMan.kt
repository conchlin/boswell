package network.packet

import network.opcode.SendOpcode
import script.ScriptMessageType
import tools.data.output.MaplePacketLittleEndianWriter

class ScriptMan {
    companion object Packet {

        /**
         * todo handle the missing ScriptMessageType entries
         */

        fun onSay(
            speakerTemplateID: Int,
            text: String?,
            back: Boolean,
            next: Boolean
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(4.toByte()) //4 is for NPC conversation actions
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.Say.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(text)
            mplew.writeBool(back)
            mplew.writeBool(next)

            return mplew.packet
        }

        fun onAskYesNo(speakerTemplateID: Int, text: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(4.toByte()) //4 is for NPC conversation actions
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskYesNo.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(text)

            return mplew.packet
        }

        fun onAskText(
            speakerTemplateID: Int,
            msg: String?,
            msgDefault: String?,
            lenMin: Short,
            lenMax: Short
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(4.toByte()) //4 is for NPC conversation actions
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskText.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(msg)
            mplew.writeMapleAsciiString(msgDefault)
            mplew.writeShort(lenMin.toInt())
            mplew.writeShort(lenMax.toInt())

            return mplew.packet
        }

        fun onAskNumber(
            speakerTemplateID: Int,
            msg: String?,
            def: Int,
            min: Int,
            max: Int
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(4.toByte())
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskNumber.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(msg)
            mplew.writeInt(def)
            mplew.writeInt(min)
            mplew.writeInt(max)

            return mplew.packet
        }

        fun onAskMenu(speakerTypeID: Byte, speakerTemplateID: Int, msg: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(speakerTypeID)
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskMenu.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(msg)

            return mplew.packet
        }

        fun onAskAvatar(
            speakerTypeID: Byte,
            speakerTemplateID: Int,
            msg: String?,
            styles: IntArray
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(speakerTypeID)
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskAvatar.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(msg)
            mplew.write(styles.size)
            for (`val` in styles) {
                mplew.writeInt(`val`)
            }
            return mplew.packet
        }

        fun onAskAccept(speakerTemplateID: Int, text: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ScriptMessage.value)
            mplew.write(4.toByte()) //4 is for NPC conversation actions
            mplew.writeInt(speakerTemplateID)
            mplew.write(ScriptMessageType.AskAccept.type)
            mplew.write(0)
            mplew.writeMapleAsciiString(text)

            return mplew.packet
        }
    }
}