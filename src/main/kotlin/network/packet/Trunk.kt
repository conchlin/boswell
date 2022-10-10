package network.packet

import client.inventory.Item
import client.inventory.MapleInventoryType
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class Trunk {

    companion object Packet {

        fun getStorage(npcId: Int, slots: Byte, items: Collection<Item?>, meso: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(0x16)
            mplew.writeInt(npcId)
            mplew.write(slots)
            mplew.writeShort(0x7E)
            mplew.writeShort(0)
            mplew.writeInt(0)
            mplew.writeInt(meso)
            mplew.writeShort(0)
            mplew.write(items.size.toByte())
            for (item in items) {
                PacketUtil.addItemInfoZeroPos(mplew, item)
            }
            mplew.writeShort(0)
            mplew.write(0)

            return mplew.packet
        }

        // use TrunkErrorType.kt for error values
        fun getStorageError(i: Byte): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(i)

            return mplew.packet
        }

        fun mesoStorage(slots: Byte, meso: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(0x13)
            mplew.write(slots)
            mplew.writeShort(2)
            mplew.writeShort(0)
            mplew.writeInt(0)
            mplew.writeInt(meso)

            return mplew.packet
        }

        fun storeStorage(slots: Byte, type: MapleInventoryType, items: Collection<Item?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(0xD)
            mplew.write(slots)
            mplew.writeShort(type.bitfieldEncoding.toInt())
            mplew.writeShort(0)
            mplew.writeInt(0)
            mplew.write(items.size)
            for (item in items) {
                PacketUtil.addItemInfoZeroPos(mplew, item)
            }

            return mplew.packet
        }

        fun takeOutStorage(slots: Byte, type: MapleInventoryType, items: Collection<Item?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(0x9)
            mplew.write(slots)
            mplew.writeShort(type.bitfieldEncoding.toInt())
            mplew.writeShort(0)
            mplew.writeInt(0)
            mplew.write(items.size)
            for (item in items) {
                PacketUtil.addItemInfoZeroPos(mplew, item)
            }

            return mplew.packet
        }

        fun arrangeStorage(slots: Byte, items: Collection<Item?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.STORAGE.value)
            mplew.write(0xF)
            mplew.write(slots)
            mplew.write(124)
            mplew.skip(10)
            mplew.write(items.size)
            for (item in items) {
                PacketUtil.addItemInfoZeroPos(mplew, item)
            }
            mplew.write(0)

            return mplew.packet
        }

    }
}