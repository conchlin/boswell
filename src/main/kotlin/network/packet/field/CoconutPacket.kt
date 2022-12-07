package network.packet.field

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class CoconutPacket {

    companion object Packet {

        fun onHitCoconut(spawn: Boolean, id: Int, type: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.CoconutHit.value)
            if (spawn) {
                mplew.writeShort(-1)
                mplew.writeShort(5000)
                mplew.write(0)
            } else {
                mplew.writeShort(id)
                mplew.writeShort(1000)//attack delay
                mplew.write(type) //action
            }
            return mplew.packet
        }

        fun onCoconutScore(team1: Int, team2: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.CoconutScore.value)
            mplew.writeShort(team1)
            mplew.writeShort(team2)

            return mplew.packet
        }
    }
}