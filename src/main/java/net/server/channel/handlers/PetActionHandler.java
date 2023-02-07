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

import client.MapleClient;
import client.autoban.AutobanFactory;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import network.packet.PetPacket;
import tools.FilePrinter;
import tools.LogHelper;
import tools.data.input.SeekableLittleEndianAccessor;

public final class PetActionHandler extends AbstractMaplePacketHandler {
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readInt();
        slea.readByte();
        int act = slea.readByte();
        byte pet = c.getPlayer().getPetIndex(petId);
        if ((pet < 0 || pet > 3) || (act < 0 || act > 9)) {
        	return;
        }
        String text = slea.readMapleAsciiString();
        if (text.length() > Byte.MAX_VALUE) {
        	AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with pets.");
        	FilePrinter.printError(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to send text with length of " + text.length());
        	c.disconnect(true, false);
        	return;
        }
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(), PetPacket.Packet.onAction(c.getPlayer().getId(), pet, act, text), true);
        if (ServerConstants.USE_ENABLE_CHAT_LOG) {
            LogHelper.logChat(c, "Pet", text);
        }
    } 
}
