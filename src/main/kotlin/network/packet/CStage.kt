package network.packet

import client.MapleCharacter
import client.MapleClient
import net.server.Server
import network.opcode.SendOpcode
import server.cashshop.CashItemFactory
import server.cashshop.CommodityFlags
import tools.Randomizer
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.awt.Point

class CStage {

    companion object Packet {

        /**
         * packet responsible for communicating with the client that the user is changing fields
         *
         * @param user the MapleCharacter instance of the changing fields
         * @param destination the id of the field being traveled to
         * @param spawnPoint the number portal the user is spawning at
         * @param exactSpawnPoint is there a need to create a specific (x,y) spawn Point?
         * @param args if posSpawn is true/needed we can pass a specific Point in the args
         */
        fun onSetField(user: MapleCharacter, destination: Int, spawnPoint: Int, exactSpawnPoint: Boolean, vararg args: Point): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetField.value)
            mplew.writeInt(user.client.channel - 1)
            mplew.writeInt(0)
            mplew.write(0)
            mplew.writeInt(destination)
            mplew.write(spawnPoint)
            mplew.writeShort(user.hp)
            mplew.writeBool(exactSpawnPoint)
            if (exactSpawnPoint) {
                mplew.writeInt(args[0].x)
                mplew.writeInt(args[0].y)
            }
            mplew.writeLong(PacketUtil.getTime(Server.getInstance().currentTime))
            mplew.skip(18)

