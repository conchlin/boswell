package database.tables

import database.DatabaseConnection.Companion.getConnection
import server.cashshop.*
import java.sql.SQLException

class CashShopTbl {

    companion object {

        @JvmStatic
        fun loadSpecialCSItems(): ArrayList<SpecialCashItem> {
            val items = ArrayList<SpecialCashItem>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM cs_modded_commodity").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val cash = SpecialCashItem(rs.getInt("sn"), rs.getInt("item_id"))
                                cash.count = rs.getInt("count")
                                cash.priority = rs.getInt("priority")
                                cash.period = rs.getInt("period")
                                cash.maplePoints = rs.getInt("maple_point")
                                cash.mesos = rs.getInt("mesos")
                                cash.isPremiumUser = rs.getBoolean("premium_user")
                                cash.requiredLevel = rs.getInt("required_level")
                                cash.gender = rs.getInt("gender")
                                cash.sale = rs.getBoolean("sale")
                                cash.job = rs.getInt("class")
                                cash.limit = rs.getInt("_limit")
                                cash.cash = rs.getInt("pb_cash")
                                val contents =
                                    rs.getString("package_contents").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                        .toTypedArray()
                                for (content in contents) {
                                    cash.items.add(content.toInt())
                                }
                                cash.point = rs.getInt("pb_point")
                                cash.gift = rs.getInt("pb_gift")
                                items.add(cash)
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return items
        }

        @JvmStatic
        fun loadBlockedItems(): ArrayList<Int> {
            val items = ArrayList<Int>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM cs_blocked_items").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                items.add(rs.getInt("sn"))
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return items
        }

        @JvmStatic
        fun loadDiscountedCategories(): ArrayList<CategoryDiscount> {
            val discounts = ArrayList<CategoryDiscount>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM cs_discounted_categories").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                discounts.add(
                                    CategoryDiscount(
                                        rs.getInt("category"), rs.getInt("subcategory"), rs.getInt("discount_rate")
                                    )
                                )
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return discounts
        }

        /**
         * There are only two valid types that can be used -> limited and zero
         * each type corresponds with different tables
         * not a great way to handle this, but it's better than having two of the same methods
         *
         * @param table limited or zero
         */
        @JvmStatic
        fun loadGoods(table: String): ArrayList<LimitedGoods> {
            val lg = ArrayList<LimitedGoods>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM cs_${table}_goods").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val goods = LimitedGoods(rs.getInt("start_sn"), rs.getInt("end_sn"))
                                goods.goodsCount = rs.getInt("count")
                                goods.eventSN = rs.getInt("event_sn")
                                goods.expireDays = rs.getInt("expire")
                                goods.flag = rs.getInt("flag")
                                goods.startDate = rs.getInt("start_date")
                                goods.endDate = rs.getInt("end_date")
                                goods.startHour = rs.getInt("start_hour")
                                goods.endHour = rs.getInt("end_hour")
                                val days = rs.getString("days").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                                val daysOfWeek = IntArray(7)
                                for (i in days.indices) {
                                    daysOfWeek[i] = days[i].toInt()
                                }
                                goods.daysOfWeek = daysOfWeek
                                lg.add(goods)
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return lg
        }

        @JvmStatic
        fun loadStock(): ArrayList<ItemStock> {
            val itemStock = ArrayList<ItemStock>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM cs_stock").use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                itemStock.add(ItemStock(rs.getInt("sn"), rs.getInt("state")))
                            }
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return itemStock
        }
    }
}