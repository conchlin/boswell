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
import net.AbstractMaplePacketHandler;
import server.MaplePortal;
import tools.data.input.SeekableLittleEndianAccessor;

import java.awt.*;

/**
 *
 * @author BubblesDev
 */
public final class PortalTeleportRequestHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        slea.readByte();
        String portalName = slea.readMapleAsciiString();
        Point portalPos = slea.readPos();
        Point targetPos = slea.readPos();
        if(c.getPlayer().getMap().getPortal(portalName) == null){
            AutobanFactory.WZ_EDIT.alert(c.getPlayer(), "Used inner portal: " + portalName + " in "
                    + c.getPlayer().getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
            return;
        }
        boolean foundPortal = false;
        for(MaplePortal portal : c.getPlayer().getMap().getPortals()){
            if(portal.getType() == 1 || portal.getType() == 2 || portal.getType() == 10 || portal.getType() == 20){
                if(portal.getPosition().equals(portalPos) || portal.getPosition().equals(targetPos)) foundPortal = true;
            }
        }
        if(!foundPortal){
            AutobanFactory.WZ_EDIT.alert(c.getPlayer(), "Used inner portal: " + portalName + " in "
                    + c.getPlayer().getMapId() + " targetPos: " + targetPos.toString() + " when it doesn't exist.");
        }
    }
}