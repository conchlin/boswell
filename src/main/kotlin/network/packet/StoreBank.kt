package network.packet

import client.MapleCharacter
import client.inventory.ItemFactory
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.sql.SQLException

class StoreBank {

    companion object Packet {

        fun onStoreBankMessage(operation: Byte): ByteArray? { // StoreBankGetAllResult
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FREDRICK_MESSAGE.value)
            mplew.write(operation)

            return mplew.packet
        }

        fun onStoreBankResult(chr: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.FREDRICK.value)
            mplew.write(0x23)
            mplew.writeInt(9030000) // Fredrick
            mplew.writeInt(32272) //id
            mplew.skip(5)
            mplew.writeInt(chr.merchantNetMeso)
            mplew.write(0)
            try {
                val items = ItemFactory.MERCHANT.loadItems(chr.id, false)
                mplew.write(items.size)
                for (item in items) {
                    PacketUtil.addItemInfoZeroPos(mplew, item.getLeft())
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            mplew.skip(3)
            return mplew.packet
        }
    }
}