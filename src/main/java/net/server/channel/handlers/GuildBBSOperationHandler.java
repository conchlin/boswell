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

import database.tables.GuildsTbl;
import net.AbstractMaplePacketHandler;
import tools.data.input.SeekableLittleEndianAccessor;

public final class GuildBBSOperationHandler extends AbstractMaplePacketHandler {

    private String correctLength(String in, int maxSize) {
        return in.length() > maxSize ? in.substring(0, maxSize) : in;
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
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
                GuildsTbl.loadBBSThreads(start * 10, c);
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

    private static void newBBSReply(MapleClient c, int localthreadid, String text) {
        if (c.getPlayer().getGuildId() <= 0) {
            return;
        }
        GuildsTbl.createBBSReply(localthreadid, text, c);
    }

    private static void editBBSThread(MapleClient client, String title, String text, int icon, int localthreadid) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() < 1) {
            return;
        }
        GuildsTbl.updateBBSThread(title, icon, text, localthreadid, client);
    }

    private static void newBBSThread(MapleClient client, String title, String text, int icon, boolean bNotice) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() <= 0) {
            return;
        }
        GuildsTbl.createBBSThread(title, icon, text, bNotice, client);
    }

    public static void deleteBBSThread(MapleClient client, int localthreadid) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() <= 0) {
            return;
        }
        GuildsTbl.deleteBBSThread(localthreadid, client);
    }

    public static void deleteBBSReply(MapleClient client, int replyid) {
        MapleCharacter c = client.getPlayer();
        if (c.getGuildId() <= 0) {
            return;
        }
        GuildsTbl.deleteBBSReply(replyid, client);
    }

    public static void displayThread(MapleClient client, int threadid) {
        displayThread(client, threadid, true);
    }

    public static void displayThread(MapleClient client, int threadid, boolean bIsThreadIdLocal) {
        MapleCharacter mc = client.getPlayer();
        if (mc.getGuildId() <= 0) {
            return;
        }
        GuildsTbl.loadThread(threadid, bIsThreadIdLocal, client);
    }
}
