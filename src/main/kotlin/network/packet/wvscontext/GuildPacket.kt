package network.packet.wvscontext

import client.MapleCharacter
import enums.GuildResultType
import net.server.guild.MapleGuildCharacter
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil
import java.sql.ResultSet
import java.sql.SQLException

class GuildPacket {

    companion object Packet {

        /**
         * Handles guild error messages and various related responses
         *
         * @param code see GuildResultType enum
         * @param args if you need to specify target users
         */
        fun onGuildMessage(code: Int, vararg args: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(code)
            when (code) {
                GuildResultType.NewGuild.result -> {
                    mplew.writeInt(0)
                    mplew.writeMapleAsciiString(args[0]) // leader name
                    mplew.writeMapleAsciiString(args[1]) // message
                }
                GuildResultType.NotAcceptingInvites.result,
                GuildResultType.AlreadyInvited.result,
                GuildResultType.DeniedInvite.result -> {
                    mplew.writeMapleAsciiString(args[0]) // target user
                }
            }

            return mplew.packet
        }

        /**
         * changes that happen to the MapleGuildCharacter
         *
         * @param mgc character being affected
         * @param result action being performed
         */
        fun onGuildResult(mgc: MapleGuildCharacter, result: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(result)
            when (result) {
                GuildResultType.JoinGuild.result -> {
                    mplew.writeInt(mgc.guildId)
                    mplew.writeInt(mgc.id)
                    mplew.writeAsciiString(
                        PacketUtil.getRightPaddedStr(mgc.name, '\u0000', 13)
                    )
                    mplew.writeInt(mgc.jobId)
                    mplew.writeInt(mgc.level)
                    mplew.writeInt(mgc.guildRank)
                    mplew.writeInt(if (mgc.isOnline) 1 else 0)
                    mplew.writeInt(1)
                    mplew.writeInt(3)
                }
                GuildResultType.LeaveGuild.result,
                GuildResultType.Expelled.result -> {
                    mplew.writeInt(mgc.guildId)
                    mplew.writeInt(mgc.id)
                    mplew.writeMapleAsciiString(mgc.name)
                }
                GuildResultType.RankChange.result -> {
                    mplew.writeInt(mgc.guildId)
                    mplew.writeInt(mgc.id)
                    mplew.write(mgc.guildRank)
                }
                GuildResultType.LevelJobChange.result -> {
                    mplew.writeInt(mgc.guildId)
                    mplew.writeInt(mgc.id)
                    mplew.writeInt(mgc.level)
                    mplew.writeInt(mgc.jobId)
                }
            }

            return mplew.packet
        }

        /**
         * Changes that happen to the guild itself
         *
         * @param guildId guild that is being affected
         * @param result see GuildResultType for decimal values
         */
        fun onGuildResult(guildId: Int, result: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            when (result) {
                GuildResultType.Disband.result,
                GuildResultType.IncreaseCapacity.result -> {
                    mplew.writeInt(guildId)
                    mplew.write(args[0]) // new guild size
                }
                GuildResultType.MemberLogin.result -> {
                    mplew.writeInt(guildId)
                    mplew.writeInt(args[0]) // char id
                    mplew.write(args[1]) // login status
                }
                GuildResultType.EmblemChange.result -> {
                    mplew.writeInt(guildId)
                    mplew.writeShort(args[0]) // background
                    mplew.write(args[1]) // background color
                    mplew.writeShort(args[2]) // logo
                    mplew.write(args[3]) // logo color
                }
                GuildResultType.GuildPoint.result -> {
                    mplew.writeInt(guildId)
                    mplew.writeInt(args[0])
                }
            }

            return mplew.packet
        }

        /**
         * Changes that are made to the guild that need reference to a player string
         *
         * @param guildId guild being affected
         * @param result use GuildResultType
         */
        fun onGuildResult(guildId: Int, result: Int, vararg args: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(result)
            when (result) {
                GuildResultType.InviteGuild.result -> {
                    mplew.writeInt(guildId)
                    mplew.writeMapleAsciiString(args[0]) // player being invited
                }
                GuildResultType.Notice.result -> {
                    mplew.writeInt(guildId)
                    mplew.writeMapleAsciiString(args[0]) // notice
                }
            }

            return mplew.packet
        }

        /**
         * only used when changing the name of guild ranks
         *
         * @param guildId
         * @param ranks writes the 5 new guild ranks
         */
        fun onGuildResult(guildId: Int, ranks: Array<String?>): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(GuildResultType.GuildRank.result)
            mplew.writeInt(guildId)
            for (i in 0..4) {
                mplew.writeMapleAsciiString(ranks[i])
            }
            return mplew.packet
        }

