package network.packet.field

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class ContiMove {

    companion object Packet {

        fun onContiMove(type: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ContiMove.value)
            mplew.write(10)
            mplew.write(if (type) 4 else 5)

            return mplew.packet
        }

        fun onContiState(type: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ContiState.value)
            mplew.write(if (type) 1 else 2)
            mplew.write(0)

            return mplew.packet
        }
    }
}