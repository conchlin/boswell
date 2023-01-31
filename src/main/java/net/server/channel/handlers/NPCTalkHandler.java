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
import network.packet.wvscontext.WvsContext;
import scripting.npc.NPCScriptManager;
import server.life.MapleNPC;
import server.maps.MapleMapObject;
import server.life.MaplePlayerNPC;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

public final class NPCTalkHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        if (!c.getPlayer().isAlive()) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        if(currentServerTime() - c.getPlayer().getNpcCooldown() < ServerConstants.BLOCK_NPC_RACE_CONDT) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        
        int oid = slea.readInt();
        MapleMapObject obj = c.getPlayer().getMap().getMapObject(oid);
        if (obj instanceof MapleNPC npc) {
            if (npc.getId() == 9010009) {   //is duey
                DueyProcessor.dueySendTalk(c, false);
            } else {
                if (c.getCM() != null || c.getQM() != null) {
                    c.announce(WvsContext.Packet.enableActions());
                    return;
                }
                if(npc.getId() >= 9100100 && npc.getId() <= 9100200) {
                    // Custom handling for gachapon scripts to reduce the amount of scripts needed.
                    NPCScriptManager.getInstance().start(c, npc.getId(), "gachapon", null);
                } else {
                    boolean hasNpcScript = NPCScriptManager.getInstance().start(c, npc.getId(), oid, null);
                    if (!hasNpcScript) {
                        if (!npc.hasShop()) {
                            FilePrinter.printError(FilePrinter.NPC_UNCODED, "NPC " + npc.getName() + "(" + npc.getId() + ") is not coded.");
                            return;
                        } else if(c.getPlayer().getShop() != null) {
                            c.announce(WvsContext.Packet.enableActions());
                            return;
                        }
                        
                        npc.sendShop(c);
                    }
                }
            }
        } else if (obj instanceof MaplePlayerNPC pnpc) {
            NPCScriptManager nsm = NPCScriptManager.getInstance();
            
            if (pnpc.getScriptId() < 9977777 && !nsm.isNpcScriptAvailable(c, "" + pnpc.getScriptId())) {
                nsm.start(c, pnpc.getScriptId(), "rank_user", null);
            } else {
                nsm.start(c, pnpc.getScriptId(), null);
            }
        }
    }
}