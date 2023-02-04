package network.packet.context

import client.MapleCharacter
import client.MapleClient
import enums.AllianceResultType
import net.server.Server
import net.server.guild.MapleAlliance
import network.opcode.SendOpcode
import tools.data.output.MaplePacketLittleEndianWriter
import tools.packets.PacketUtil

class AlliancePacket {

    companion object Packet {

        /**
         * onAllianceResult
         *
         * @param alliance alliance to target
         * @param result see AllianceResultType
         * @param args either worldId or guildId depending on result type
         */
        fun onAllianceResult(alliance: MapleAlliance, result: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AllianceResult.value)
            mplew.write(result)
            when (result) {
                AllianceResultType.ShowInfo.result -> {
                    mplew.write(1)
                    mplew.writeInt(alliance.id)
                    mplew.writeMapleAsciiString(alliance.name)
                    for (i in 1..5) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i))
                    }
                    mplew.write(alliance.guilds.size)
                    mplew.writeInt(alliance.capacity)

                    for (guild in alliance.guilds) {
                        mplew.writeInt(guild!!)
                    }
                    mplew.writeMapleAsciiString(alliance.notice)
                }
                AllianceResultType.GuildInfo.result -> {
                    mplew.writeInt(alliance.guilds.size)
                    for (guild in alliance.guilds) {
                        PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(guild!!, args[0])) // world id
                    }
                }
                AllianceResultType.UpdateInfo.result -> {
                    mplew.writeInt(alliance.id)
                    mplew.writeMapleAsciiString(alliance.name)
                    for (i in 1..5) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i))
                    }
                    mplew.write(alliance.guilds.size)
                    for (guild in alliance.guilds) {
                        mplew.writeInt(guild!!)
                    }
                    mplew.writeInt(alliance.capacity)

                    mplew.writeShort(0)
                    for (guildid in alliance.guilds) {
                        PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(guildid!!, args[0])) // world id
                    }
                }
                AllianceResultType.RemoveGuild.result -> {
                    mplew.writeInt(alliance.id)
                    mplew.writeMapleAsciiString(alliance.name)
                    for (i in 1..5) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i))
                    }
                    mplew.write(alliance.guilds.size)
                    for (guild in alliance.guilds) {
                        mplew.writeInt(guild!!)
                    }
                    mplew.writeInt(alliance.capacity)
                    mplew.writeMapleAsciiString(alliance.notice)
                    mplew.writeInt(args[0]) // expelled guild id
                    PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(args[0], args[1], null)) // expelled guild, world id
                    mplew.write(1)
                }
                AllianceResultType.Disband.result -> {
                    mplew.writeInt(alliance.id)
                }
            }
            return mplew.packet
        }

        /**
         * onAllianceResult for when a string is needed for the packet structure
         * only used for alliance notices afaik
         *
         * @param alliance alliance to target
         * @param result see AllianceResultType
         * @param notice string message
         */
        fun onAllianceResult(alliance: MapleAlliance, result: Int, notice: String): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AllianceResult.value)
            mplew.write(result)
            when (result) {
                AllianceResultType.Notice.result -> {
                    mplew.writeInt(alliance.id)
                    mplew.writeMapleAsciiString(notice)
                }
            }
            return mplew.packet
        }

        /**
         * onAllianceResult Array<String>
         * only used for alliance ranks afaik
         *
         * @param alliance alliance to target
         * @param result see AllianceResultType
         * @param ranks array of all alliance ranks
         */
        fun onAllianceResult(alliance: MapleAlliance, result: Int, ranks: Array<String?>):ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AllianceResult.value)
            mplew.write(result)
            when (result) {
                AllianceResultType.AllianceRank.result -> {
                    mplew.writeInt(alliance.id)
                    for (i in 0..4) {
                        mplew.writeMapleAsciiString(ranks[i])
                    }
                }
            }
            return mplew.packet
        }

        /**
         * onAllianceResult where a MapleCharacter reference is needed
         *
         * @param user MapleCharacter
         * @param result see AllianceResultType
         * @param args description of value purpose should be commented
         */
        fun onAllianceResult(user: MapleCharacter, result: Int, vararg args: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AllianceResult.value)
            mplew.write(result)
            when (result) {
                AllianceResultType.LogInOut.result -> {
                    mplew.writeInt(user.guild.allianceId)
                    mplew.writeInt(user.guildId)
                    mplew.writeInt(user.id)
                    mplew.write(args[0]) // login "bool"
                }
                AllianceResultType.JobLevelUpdate.result -> {
                    mplew.writeInt(user.guild.allianceId)
                    mplew.writeInt(user.guildId)
                    mplew.writeInt(user.id)
                    mplew.writeInt(user.level)
                    mplew.writeInt(user.job.id)
                }
                AllianceResultType.Invite.result -> {
                    mplew.writeInt(args[0]) // alliance id
                    mplew.writeMapleAsciiString(user.name)
                    mplew.writeShort(0)
                }
            }
            return mplew.packet
        }

        /**
         * onAllianceResult for when an Alliance and Client reference is needed
         * only used for adding guilds to an alliance for now
         *
         * @param alliance MapleAlliance
         * @param client MapleClient
         * @param result see AllianceResultType
         * @param guildId id of the guild being added to the alliance
         */
        fun onAllianceResult(alliance: MapleAlliance, client: MapleClient, result: Int, guildId: Int): ByteArray? {
            val mplew = MaplePacketLittleEndianWriter()
            mplew.writeShort(SendOpcode.AllianceResult.value)
            mplew.write(result)
            when (result) {
                AllianceResultType.AddGuild.result -> {
                    mplew.writeInt(alliance.id)
                    mplew.writeMapleAsciiString(alliance.name)
                    for (i in 1..5) {
                        mplew.writeMapleAsciiString(alliance.getRankTitle(i))
                    }
                    mplew.write(alliance.guilds.size)
                    for (guild in alliance.guilds) {
                        mplew.writeInt(guild!!)
                    }
                    mplew.writeInt(alliance.capacity)
                    mplew.writeMapleAsciiString(alliance.notice)
                    mplew.writeInt(guildId)
                    PacketUtil.getGuildInfo(mplew, Server.getInstance().getGuild(guildId, client.world, null))
                }
            }
            return mplew.packet
        }
    }
}