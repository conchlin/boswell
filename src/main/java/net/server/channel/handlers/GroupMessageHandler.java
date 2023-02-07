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
import client.autoban.AutobanFactory;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import net.server.world.World;
import network.packet.field.CField;
import tools.FilePrinter;
import tools.LogHelper;
import tools.data.input.SeekableLittleEndianAccessor;

public final class GroupMessageHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter player = c.getPlayer();
        if(player.getAutobanManager().getLastSpam(7) + 200 > currentServerTime()) {
                return;
        }
        
        int type = slea.readByte(); // 0 for buddys, 1 for partys
        int numRecipients = slea.readByte();
        int[] recipients = new int[numRecipients];
        for (int i = 0; i < numRecipients; i++) {
            recipients[i] = slea.readInt();
        }
        String chattext = slea.readMapleAsciiString();
        if (chattext.length() > Byte.MAX_VALUE && !player.isGM()) {
        	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit chats.");
        	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to send text with length of " + chattext.length());
        	c.disconnect(true, false);
        	return;
        }	
        World world = c.getWorldServer();
        if (type == 0) {
            world.buddyChat(recipients, player.getId(), player.getName(), chattext);
            if (ServerConstants.USE_ENABLE_CHAT_LOG) {
                LogHelper.logChat(c, "Buddy", chattext);
            }
        } else if (type == 1 && player.getParty() != null) {
            world.partyChat(player.getParty(), chattext, player.getName());
            if (ServerConstants.USE_ENABLE_CHAT_LOG) {
                LogHelper.logChat(c, "Party", chattext);
            }
        } else if (type == 2 && player.getGuildId() > 0) {
            Server.getInstance().guildChat(player.getGuildId(), player.getName(), player.getId(), chattext);
            if (ServerConstants.USE_ENABLE_CHAT_LOG) {
                LogHelper.logChat(c, "Guild", chattext);
            }
        } else if (type == 3 && player.getGuild() != null) {
            int allianceId = player.getGuild().getAllianceId();
            if (allianceId > 0) {
                Server.getInstance().allianceMessage(allianceId, CField.Packet.onGroupMessage(player.getName(), chattext, 3), player.getId(), -1);
                if (ServerConstants.USE_ENABLE_CHAT_LOG) {
                    LogHelper.logChat(c, "Ally", chattext);
                }
            }
        }
        player.getAutobanManager().spam(7);
    }
}