            return mplew.packet
        }

        /**
         * packet responsible for processing character info when adding user into field
         *
         * @param user
         */
        fun onSetField(user: MapleCharacter): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetField.value)
            mplew.writeInt(user.client.channel - 1)
            mplew.write(1)
            mplew.write(1)
            mplew.writeShort(0)
            for (i in 0..2) {
                mplew.writeInt(Randomizer.nextInt())
            }
            PacketUtil.addCharacterInfo(mplew, user)
            mplew.writeLong(PacketUtil.getTime(System.currentTimeMillis()))

            return mplew.packet
        }

        /**
         * packet responsible for adding user to the cash shop
         *
         * @param c the client being sent to the cash shop
         * @param betaCs
         */
        fun onSetCashShop(c: MapleClient, betaCs: Boolean): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.SetCashShop.value)
            PacketUtil.addCharacterInfo(mplew, c.player)
            mplew.writeBool(!betaCs)
            //sub_A25DB4
            if (!betaCs) {
                mplew.writeMapleAsciiString(c.accountName)
            }
            val blocked = CashItemFactory.getBlockedCashItems()
            mplew.writeInt(blocked.size)
            for (serial in blocked) {
                mplew.writeInt(serial!!)
            }
            val lsci = CashItemFactory.getSpecialCashItems()
            mplew.writeShort(lsci.size)
            for (sci in lsci) {
                mplew.writeInt(sci.sn)
                val flag = sci.flag
                mplew.writeInt(flag)
                if (flag and CommodityFlags.ITEM_ID.flag == CommodityFlags.ITEM_ID.flag) {
                    mplew.writeInt(sci.itemId)
                }
                if (flag and CommodityFlags.COUNT.flag == CommodityFlags.COUNT.flag) {
                    mplew.writeShort(sci.count)
                }
                if (flag and CommodityFlags.PRICE.flag == CommodityFlags.PRICE.flag) {
                    mplew.writeInt(sci.price)
                }
                if (flag and CommodityFlags.PRIORITY.flag == CommodityFlags.PRIORITY.flag) {
                    mplew.write(sci.priority)
                }
                if (flag and CommodityFlags.PERIOD.flag == CommodityFlags.PERIOD.flag) {
                    mplew.writeShort(sci.period)
                }
                if (flag and CommodityFlags.MAPLE_POINTS.flag == CommodityFlags.MAPLE_POINTS.flag) {
                    mplew.writeInt(sci.period)
                }
                if (flag and CommodityFlags.MESOS.flag == CommodityFlags.MESOS.flag) {
                    mplew.writeInt(sci.mesos)
                }
                if (flag and CommodityFlags.PREMIUM_USER.flag == CommodityFlags.PREMIUM_USER.flag) {
                    mplew.writeBool(sci.isPremiumUser)
                }
                if (flag and CommodityFlags.GENDER.flag == CommodityFlags.GENDER.flag) {
                    mplew.write(sci.gender)
                }
                if (flag and CommodityFlags.SALE.flag == CommodityFlags.SALE.flag) {
                    mplew.writeBool(sci.sale)
                }
                if (flag and CommodityFlags.CLASS.flag == CommodityFlags.CLASS.flag) {
                    mplew.write(sci.job)
                }
                if (flag and CommodityFlags.REQUIRED_LEVEL.flag == CommodityFlags.REQUIRED_LEVEL.flag) {
                    mplew.writeShort(sci.requiredLevel)
                }
                if (flag and CommodityFlags.CASH.flag == CommodityFlags.CASH.flag) {
                    mplew.writeShort(sci.cash)
                }
                if (flag and CommodityFlags.POINT.flag == CommodityFlags.POINT.flag) {
                    mplew.writeShort(sci.point)
                }
                if (flag and CommodityFlags.GIFT.flag == CommodityFlags.GIFT.flag) {
                    mplew.writeShort(sci.gift)
                }
                if (flag and CommodityFlags.PACKAGE_COUNT.flag == CommodityFlags.PACKAGE_COUNT.flag) {
                    mplew.write(sci.items.size)
                    for (item in sci.items) {
                        mplew.writeInt(item!!)
                    }
                }
                if (flag and CommodityFlags.LIMIT.flag == CommodityFlags.LIMIT.flag) {
                    mplew.write(sci.limit)
                }
            }
            val discounts = CashItemFactory.getDiscountedCategories()
            mplew.write(discounts.size) //size
            for (discount in discounts) {
                mplew.write(discount.category)
                mplew.write(discount.subCategory)
                mplew.write(discount.discontRate)
            }

            for (i in 0..89) {
                mplew.writeInt(0)
                mplew.writeInt(0)
                mplew.writeInt(0)
            }
            val stock = CashItemFactory.getStock()
            mplew.writeShort(stock.size)
            for (item in stock) {
                mplew.writeInt(item.sn)
                mplew.writeInt(item.stockState)
            }
            val goods = CashItemFactory.getLimitedGoods()
            mplew.writeShort(goods.size) //DecodeLimitGoods
            for (good in goods) {
                mplew.writeInt(good.startSN)
                mplew.writeInt(good.endSN)
                mplew.writeInt(good.goodsCount)
                mplew.writeInt(good.eventSN)
                mplew.writeInt(good.expireDays)
                mplew.writeInt(good.flag)
                mplew.writeInt(good.startDate)
                mplew.writeInt(good.endDate)
                mplew.writeInt(good.startHour)
                mplew.writeInt(good.endHour)
                for (day in good.daysOfWeek) {
                    mplew.writeInt(day)
                }
                mplew.write(ByteArray(36))
            }
            val zeroGoods = CashItemFactory.getLimitedGoods()
            mplew.writeShort(goods.size)
            for (good in zeroGoods) {
                mplew.writeInt(good.startSN)
                mplew.writeInt(good.endSN)
                mplew.writeInt(good.goodsCount)
                mplew.writeInt(good.eventSN)
                mplew.writeInt(good.expireDays)
                mplew.writeInt(good.flag)
                mplew.writeInt(good.startDate)
                mplew.writeInt(good.endDate)
                mplew.writeInt(good.startHour)
                mplew.writeInt(good.endHour)
                for (day in good.daysOfWeek) {
                    mplew.writeInt(day)
                }
            }
            mplew.write(0) //CShopInfo::IsEventOn
            mplew.writeInt(75)
            return mplew.packet
        }
    }
}