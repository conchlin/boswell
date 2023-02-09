package network.packet

import tools.data.output.MaplePacketLittleEndianWriter

class TCPHandshake {

    companion object Packet {

        /**
         * authenticate connection between network and client
         */
        fun onHandshake(version: Short, sendIv: ByteArray?, recvIv: ByteArray?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(8)
            mplew.writeShort(0x0E)
            mplew.writeShort(version.toInt())
            mplew.writeShort(1)
            mplew.write(49)
            mplew.write(recvIv)
            mplew.write(sendIv)
            mplew.write(8)
            return mplew.packet
        }
    }
}