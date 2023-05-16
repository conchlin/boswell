package database.tables

import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.*
import java.sql.SQLException
import java.sql.Statement

class CharactersTbl {

    companion object {

        @JvmStatic
        fun deleteCharacter(userId: Int) {
            try {
                getConnection().use { con ->
                    Delete.from("characters").where("id", userId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        /**
         * @return the newly created userId
         */
        @JvmStatic
        fun createNewChar(
            skinColor: Int,
            gender: Int,
            job: Int,
            hair: Int,
            face: Int,
            fieldId: Int,
            accountId: Int,
            name: String,
            world: Int
        ) {
            try {
                getConnection().use { con ->
                    val statement = Insert("characters")
                    statement.add("skincolor", skinColor)
                    statement.add("gender", gender)
                    statement.add("job", job)
                    statement.add("hair", hair)
                    statement.add("face", face)
                    statement.add("map", fieldId)
                    statement.add("spawnpoint", 0)
                    statement.add("accountid", accountId)
                    statement.add("name", name)
                    statement.add("world", world)
                    statement.execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun disbandGuild(guildId: Int) {
            try {
                getConnection().use { con ->
                    Update("characters")
                        .set("guildid", 0)
                        .set("guildrank", 5)
                        .where("guildid", guildId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        /**
         * add a user to guild
         *
         * @param guildId
         * @param userId the one being invited
         */
        @JvmStatic
        fun updateGuild(guildId: Int, userId: Int) {
            try {
                getConnection().use { con ->
                    Update("characters").set("guildid", guildId).where("id", userId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateName(newName: String, userId: Int) {
            try {
                getConnection().use { con ->
                    Update("characters").set("name", newName).where("id", userId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        /**
         * Updates all characters
         */
        @JvmStatic
        fun updateHasMerchant(active: Boolean) {
            try {
                getConnection().use { con ->
                    Update("characters").set("hasmerchant", active).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateHasMerchant(active: Boolean, userId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE characters SET HasMerchant = ? WHERE id = ?").use { ps ->
                        ps.setBoolean(1, active)
                        ps.setInt(2, userId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        /**
         * Updates the MerchantMesos column based on the current value of the column
         */
        @JvmStatic
        fun loadMerchantMesos(userId: Int): Long {
            try {
                getConnection().use { con ->
                    var merchantMesos: Long = 0
                    con!!.prepareStatement("SELECT MerchantMesos FROM characters WHERE id = ?").use { ps ->
                        ps.setInt(1, userId)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                merchantMesos = rs.getInt(1).toLong()
                            }
                        }
                    }
                    return merchantMesos
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return 0
        }

        @JvmStatic
        fun updateMerchantMesos(mesosGained: Int, userId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement(
                        "UPDATE characters SET MerchantMesos = ? WHERE id = ?",
                        Statement.RETURN_GENERATED_KEYS
                    ).use { ps ->
                        ps.setInt(1, mesosGained.coerceAtMost(Integer.MAX_VALUE))
                        ps.setInt(2, userId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun loadGMLevel(userName: String): Int {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT gm FROM characters WHERE name = ?").use { ps ->
                        ps.setString(1, userName)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                    return rs.getInt("gm")
                                }
                            }
                        }
                    }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return 0
        }

        @JvmStatic
        fun resetAllianceRanks(guildId: Int) {
            try {
                getConnection().use { con ->
                    Update("characters").set("allianceRank", 5).where("guildid", guildId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun loadIdsFromUsername(userName: String): MutableMap<String, String>? {
            val character: MutableMap<String, String> = LinkedHashMap()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT id, accountid, name FROM characters WHERE name = ?").use { ps ->
                        ps.setString(1, userName)
                        ps.executeQuery().use { rs ->
                            if (!rs.next()) {
                                return null
                            }
                            for (i in 1..rs.metaData.columnCount) {
                                character[rs.metaData.getColumnLabel(i)] = rs.getString(i)
                            }
                        }
                    }
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }

            return character
        }

        @JvmStatic
        fun loadAccountIdByUsername(userName: String): Int {
            var id: Int
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT accountid FROM characters WHERE name = ?").use { ps ->
                        ps.setString(1, userName)
                        ps.executeQuery().use { rs ->
                            if (!rs.next()) {
                                return -1
                            }
                            id = rs.getInt("accountid")
                        }
                    }
                    return id
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return -1
        }

        @JvmStatic
        fun loadNameById(userId: Int): String? {
            var name: String
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT name FROM characters WHERE id = ?").use { ps ->
                        ps.setInt(1, userId)
                        ps.executeQuery().use { rs ->
                            if (!rs.next()) {
                                return null
                            }
                            name = rs.getString("name")
                        }
                    }
                    return name
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }

        @JvmStatic
        fun loadIdByName(userName: String): Int {
            var id: Int
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT id FROM characters WHERE name ilike ?").use { ps ->
                        ps.setString(1, userName)
                        ps.executeQuery().use { rs ->
                            if (!rs.next()) {
                                return -1
                            }
                            id = rs.getInt("id")
                        }
                    }
                    return id
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return -1
        }
    }
}