package network.packet

import network.opcode.SendOpcode
import tools.HexTool
import tools.data.output.MaplePacketLittleEndianWriter

class ScriptMan {
    companion object Packet {

        /**
         * Possible values for `speaker`:<br></br> 0: Npc talking (left)<br></br>
         * 1: Npc talking (right)<br></br> 2: Player talking (left)<br></br> 3: Player talking
         * (left)<br></br>
         *
         * @param npc Npcid
         * @param msgType
         * @param talk
         * @param endBytes
         * @param speaker
         * @return
         */
        fun getNPCTalk(npc: Int, msgType: Byte, talk: String?, endBytes: String?, speaker: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_TALK.value)
            mplew.write(4) // ?
            mplew.writeInt(npc)
            mplew.write(msgType)
            mplew.write(speaker)
            mplew.writeMapleAsciiString(talk)
            mplew.write(HexTool.getByteArrayFromHexString(endBytes))

            return mplew.packet
        }

        fun getDimensionalMirror(talk: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_TALK.value)
            mplew.write(4) // ?
            mplew.writeInt(9010022)
            mplew.write(0x0E)
            mplew.write(0)
            mplew.writeInt(0)
            mplew.writeMapleAsciiString(talk)

            return mplew.packet
        }

        fun getNPCTalkStyle(npc: Int, talk: String?, styles: IntArray): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_TALK.value)
            mplew.write(4) // ?
            mplew.writeInt(npc)
            mplew.write(7)
            mplew.write(0) //speaker
            mplew.writeMapleAsciiString(talk)
            mplew.write(styles.size)
            for (style in styles) {
                mplew.writeInt(style)
            }

            return mplew.packet
        }

        fun getNPCTalkNum(npc: Int, talk: String?, def: Int, min: Int, max: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_TALK.value)
            mplew.write(4) // ?
            mplew.writeInt(npc)
            mplew.write(3)
            mplew.write(0) //speaker
            mplew.writeMapleAsciiString(talk)
            mplew.writeInt(def)
            mplew.writeInt(min)
            mplew.writeInt(max)
            mplew.writeInt(0)

            return mplew.packet
        }

        fun getNPCTalkText(npc: Int, talk: String?, def: String?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.NPC_TALK.value)
            mplew.write(4) // Doesn't matter
            mplew.writeInt(npc)
            mplew.write(2)
            mplew.write(0) //speaker
            mplew.writeMapleAsciiString(talk)
            mplew.writeMapleAsciiString(def) //:D
            mplew.writeInt(0)

            return mplew.packet
        }
    }
}