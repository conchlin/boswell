package database.tables

import client.MapleClient
import database.DatabaseConnection.Companion.getConnection
import database.DatabaseStatements.*
import database.DatabaseStatements.Delete.Companion.from
import enums.GuildResultType
import net.server.Server
import net.server.channel.handlers.GuildBBSOperationHandler
import network.packet.context.GuildPacket.Packet.onGuildBBSPacket
import java.sql.ResultSet
import java.sql.SQLException

class GuildsTbl {

    /**
     * this contains database logic methods for tables -> guilds, bbs_thread, bbs_replies
     */

    companion object {

        /**
         * @return the new guild ID or 0 for an error
         */
        @JvmStatic
        fun createGuild(leaderId: Int, guildName: String): Int {
            try {
                getConnection().use { con ->
                    return Insert.into("guilds")
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
                    con!!.prepareStatement("UPDATE characters SET guildid = 0, guildrank = 5 WHERE guildid = ?")
                        .use { ps ->
                            ps.setInt(1, guildId)
                            ps.execute()
                        }
                    Delete.from("guilds").where("guildid", guildId).execute(con)
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

        @JvmStatic
        fun createBBSReply(threadId: Int, text: String, client: MapleClient) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT threadid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")
                        .use { ps ->
                            ps.setInt(1, client.player.guildId)
                            ps.setInt(2, threadId)
                            val threadRS = ps.executeQuery()
                            if (!threadRS.next()) {
                                return
                            }
                            val threadid = threadRS.getInt("threadid")
                            Insert.into("bbs_replies")
                                .add("threadid", threadid)
                                .add("postercid", client.player.id)
                                .add("timestamp", Server.getInstance().currentTime)
                                .add("content", text)
                                .execute(con)
                            con.prepareStatement("UPDATE bbs_threads SET replycount = replycount + 1 WHERE threadid = ?")
                                .use { psb ->
                                    psb.setInt(1, threadid)
                                    psb.execute()
                                    GuildBBSOperationHandler.displayThread(client, threadId)
                                }
                        }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun loadBBSThreads(start: Int, client: MapleClient) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement(
                        "SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC",
                        ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY
                    ).use { ps ->
                        ps.setInt(1, client.player.guildId)
                        ps.executeQuery().use { rs ->
                            client.announce(
                                onGuildBBSPacket(
                                    start,
                                    GuildResultType.LoadBBS.result,
                                    rs
                                )
                            )
                        }
                    }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun updateBBSThread(title: String, icon: Int, text: String, threadId: Int, client: MapleClient) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("UPDATE bbs_threads SET name = ?, timestamp = ?, icon = ?, startpost = ? WHERE guildid = ? AND localthreadid = ? AND (postercid = ? OR ?)")
                        .use { ps ->
                            ps.setString(1, title)
                            ps.setLong(2, Server.getInstance().currentTime)
                            ps.setInt(3, icon)
                            ps.setString(4, text)
                            ps.setInt(5, client.player.guildId)
                            ps.setInt(6, threadId)
                            ps.setInt(7, client.player.id)
                            ps.setBoolean(8, client.player.guildRank < 3)
                            ps.execute()
                            GuildBBSOperationHandler.displayThread(client, threadId)
                        }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun createBBSThread(title: String, icon: Int, text: String, bNotice: Boolean, client: MapleClient) {
            var nextId = 0
            try {
                getConnection().use { con ->
                    if (!bNotice) {
                        val ps = con!!.prepareStatement("SELECT MAX(localthreadid) AS lastLocalId FROM bbs_threads WHERE guildid = ?")
                        ps.setInt(1, client.player.guildId)
                        ps.executeQuery().use { rs ->
                            rs.next()
                            nextId = rs.getInt("lastLocalId") + 1
                        }
                    }
                    Insert.into("bbs_threads")
                        .add("postercid", client.player.id)
                        .add("name", title)
                        .add("timestamp", Server.getInstance().currentTime)
                        .add("icon", icon)
                        .add("startpost", text)
                        .add("guildid", client.player.guildId)
                        .add("localthreadid", nextId)
                        .execute(con!!)
                    GuildBBSOperationHandler.displayThread(client, nextId)
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun deleteBBSThread(threadId: Int, client: MapleClient) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT threadid, postercid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")
                        .use { ps ->
                            ps.setInt(1, client.player.guildId)
                            ps.setInt(2, threadId)
                            val threadRS = ps.executeQuery()
                            if (!threadRS.next()) {
                                return
                            }
                            if (client.player.id != threadRS.getInt("postercid") && client.player.guildRank > 2) {
                                return
                            }
                            val threadid = threadRS.getInt("threadid")
                            from("bbs_replies").where("threadid", threadid).execute(con)
                            from("bbs_threads").where("threadid", threadid).execute(con)
                        }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun deleteBBSReply(replyId: Int, client: MapleClient) {
            var threadId = 0
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT postercid, threadid FROM bbs_replies WHERE replyid = ?").use { ps ->
                        ps.setInt(1, replyId)
                        ps.executeQuery().use { rs ->
                            if (!rs.next()) {
                                return
                            }
                            if (client.player.id != rs.getInt("postercid") && client.player.guildRank > 2) {
                                return
                            }
                            threadId = rs.getInt("threadid")
                        }
                        from("bbs_replies").where("replyid", replyId).execute(con)
                        con.prepareStatement("UPDATE bbs_threads SET replycount = replycount - 1 WHERE threadid = ?")
                            .use { psb ->
                                psb.setInt(1, threadId)
                                psb.execute()
                                GuildBBSOperationHandler.displayThread(client, threadId, false)
                            }
                    }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            }
        }

        @JvmStatic
        fun loadThread(threadId: Int, bIsThreadIdLocal: Boolean, client: MapleClient) {
            try {
                getConnection().use { con ->
                    con!!.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? AND " + (if (bIsThreadIdLocal) "local" else "") + "threadid = ?")
                        .use { ps ->
                            ps.setInt(1, client.player.guildId)
                            ps.setInt(2, threadId)
                            val threadRS = ps.executeQuery()
                            if (!threadRS.next()) {
                                return
                            }
                            if (threadRS.getInt("replycount") >= 0) {
                                con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?").use { ps2 ->
                                    ps2.setInt(1, if (!bIsThreadIdLocal) threadId else threadRS.getInt("threadid"))
                                    val repliesRS = ps2.executeQuery()
                                    client.announce(
                                        onGuildBBSPacket(
                                            if (bIsThreadIdLocal) threadId else threadRS.getInt("localthreadid"),
                                            GuildResultType.ShowBBS.result,
                                            threadRS,
                                            repliesRS
                                        )
                                    )
                                }
                            }
                        }
                }
            } catch (se: SQLException) {
                se.printStackTrace()
            } catch (re: RuntimeException) { //btw we get this everytime for some reason, but replies work!
                re.printStackTrace()
                println("The number of reply rows does not match the replycount in thread.")
            }
        }
    }
}