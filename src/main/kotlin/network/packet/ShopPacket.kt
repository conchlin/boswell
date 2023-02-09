package network.packet

import client.MapleClient
import constants.ItemConstants
import network.opcode.SendOpcode
import server.MapleItemInformationProvider
import server.MapleShopItem
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class ShopPacket {

    companion object Packet {

        fun onOpenShopDlg(c: MapleClient?, sid: Int, items: List<MapleShopItem>): ByteArray? {
            val ii = MapleItemInformationProvider.getInstance()
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.OpenShopDlg.value)
            mplew.writeInt(sid)
            mplew.writeShort(items.size)
            for (item in items) {
                mplew.writeInt(item.itemId)
                mplew.writeInt(item.price)
                mplew.writeInt(if (item.price == 0) item.pitch else 0)
                mplew.writeInt(0)
                mplew.writeInt(0)
                if (!ItemConstants.isRechargeable(item.itemId)) {
                    mplew.writeShort(1)
                    mplew.writeShort(item.buyable.toInt())
                } else {
                    mplew.writeShort(0)
                    mplew.writeInt(0)
                    mplew.writeShort(PacketUtil.doubleToShortBits(ii.getUnitPrice(item.itemId)))
                    mplew.writeShort(ii.getSlotMax(c, item.itemId).toInt())
                }
            }
            return mplew.packet
        }

        /**
         * packet responsible for shop result messages
         *
         * @param result see ShopResultType
         */
        fun onShopResult(result: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter(3)
            mplew.writeShort(SendOpcode.ShopResult.value)
            mplew.write(result)

            return mplew.packet
        }

    }
}