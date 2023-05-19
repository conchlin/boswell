package database.tables

import database.DatabaseConnection.Companion.getConnection
import tools.Pair
import java.util.*

class MakerTbl {

    companion object {

        @JvmStatic
        fun loadDisassembleFee(itemId: Int): Int {
            var fee = -1
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT req_meso FROM maker_create_data WHERE itemid = ?").use { ps ->
                        ps.setInt(1, itemId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {   // cost is 13.6363~ % of the original value trimmed by 1000.
                                val req = (rs.getInt("req_meso") * 0.13636363636364).toFloat()
                                fee = (req / 1000).toInt()
                                fee *= 1000
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return fee
        }

        @JvmStatic
        fun loadDisassembledItems(itemId: Int): MutableList<Pair<Int, Int>> {
            val items: MutableList<Pair<Int, Int>> = LinkedList()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT req_item, count FROM maker_recipe_data WHERE itemid = ? AND req_item >= 4260000 AND req_item < 4270000")
                        .use { ps ->
                            ps.setInt(1, itemId)
                            ps.executeQuery().use { rs ->
                                while (rs.next()) {
                                    // TODO im not sure whether this value is actually half the crystals
                                    //  needed for creation or slightly randomized
                                    items.add(
                                        Pair(
                                            rs.getInt("req_item"), rs.getInt("count") / 2
                                        )
                                    )
                                }
                            }
                        }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return items
        }

        @JvmStatic
        fun loadReagentStatUpgrade(itemId: Int): Pair<String, Int>? {
            var statUpgrade: Pair<String, Int>? = null
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT stat, value FROM maker_reagent_data WHERE itemid = ?").use { ps ->
                        ps.setInt(1, itemId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                val statType = rs.getString("stat")
                                val statGain = rs.getInt("value")
                                statUpgrade = Pair(statType, statGain)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return statUpgrade
        }

        @JvmStatic
        fun loadLevelRequirement(itemToCreate: Int): Array<Int>? {
            val requirements: Array<Int>? = null
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT req_level, req_maker_level, req_meso, quantity FROM maker_create_data WHERE itemid = ?")
                        .use { ps ->
                            ps.setInt(1, itemToCreate)
                            ps.executeQuery().use { rs ->
                                if (rs.next()) {
                                    requirements?.set(0, rs.getInt("req_meso"))
                                    requirements?.set(1, rs.getInt("req_level"))
                                    requirements?.set(2, rs.getInt("req_maker_level"))
                                    requirements?.set(3, rs.getInt("quantity"))
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return requirements
        }

        @JvmStatic
        fun loadItemRequirement(itemToCreate: Int): Pair<Int, Int>? {
            val requirements: Pair<Int, Int>? = null
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT req_item, count FROM maker_recipe_data WHERE itemid = ?").use { ps ->
                        ps.setInt(1, itemToCreate)
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                requirements?.left = rs.getInt("req_item")
                                requirements?.right = rs.getInt("count")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return requirements
        }
    }

}