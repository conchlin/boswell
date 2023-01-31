/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation version 3 as published by
 the Free Software Foundation. You may not use, modify or distribute
 this program under any other version of the GNU Affero General Public
 License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.server.channel.handlers;

import client.MapleCharacter;
import client.MapleClient;
import enums.AllianceResultType;
import net.AbstractMaplePacketHandler;
import network.opcode.SendOpcode;
import net.server.Server;
import net.server.guild.MapleGuild;
import net.server.guild.MapleGuildCharacter;
import net.server.guild.MapleAlliance;
import network.packet.wvscontext.WvsContext;
import network.packet.wvscontext.AlliancePacket;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author XoticStory, Ronan
 */
public final class AllianceOperationHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleAlliance alliance = null;
        MapleCharacter chr = c.getPlayer();
        
        if (chr.getGuild() == null) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        if (chr.getGuild().getAllianceId() > 0) {
            alliance = chr.getAlliance();
        }
        
        byte b = slea.readByte();
        if (alliance == null) {
            if (b != 4) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
        } else {
            if (b == 4) {
                chr.dropMessage(5, "Your guild is already registered on a guild alliance.");
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
            
            if (chr.getMGC().getAllianceRank() > 2 || !alliance.getGuilds().contains(chr.getGuildId())) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
        }
        
        // "alliance" is only null at case 0x04
        switch (b) {
            case 0x01 -> Server.getInstance().allianceMessage(alliance.getId(), sendShowInfo(chr.getGuild().getAllianceId(), chr.getId()), -1, -1);
            case 0x02 -> { // Leave Alliance
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuildId() < 1 || chr.getGuildRank() != 1) {
                    return;
                }

                MapleAlliance.removeGuildFromAlliance(chr.getGuild().getAllianceId(), chr.getGuildId(), chr.getWorld());
            }
            case 0x03 -> { // Send Invite
                String guildName = slea.readMapleAsciiString();
                if (alliance.getGuilds().size() == alliance.getCapacity()) {
                    chr.dropMessage(5, "Your alliance cannot comport any more guilds at the moment.");
                } else {
                    MapleAlliance.sendInvitation(c, guildName, alliance.getId());
                }
            }
            case 0x04 -> { // Accept Invite
                MapleGuild guild = chr.getGuild();
                if (guild.getAllianceId() != 0 || chr.getGuildRank() != 1 || chr.getGuildId() < 1) {
                    return;
                }

                int allianceid = slea.readInt();
                //slea.readMapleAsciiString();  //recruiter's guild name

                alliance = Server.getInstance().getAlliance(allianceid);
                if (alliance == null) {
                    return;
                }

                if (!MapleAlliance.answerInvitation(c.getPlayer().getId(), guild.getName(), alliance.getId(), true)) {
                    return;
                }

                if (alliance.getGuilds().size() == alliance.getCapacity()) {
                    chr.dropMessage(5, "Your alliance cannot comport any more guilds at the moment.");
                    return;
                }

                int guildid = chr.getGuildId();

                Server.getInstance().addGuildtoAlliance(alliance.getId(), guildid);
                Server.getInstance().resetAllianceGuildPlayersRank(guildid);

                chr.getMGC().setAllianceRank(2);
                Server.getInstance().getGuild(chr.getGuildId()).getMGC(chr.getId()).setAllianceRank(2);
                chr.saveGuildStatus();

                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, c, AllianceResultType.AddGuild.getResult(), guildid), -1, -1);
                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.UpdateInfo.getResult(), c.getWorld()), -1, -1);
                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.Notice.getResult()), -1, -1);
                guild.dropMessage("Your guild has joined the [" + alliance.getName() + "] union.");
            }
            case 0x06 -> { // Expel Guild
                int guildid = slea.readInt();
                int allianceid = slea.readInt();
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuild().getAllianceId() != allianceid) {
                    return;
                }

                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.RemoveGuild.getResult(), guildid, c.getWorld()), -1, -1);
                Server.getInstance().removeGuildFromAlliance(alliance.getId(), guildid);

                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.GuildInfo.getResult(), c.getWorld()), -1, -1);
                Server.getInstance().allianceMessage(allianceid,
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.Notice.getResult(), alliance.getNotice()), -1, -1);
                Server.getInstance().guildMessage(guildid,
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.Disband.getResult()));

                alliance.dropMessage("[" + Server.getInstance().getGuild(guildid).getName() + "] guild has been expelled from the union.");
            }
            case 0x07 -> { // Change Alliance Leader
                if (chr.getGuild().getAllianceId() == 0 || chr.getGuildId() < 1) {
                    return;
                }
                int victimid = slea.readInt();
                MapleCharacter player = Server.getInstance().getWorld(c.getWorld()).getPlayerStorage().getCharacterById(victimid);
                if (player.getAllianceRank() != 2) {
                    return;
                }

                //Server.getInstance().allianceMessage(alliance.getId(), sendChangeLeader(chr.getGuild().getAllianceId(), chr.getId(), slea.readInt()), -1, -1);
                changeLeaderAllianceRank(alliance, player);
            }
            case 0x08 -> {
                String[] ranks = new String[5];
                for (int i = 0; i < 5; i++) {
                    ranks[i] = slea.readMapleAsciiString();
                }
                Server.getInstance().setAllianceRanks(alliance.getId(), ranks);
                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.AllianceRank.getResult(), ranks), -1, -1);
            }
            case 0x09 -> {
                int int1 = slea.readInt();
                byte byte1 = slea.readByte();

                //Server.getInstance().allianceMessage(alliance.getId(), sendChangeRank(chr.getGuild().getAllianceId(), chr.getId(), int1, byte1), -1, -1);
                MapleCharacter player = Server.getInstance().getWorld(c.getWorld()).getPlayerStorage().getCharacterById(int1);
                changePlayerAllianceRank(alliance, player, (byte1 > 0));
            }
            case 0x0A -> {
                String notice = slea.readMapleAsciiString();
                Server.getInstance().setAllianceNotice(alliance.getId(), notice);
                Server.getInstance().allianceMessage(alliance.getId(),
                        AlliancePacket.Packet.onAllianceResult(alliance, AllianceResultType.Notice.getResult(), notice), -1, -1);
                alliance.dropMessage(5, "* Alliance Notice : " + notice);
            }
            default -> chr.dropMessage("Feature not available");
        }
        
        alliance.saveToDB();
    }
    
    private void changeLeaderAllianceRank(MapleAlliance alliance, MapleCharacter newLeader) {
        MapleGuildCharacter lmgc = alliance.getLeader();
        MapleCharacter leader = newLeader.getWorldServer().getPlayerStorage().getCharacterById(lmgc.getId());
        leader.getMGC().setAllianceRank(2);
        leader.saveGuildStatus();
        
        newLeader.getMGC().setAllianceRank(1);
        newLeader.saveGuildStatus();

        Server.getInstance().allianceMessage(alliance.getId(), AlliancePacket.Packet.onAllianceResult(alliance,
                AllianceResultType.GuildInfo.getResult(), newLeader.getWorld()), -1, -1);
        alliance.dropMessage("'" + newLeader.getName() + "' has been appointed as the new head of this Alliance.");
    }
    
    private void changePlayerAllianceRank(MapleAlliance alliance, MapleCharacter chr, boolean raise) {
        int newRank = chr.getAllianceRank() + (raise ? -1 : 1);
        if(newRank < 3 || newRank > 5) return;
        
        chr.getMGC().setAllianceRank(newRank);
        chr.saveGuildStatus();

        Server.getInstance().allianceMessage(alliance.getId(), AlliancePacket.Packet.onAllianceResult(alliance,
                AllianceResultType.GuildInfo.getResult(), chr.getWorld()), -1, -1);
        alliance.dropMessage("'" + chr.getName() + "' has been reassigned to '" + alliance.getRankTitle(newRank) + "' in this Alliance.");
    }

    private static byte[] sendShowInfo(int allianceid, int playerid) {
        MaplePacketLittleEndianWriter mplew = new MaplePacketLittleEndianWriter();
        mplew.writeShort(SendOpcode.AllianceResult.getValue());
        mplew.write(0x02);
        mplew.writeInt(allianceid);
        mplew.writeInt(playerid);
        return mplew.getPacket();
    }
}
