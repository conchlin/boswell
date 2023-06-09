package database.tables

import client.MapleCharacter
import client.processor.FredrickProcessor
import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.*
import net.server.Server
import net.server.guild.MapleGuildCharacter
import java.sql.SQLException
import java.sql.Statement

class CharactersTbl {

    companion object {

        @JvmStatic
        fun deleteCharacter(user: MapleCharacter): Boolean {
            var world = 0
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT world FROM characters WHERE id = ?").use { ps ->
                        ps.setInt(1, user.id)
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                world = rs.getInt("world")
                            }
                        }
                    }
                    con.prepareStatement("SELECT buddyid FROM buddies WHERE characterid = ?").use { ps ->
                        ps.setInt(1, user.id)
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val buddyId = rs.getInt("buddyid")
                                val buddy =
                                    Server.getInstance().getWorld(world).playerStorage.getCharacterById(buddyId)
                                buddy?.deleteBuddy(user.id)
                            }
                        }
                    }
                    Delete.from("buddies").where("characterid", user.id).execute(con)
                    con.prepareStatement("SELECT threadid FROM bbs_threads WHERE postercid = ?").use { ps ->
                        ps.setInt(1, user.id)
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                Delete.from("bbs_replies").where("threadid", rs.getInt("threadid"))
                                    .execute(con)
                            }
                        }
                    }
                    Delete.from("bbs_threads").where("postercid", user.id).execute(con)
                    con.prepareStatement("SELECT id, guildid, guildrank, name, allianceRank FROM characters WHERE id = ? AND accountid = ?")
                        .use { ps ->
                            ps.setInt(1, user.id)
                            ps.setInt(2, user.accountID)
                            ps.executeQuery().use { rs ->
                                if (rs.next() && rs.getInt("guildid") > 0) {
                                    Server.getInstance().deleteGuildCharacter(
                                        MapleGuildCharacter(
                                            user,
                                            user.id,
                                            0,
                                            rs.getString("name"),
                                            1.toByte().unaryMinus(),
                                            1.toByte().unaryMinus(),
                                            0,
                                            rs.getInt("guildrank"),
                                            rs.getInt("guildid"),
                                            false,
                                            rs.getInt("allianceRank")
                                        )
                                    )
                                }
                            }
                        }
                    Delete.from("wish_lists").where("charid", user.id).execute(con)
                    Delete.from("cooldowns").where("charid", user.id).execute(con)
                    Delete.from("player_diseases").where("charid", user.id).execute(con)
                    Delete.from("area_info").where("charid", user.id).execute(con)
                    Delete.from("monster_book").where("charid", user.id).execute(con)
                    Delete.from("characters").where("id", user.id).execute(con)
                    Delete.from("fame_log").where("characterid_to", user.id).execute(con)
                    con.prepareStatement("SELECT inventoryitemid, petid FROM inventory_items WHERE characterid = ?")
                        .use { ps ->
                            ps.setInt(1, user.id)
                            ps.executeQuery().use { rs ->
                                while (rs.next()) {
                                    val inventoryItemId = rs.getLong("inventoryitemid")
                                    con.prepareStatement("SELECT ringid FROM inventory_equipment WHERE inventoryitemid = ?")
                                        .use { ps2 ->
                                            ps2.setLong(1, inventoryItemId)
                                            ps2.executeQuery().use { rs2 ->
                                                while (rs2.next()) {
                                                    Delete.from("rings").where("id", rs2.getInt("ringid"))
                                                        .execute(con)
                                                }
                                            }
                                        }
                                    Delete.from("inventory_equipment").where("inventoryitemid", inventoryItemId)
                                        .execute(con)
                                    Delete.from("pets").where("petid", rs.getInt("petid")).execute(con)
                                }
                            }
                        }
                    FredrickProcessor.removeFredrickLog(user.id) // thanks maple006 for pointing out the player's Fredrick items are not being deleted at character deletion
                    con.prepareStatement("SELECT id FROM mts_cart WHERE cid = ?").use { ps ->
                        ps.setInt(1, user.id)
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val mtsId = rs.getInt("id")
                                Delete.from("mts_items").where("id", mtsId).execute(con)
                            }
                        }
                    }
                    Delete.from("mts_cart").where("cid", user.id).execute(con)
                    val toDel = arrayOf(
                        "fame_log",
                        "inventory_items",
                        "keymap",
                        "medal_maps",
                        "quest_status",
                        "quest_progress",
                        "saved_locations",
                        "trock_locations",
                        "skill_macros",
                        "skills",
                        "event_stats",
                        "server_queue"
                    )
                    for (s in toDel) {
                        Delete.from(s).where("characterid", user.id).execute(con)
                    }
                    Server.getInstance().deleteCharacterEntry(user.accountID, user.id)
                    return true
                }
            } catch (e: SQLException) {
                e.printStackTrace()
                return false
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
                    statement.executeUpdate(con!!)
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