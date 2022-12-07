package network.packet.field

import client.MapleCharacter
import network.opcode.SendOpcode
import tools.MaplePacketCreator
import tools.data.output.MaplePacketLittleEndianWriter

class AriantArenaPacket {

    companion object Packet {

        fun onUserScore(playerScore: Map<MapleCharacter, Int>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ArenaUserScore.value)
            mplew.write(playerScore.size)
            playerScore.forEach { entry ->
                mplew.writeMapleAsciiString(entry.key.name)
                mplew.writeInt(entry.value)
            }
            return mplew.packet
        }

    }
}