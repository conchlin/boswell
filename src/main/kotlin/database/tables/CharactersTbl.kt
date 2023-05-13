package database.tables

import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.Update
import java.sql.SQLException

class CharactersTbl {

    companion object {

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
    }
}