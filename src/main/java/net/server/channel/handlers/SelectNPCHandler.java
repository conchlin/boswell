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
import client.processor.DueyProcessor;
import constants.ServerConstants;
import net.AbstractMaplePacketHandler;
import network.packet.context.WvsContext;
import script.ScriptManager;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.life.MaplePlayerNPC;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SelectNPCHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }

        if (currentServerTime() - c.getPlayer().getNpcCooldown() < ServerConstants.BLOCK_NPC_RACE_CONDT) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        int objectId = slea.readInt();
        MapleMapObject obj = c.getPlayer().getMap().getMapObject(objectId);
        if (obj instanceof MapleNPC npc) {
            if (npc.getId() == 9010009) {   //is duey
                DueyProcessor.dueySendTalk(c, false);
            } else {
                // todo handle shop compatibility
                String script = npc.getScripts().get(npc.getId());

                if (script == null) {
                    // if npc does not have a valid script name we use the npcId instead
                    script = String.valueOf(npc.getId());
                }

                ScriptManager.Companion.startScript(c, npc, script);
            }
        }
        // todo add additional support for player npcs
    }
}