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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import enums.GuildResultType;
import net.AbstractMaplePacketHandler;
import database.DatabaseStatements;
import database.DatabaseConnection;
import network.packet.context.GuildPacket;
import tools.data.input.SeekableLittleEndianAccessor;

public final class GuildBBSOperationHandler extends AbstractMaplePacketHandler {

    private String correctLength(String in, int maxSize) {
        return in.length() > maxSize ? in.substring(0, maxSize) : in;
    }

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (c.getPlayer().getGuildId() < 1) {
            return;
        }
        byte mode = slea.readByte();
        int localthreadid = 0;
        switch (mode) {
            case 0:
                boolean bEdit = slea.readByte() == 1;
                if (bEdit) {
                    localthreadid = slea.readInt();
                }
                boolean bNotice = slea.readByte() == 1;
                String title = correctLength(slea.readMapleAsciiString(), 25);
                String text = correctLength(slea.readMapleAsciiString(), 600);
                int icon = slea.readInt();
                if (icon >= 0x64 && icon <= 0x6a) {
                    if (c.getPlayer().haveItemWithId(5290000 + icon - 0x64, false)) {
                        return;
                    }
                } else if (icon < 0 || icon > 3) {
                    return;
                }
                if (!bEdit) {
                    newBBSThread(c, title, text, icon, bNotice);
                } else {
                    editBBSThread(c, title, text, icon, localthreadid);
                }
                break;
            case 1:
                localthreadid = slea.readInt();
                deleteBBSThread(c, localthreadid);
                break;
            case 2:
                int start = slea.readInt();
                listBBSThreads(c, start * 10);
                break;
            case 3: // list thread + reply, followed by id (int)
                localthreadid = slea.readInt();
                displayThread(c, localthreadid);
                break;
            case 4: // reply
                localthreadid = slea.readInt();
                text = correctLength(slea.readMapleAsciiString(), 25);
                newBBSReply(c, localthreadid, text);
                break;
            case 5: // delete reply
                slea.readInt(); // we don't use this
                int replyid = slea.readInt();
                deleteBBSReply(c, replyid);
                break;
            default:
                //System.out.println("Unhandled BBS mode: " + slea.toString());
        }
    }

    private static void listBBSThreads(MapleClient c, int start) {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? ORDER BY localthreadid DESC",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                ps.setInt(1, c.getPlayer().getGuildId());
                try (ResultSet rs = ps.executeQuery()) {
                    c.announce(GuildPacket.Packet.onGuildBBSPacket(start, GuildResultType.LoadBBS.getResult(), rs));
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static void newBBSReply(MapleClient c, int localthreadid, String text) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT threadid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")) {
            ps.setInt(1, c.getPlayer().getGuildId());
            ps.setInt(2, localthreadid);
            ResultSet threadRS = ps.executeQuery();
            if (!threadRS.next()) {
                return;
            }
            int threadid = threadRS.getInt("threadid");
            DatabaseStatements.Insert.into("bbs_replies")
                    .add("threadid", threadid)
                    .add("postercid", c.getPlayer().getId())
                    .add("timestamp", currentServerTime())
                    .add("content", text)
                    .execute(con);
            try (PreparedStatement psb = con.prepareStatement("UPDATE bbs_threads SET replycount = replycount + 1 WHERE threadid = ?")) {
                psb.setInt(1, threadid);
                psb.execute();
                displayThread(c, localthreadid);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static void editBBSThread(MapleClient client, String title, String text, int icon, int localthreadid) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() < 1) {
            return;
        }
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE bbs_threads SET name = ?, timestamp = ?, icon = ?, startpost = ? WHERE guildid = ? AND localthreadid = ? AND (postercid = ? OR ?)")) {
            ps.setString(1, title);
            ps.setLong(2, currentServerTime());
            ps.setInt(3, icon);
            ps.setString(4, text);
            ps.setInt(5, c.getGuildId());
            ps.setInt(6, localthreadid);
            ps.setInt(7, c.getId());
            ps.setBoolean(8, c.getGuildRank() < 3);
            ps.execute();
            displayThread(client, localthreadid);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static void newBBSThread(MapleClient client, String title, String text, int icon, boolean bNotice) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() <= 0) {
            return;
        }
        int nextId = 0;
        try (Connection con = DatabaseConnection.getConnection()) {
            if (!bNotice) {
                PreparedStatement ps = con.prepareStatement("SELECT MAX(localthreadid) AS lastLocalId FROM bbs_threads WHERE guildid = ?");
                ps.setInt(1, c.getGuildId());
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    nextId = rs.getInt("lastLocalId") + 1;
                }
            }
            DatabaseStatements.Insert.into("bbs_threads")
                .add("postercid", c.getId())
                .add("name", title)
                .add("timestamp", currentServerTime())
                .add("icon", icon)
                .add("startpost", text)
                .add("guildid", c.getGuild().getId())
                .add("localthreadid", nextId)
                .execute(con);
            displayThread(client, nextId);
        } catch (SQLException se) {
            se.printStackTrace();
        }

    }

    public static void deleteBBSThread(MapleClient client, int localthreadid) {
        MapleCharacter mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT threadid, postercid FROM bbs_threads WHERE guildid = ? AND localthreadid = ?")) {
            ps.setInt(1, mc.getGuildId());
            ps.setInt(2, localthreadid);
            ResultSet threadRS = ps.executeQuery();
            if (!threadRS.next()) {
                return;
            }
            if (mc.getId() != threadRS.getInt("postercid") && mc.getGuildRank() > 2) {
                return;
            }
            int threadid = threadRS.getInt("threadid");

            DatabaseStatements.Delete.from("bbs_replies").where("threadid", threadid).execute(con);
            DatabaseStatements.Delete.from("bbs_threads").where("threadid", threadid).execute(con);
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void deleteBBSReply(MapleClient client, int replyid) {
        MapleCharacter mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }
        int threadid;
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT postercid, threadid FROM bbs_replies WHERE replyid = ?")) {
            ps.setInt(1, replyid);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                if (mc.getId() != rs.getInt("postercid") && mc.getGuildRank() > 2) {
                    return;
                }
                threadid = rs.getInt("threadid");
            }
            DatabaseStatements.Delete.from("bbs_replies").where("replyid", replyid).execute(con);
            try (PreparedStatement psb = con.prepareStatement("UPDATE bbs_threads SET replycount = replycount - 1 WHERE threadid = ?")) {
                psb.setInt(1, threadid);
                psb.execute();
                displayThread(client, threadid, false);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    public static void displayThread(MapleClient client, int threadid) {
        displayThread(client, threadid, true);
    }

    public static void displayThread(MapleClient client, int threadid, boolean bIsThreadIdLocal) {
        MapleCharacter mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM bbs_threads WHERE guildid = ? AND " + (bIsThreadIdLocal ? "local" : "") + "threadid = ?")) {
                ps.setInt(1, mc.getGuildId());
                ps.setInt(2, threadid);
                ResultSet threadRS = ps.executeQuery();
                if (!threadRS.next()) {
                    return;
                }
                if (threadRS.getInt("replycount") >= 0) {
                    try (PreparedStatement ps2 = con.prepareStatement("SELECT * FROM bbs_replies WHERE threadid = ?")) {
                        ps2.setInt(1, !bIsThreadIdLocal ? threadid : threadRS.getInt("threadid"));
                        ResultSet repliesRS = ps2.executeQuery();
                        client.announce(
                                GuildPacket.Packet.onGuildBBSPacket(bIsThreadIdLocal ? threadid : threadRS.getInt("localthreadid"), GuildResultType.ShowBBS.getResult(), threadRS, repliesRS));
                    }
                }
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (RuntimeException re) {//btw we get this everytime for some reason, but replies work!
            re.printStackTrace();
            System.out.println("The number of reply rows does not match the replycount in thread.");
        }
    }
}
