package network.packet

import network.opcode.SendOpcode
import server.maps.MapleHiredMerchant
import tools.data.output.MaplePacketLittleEndianWriter

class EmployeePool {

    // naming this packet for readability purposes when called in java
    companion object Packet {

        fun spawnHiredMerchantBox(hm: MapleHiredMerchant): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SPAWN_HIRED_MERCHANT.value)
            mplew.writeInt(hm.ownerId)
            mplew.writeInt(hm.itemId)
            mplew.writePos(hm.position)
            mplew.writeShort(hm.footHold) //fhId
            mplew.writeMapleAsciiString(hm.owner)
            mplew.write(0x05)
            mplew.writeInt(hm.objectId)
            mplew.writeMapleAsciiString(hm.description)
            mplew.write(hm.itemId % 100)
            mplew.write(byteArrayOf(1, 4))

            return mplew.packet
        }

        fun removeHiredMerchantBox(id: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.DESTROY_HIRED_MERCHANT.value)
            mplew.writeInt(id)

            return mplew.packet
        }

        fun updateHiredMerchantBox(hm: MapleHiredMerchant): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.UPDATE_HIRED_MERCHANT.value)
            mplew.writeInt(hm.ownerId)
            updateHiredMerchantBoxInfo(mplew, hm)

            return mplew.packet
        }

        private fun updateHiredMerchantBoxInfo(mplew: MaplePacketLittleEndianWriter, hm: MapleHiredMerchant) {
            val roomInfo = hm.shopRoomInfo
            mplew.write(5)
            mplew.writeInt(hm.objectId)
            mplew.writeMapleAsciiString(hm.description)
            mplew.write(hm.itemId % 100)
            mplew.write(roomInfo) // visitor capacity here, thanks GabrielSin!
        }
    }
}