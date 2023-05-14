package database.tables

import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.Delete
import java.sql.PreparedStatement
import java.sql.SQLException

class AllianceTbl {

    /**
     * contains methods for both the alliance and alliance_guilds tables
     */

    companion object {

        /**
         * will generate the alliance ID automatically
         *
         * @return the allianceID
         */
        @JvmStatic
        fun createAlliance(allianceName: String): Int {
            var id = -1
            try {
                getConnection().use { con ->
                    con!!.prepareStatement(
                        "INSERT INTO alliance (name) VALUES (?)",
                        PreparedStatement.RETURN_GENERATED_KEYS
                    ).use { ps ->
                        ps.setString(1, allianceName)
                        ps.executeUpdate()
                        ps.generatedKeys.use { rs ->
                            rs.next()
                            id = rs.getInt(1)
                            return id
                        }
                    }
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
                return id
            }
        }

        @JvmStatic
        fun validAllianceName(name: String): Boolean {
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
        fun removeGuildFromAlliance(guildId: Int) {
            try {
                getConnection().use { con ->
                    Delete.from("alliance_guilds").where("guildid", guildId).execute(
                        con!!
                    )
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }
        }

        @JvmStatic
        fun addAllianceGuild(allianceId: Int, guildId: Int) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("INSERT INTO alliance_guilds (allianceid, guildid) VALUES (?, ?)")
                        .use { psg ->
                            psg.setInt(1, allianceId)
                            psg.setInt(2, guildId)
                            psg.executeUpdate()
                        }
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }
        }

        @JvmStatic
        fun deleteAlliance(allianceId: Int) {
            try {
                getConnection().use { con ->
                    Delete.from("alliance").where("id", allianceId).execute(con!!)
                    Delete.from("alliance_guilds").where("allianceid", allianceId).execute(con)
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }
        }

        @JvmStatic
        fun loadAllianceGuilds(allianceId: Int): ArrayList<Int> {
            val guilds = ArrayList<Int>()
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT guildid FROM alliance_guilds WHERE allianceid = ?").use { ps ->
                        ps.setInt(1, allianceId)
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                guilds.add(rs.getInt("guildid"))
                            }
                        }
                    }
                }
            } catch (sqle: SQLException) {
                sqle.printStackTrace()
            }
            return guilds
        }
    }
}