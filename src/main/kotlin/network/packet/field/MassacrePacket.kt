package network.packet.field

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter

class MassacrePacket {
    /**
     * packets used in the PyramidPQ
     * not to be confused with AriantPQ
     */

    companion object Packet {

        /**
         * packet responsible for modifying the PyramidPQ energy gauge
         *
         * @param gauge new energy amount
         */
        fun onMassacreIncGauge(gauge: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(6)
            mplew.writeShort(SendOpcode.MassacreIncGauge.value)
            mplew.writeInt(gauge)

            return mplew.packet
        }

        /**
         * packet responsible for broadcasting PyramidPQ end score
         *
         * @param score the rank of PQ that cannot be higher than 4 (Rank D)
         * @param exp
         */
        fun onMassacreResult(score: Byte, exp: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(7)
            mplew.writeShort(SendOpcode.MassacreResult.value)
            mplew.write(score)
            mplew.writeInt(exp)

            return mplew.packet
        }
    }
}