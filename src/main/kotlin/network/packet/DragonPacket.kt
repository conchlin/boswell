package network.packet

import network.opcode.SendOpcode
import server.maps.MapleDragon
import server.movement.LifeMovementFragment
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point

class DragonPacket {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        /**
         * these dragon packets are in the client but the Evan class was not released until v84
         */

        fun onEnterField(dragon: MapleDragon): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DragonEnterField.value)
            mplew.writeInt(dragon.owner.id) //objectid = owner id
            mplew.writeShort(dragon.position.x)
            mplew.writeShort(0)
            mplew.writeShort(dragon.position.y)
            mplew.writeShort(0)
            mplew.write(dragon.stance)
            mplew.write(0)
            mplew.writeShort(dragon.owner.job.id)

            return mplew.packet
        }

        fun onMove(dragon: MapleDragon, p: Point?, res: List<LifeMovementFragment?>?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DragonMove.value)
            mplew.writeInt(dragon.owner.id)
            mplew.writePos(p)
            PacketUtil.serializeMovementList(mplew, res)

            return mplew.packet
        }

        fun onRemoveField(charid: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DragonLeaveField.value)
            mplew.writeInt(charid)

            return mplew.packet
        }
    }
}