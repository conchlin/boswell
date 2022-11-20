package network.packet

import client.MapleCharacter
import network.opcode.SendOpcode
import server.maps.MapleMapItem
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point

class DropPool {

    companion object Packet {

        fun onDropEnterField(drop: MapleMapItem, giveOwnership: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DropEnterField.value)
            mplew.write(2)
            mplew.writeInt(drop.objectId)
            mplew.writeBool(drop.meso > 0)
            mplew.writeInt(drop.itemId)
            mplew.writeInt(if (giveOwnership) 0 else -1)
            mplew.write(if (drop.hasExpiredOwnershipTime()) 2 else drop.dropType)
            mplew.writePos(drop.position)
            mplew.writeInt(if (giveOwnership) 0 else -1)
            if (drop.meso == 0) {
                mplew.writeLong(PacketUtil.getTime(drop.item.expiration))
            }
            mplew.write(if (drop.isPlayerDrop) 0 else 1)

            return mplew.packet
        }

        fun onDropEnterField(
            player: MapleCharacter?,
            drop: MapleMapItem,
            dropfrom: Point?,
            dropto: Point?,
            mod: Byte
        ): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DropEnterField.value)
            mplew.write(mod) //nEnterType 0 1 2 3 - mob?
            mplew.writeInt(drop.objectId)
            mplew.writeBool(drop.meso > 0) // 1 mesos, 0 item, 2 and above all item meso bag,
            mplew.writeInt(drop.itemId) // drop object ID
            mplew.writeInt(drop.clientsideOwnerId) // owner charid/partyid :)
            mplew.write(if (drop.hasClientsideOwnership(player)) 2 else drop.dropType) // 0 = timeout for non-owner, 1 = timeout for non-owner's party, 2 = FFA, 3 = explosive/FFA
            mplew.writePos(dropto)
            mplew.writeInt(drop.dropper.objectId) // dropper oid, found thanks to Li Jixue
            if (mod.toInt() != 2) {
                mplew.writePos(dropfrom)
                mplew.writeShort(0) //Fh?
            }
            if (drop.meso == 0) {
                mplew.writeLong(PacketUtil.getTime(drop.item.expiration))
            }
            mplew.write(if (drop.isPlayerDrop) 0 else 1) //pet EQP pickup

            return mplew.packet
        }

        fun onDropLeaveField(oid: Int, animation: Int, cid: Int, pet: Boolean, slot: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DropLeaveField.value)
            mplew.write(animation) // expire
            mplew.writeInt(oid)
            if (animation >= 2) {
                mplew.writeInt(cid)
                if (pet) {
                    mplew.write(slot)
                }
            }

            return mplew.packet
        }

        fun onSilentDropLeaveField(oid: Int): ByteArray? {
            return onDropLeaveField(oid, 1, 0)
        }

        /**
         * animation: 0 - expire<br></br> 1 - without animation<br></br> 2 - pickup<br></br> 4 -
         * explode<br></br> cid is ignored for 0 and 1
         *
         * @param oid
         * @param animation
         * @param cid
         * @return
         */
        fun onDropLeaveField(oid: Int, animation: Int, cid: Int): ByteArray? {
            return onDropLeaveField(oid, animation, cid, false, 0)
        }
    }
}