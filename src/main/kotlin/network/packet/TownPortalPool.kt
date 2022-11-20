package network.packet

import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import java.awt.Point

class TownPortalPool {

    companion object Packet {

        /**
         * Gets a packet to spawn a door.
         *
         * @param oid The door's object ID.
         * @param pos The position of the door.
         * @param town
         *
         * @return The remove door packet.
         */
        fun onTownPortalCreated(oid: Int, pos: Point?, town: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.TownPortalCreated.value)
            mplew.writeBool(town)
            mplew.writeInt(oid)
            mplew.writePos(pos)

            return mplew.packet
        }

        fun onTownPortalRemoved(oid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(10)
            mplew.writeShort(SendOpcode.TownPortalRemoved.value)
            mplew.write(0)
            mplew.writeInt(oid)

            return mplew.packet
        }
    }
}