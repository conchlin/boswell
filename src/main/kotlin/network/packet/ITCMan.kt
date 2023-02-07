package network.packet

import client.MapleCharacter
import enums.ITCQueryCashResultType
import network.opcode.SendOpcode
import server.MTSItemInfo
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class ITCMan {
    /**
     * standard practice for private servers is to replace the maple trading system
     * with an FM warp button so these packets are largely unused
     */

    companion object Packet {

        fun onChargeParamResult(p: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ITCChargeParamResult.value)
            mplew.writeInt(p.cashShop.getCash(4))
            mplew.writeInt(p.cashShop.getCash(2))

            return mplew.packet
        }

        fun onQueryCashResult(items: List<MTSItemInfo>, tab: Int, type: Int, page: Int, pages: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ITCQueryCashResult.value)
            mplew.write(21)
            mplew.writeInt(pages * 16) //testing, change to 10 if fails
            mplew.writeInt(items.size) //number of items
            mplew.writeInt(tab)
            mplew.writeInt(type)
            mplew.writeInt(page)
            mplew.write(1)
            mplew.write(1)
            for (item in items) {
                PacketUtil.addItemInfoZeroPos(mplew, item.item)
                mplew.writeInt(item.id) //id
                mplew.writeInt(item.taxes) //this + below = price
                mplew.writeInt(item.price) //price
                mplew.writeInt(0)
                mplew.writeLong(PacketUtil.getTime(item.endingDate))
                mplew.writeMapleAsciiString(item.seller)
                mplew.writeMapleAsciiString(item.seller)
                for (j in 0..27) {
                    mplew.write(0)
                }
            }
            mplew.write(1)
            return mplew.packet
        }

        /**
         * @param type see ITCQueryCashResultType.kt
         */
        fun onQueryCashResult(type: Int, items: List<MTSItemInfo>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ITCQueryCashResult.value)
            mplew.write(type)
            when (type) {
                ITCQueryCashResultType.TransferInventory.type -> {
                    mplew.writeInt(items.size)
                    if (items.isNotEmpty()) {
                        for (item in items) {
                            PacketUtil.addItemInfoZeroPos(mplew, item.item)
                            mplew.writeInt(item.id)
                            mplew.writeInt(item.taxes)
                            mplew.writeInt(item.price)
                            mplew.writeInt(0)
                            mplew.writeLong(PacketUtil.getTime(item.endingDate))
                            mplew.writeMapleAsciiString(item.seller)
                            mplew.writeMapleAsciiString(item.seller)
                            for (i in 0..27) {
                                mplew.write(0)
                            }
                        }
                    }
                    mplew.write(0xD0 + items.size)
                    mplew.write(byteArrayOf(-1, -1, -1, 0))
                }
                ITCQueryCashResultType.NotYetSold.type -> {
                    mplew.writeInt(items.size)
                    if (items.isNotEmpty()) {
                        for (item in items) {
                            PacketUtil.addItemInfoZeroPos(mplew, item.item)
                            mplew.writeInt(item.id)
                            mplew.writeInt(item.taxes)
                            mplew.writeInt(item.price)
                            mplew.writeInt(0)
                            mplew.writeLong(PacketUtil.getTime(item.endingDate))
                            mplew.writeMapleAsciiString(item.seller)
                            mplew.writeMapleAsciiString(item.seller)
                            for (i in 0..27) {
                                mplew.write(0)
                            }
                        }
                    } else {
                        mplew.writeInt(0)
                    }
                }
            }
            return mplew.packet
        }

        fun onQueryCashResult(type: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.ITCQueryCashResult.value)
            mplew.write(type)
            when (type) {
                ITCQueryCashResultType.ConfirmTransfer.type -> {
                    mplew.writeInt(args[0]) //quantity
                    mplew.writeInt(args[1]) //pos
                }
                ITCQueryCashResultType.FailBuy.type -> {
                    mplew.write(66)
                }
                ITCQueryCashResultType.WantList.type -> {
                    mplew.writeInt(args[0]) //nx
                    mplew.writeInt(args[1])//items
                }
            }

            return mplew.packet
        }
    }
}