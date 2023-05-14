package database.tables

import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.Delete
import database.DatabaseStatements.Insert.Companion.into
import database.DatabaseStatements.Update
import java.sql.SQLException

class GuildsTbl {

    companion object {

        /**
         * @return the new guild ID or 0 for an error
         */
        @JvmStatic
        fun createGuild(leaderId: Int, guildName: String): Int {
            try {
                getConnection().use { con ->
                    return into("guilds")
                        .add("leader", leaderId)
                        .add("name", guildName)
                        .add("signature", System.currentTimeMillis().toInt())
                        .executeUpdate(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return 0
        }

        @JvmStatic
        fun checkNameAvailability(name: String): Boolean {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT guildid FROM guilds WHERE name = ?").use { checkPs ->
                        checkPs.setString(1, name)
                        val rs = checkPs.executeQuery()
                        if (!rs.next()) {
                            return true
                        }
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
            return false
        }

        @JvmStatic
        fun updateGuildName(guildId: Int, guildName: String) {
            try {
                getConnection().use { con ->
                    Update("guilds").set("name", guildName).where("guildid", guildId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun deleteGuild(guildId: Int) {
            try {
                getConnection().use { con ->
                    Delete.from("guilds").where("guildid", guildId).execute(con!!)
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun updateAllianceId(guildId: Int, allianceId: Int) {
            try {
                getConnection().use { con ->
                    Update("guilds").set("allianceid", allianceId).where("guildid", guildId).execute(
                        con!!
                    )
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

    }
}