        @Throws(SQLException::class)
        fun showGuildRanks(npcId: Int, rs: ResultSet): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(GuildResultType.ShowRank.result)
            mplew.writeInt(npcId)
            if (!rs.last()) { //no guilds o.o
                mplew.writeInt(0)
                return mplew.packet
            }
            mplew.writeInt(rs.row) //number of entries
            rs.beforeFirst()
            while (rs.next()) {
                mplew.writeMapleAsciiString(rs.getString("name"))
                mplew.writeInt(rs.getInt("GP"))
                mplew.writeInt(rs.getInt("logo"))
                mplew.writeInt(rs.getInt("logoColor"))
                mplew.writeInt(rs.getInt("logoBG"))
                mplew.writeInt(rs.getInt("logoBGColor"))
            }
            return mplew.packet
        }

        fun showGuildInfo(c: MapleCharacter?): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GuildResult.value)
            mplew.write(GuildResultType.GuildInfo.result)
            if (c == null) { //show empty guild (used for leaving, expelled)
                mplew.write(0)
                return mplew.packet
            }
            val g = c.client.worldServer.getGuild(c.mgc)
            if (g == null) { //failed to read from DB - don't show a guild
                mplew.write(0)
                return mplew.packet
            }
            mplew.write(1) //bInGuild
            mplew.writeInt(g.id)
            mplew.writeMapleAsciiString(g.name)
            for (i in 1..5) {
                mplew.writeMapleAsciiString(g.getRankTitle(i))
            }
            val members: Collection<MapleGuildCharacter> = g.members
            mplew.write(members.size) //then it is the size of all the members
            for (mgc in members) { //and each of their character ids o_O
                mplew.writeInt(mgc.id)
            }
            for (mgc in members) {
                mplew.writeAsciiString(PacketUtil.getRightPaddedStr(mgc.name, '\u0000', 13))
                mplew.writeInt(mgc.jobId)
                mplew.writeInt(mgc.level)
                mplew.writeInt(mgc.guildRank)
                mplew.writeInt(if (mgc.isOnline) 1 else 0)
                mplew.writeInt(g.signature)
                mplew.writeInt(mgc.allianceRank)
            }
            mplew.writeInt(g.capacity)
            mplew.writeShort(g.logoBG)
            mplew.write(g.logoBGColor)
            mplew.writeShort(g.logo)
            mplew.write(g.logoColor)
            mplew.writeMapleAsciiString(g.notice)
            mplew.writeInt(g.gp)
            mplew.writeInt(g.allianceId)
            return mplew.packet
        }

        /**
         * loading and showing threads within the BBS guild system
         */
        fun onGuildBBSPacket(thread: Int, result: Int, vararg args: ResultSet): ByteArray {
            var t = thread // mutable
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.GUILD_BBS_PACKET.value)
            mplew.write(result)
            when (result) {
                GuildResultType.LoadBBS.result -> {
                    if (!args[0].last()) {
                        mplew.write(0)
                        mplew.writeInt(0)
                        mplew.writeInt(0)
                        return mplew.packet
                    }
                    var threadCount = args[0].row
                    if (args[0].getInt("localthreadid") == 0) { //has a notice
                        mplew.write(1)
                        addThread(mplew, args[0])
                        threadCount-- //one thread didn't count (because it's a notice)
                    } else {
                        mplew.write(0)
                    }
                    if (!args[0].absolute(t + 1)) { //seek to the thread before where we start
                        args[0].first() //uh, we're trying to start at a place past possible
                        t = 0
                    }
                    mplew.writeInt(threadCount)
                    mplew.writeInt(10.coerceAtMost(threadCount - t))
                    for (i in 0 until 10.coerceAtMost(threadCount - t)) {
                        addThread(mplew, args[0])
                        args[0].next()
                    }
                }
                GuildResultType.ShowBBS.result -> {
                    mplew.writeInt(t)
                    mplew.writeInt(args[0].getInt("postercid"))
                    mplew.writeLong(PacketUtil.getTime(args[0].getLong("timestamp")))
                    mplew.writeMapleAsciiString(args[0].getString("name"))
                    mplew.writeMapleAsciiString(args[0].getString("startpost"))
                    mplew.writeInt(args[0].getInt("icon"))
                    if (args[1] != null) {
                        val replyCount: Int = args[0].getInt("replycount")
                        mplew.writeInt(replyCount)
                        var i: Int
                        i = 0
                        while (i < replyCount && args[1].next()) {
                            mplew.writeInt(args[1].getInt("replyid"))
                            mplew.writeInt(args[1].getInt("postercid"))
                            mplew.writeLong(PacketUtil.getTime(args[1].getLong("timestamp")))
                            mplew.writeMapleAsciiString(args[1].getString("content"))
                            i++
                        }
                        if (i != replyCount || args[1].next()) {
                            throw RuntimeException(args[0].getInt("threadid").toString())
                        }
                    } else {
                        mplew.writeInt(0)
                    }
                }
            }

            return mplew.packet
        }

        @Throws(SQLException::class)
        private fun addThread(mplew: MaplePacketLittleEndianWriter, rs: ResultSet) {
            mplew.writeInt(rs.getInt("localthreadid"))
            mplew.writeInt(rs.getInt("postercid"))
            mplew.writeMapleAsciiString(rs.getString("name"))
            mplew.writeLong(PacketUtil.getTime(rs.getLong("timestamp")))
            mplew.writeInt(rs.getInt("icon"))
            mplew.writeInt(rs.getInt("replycount"))
        }
    }
}