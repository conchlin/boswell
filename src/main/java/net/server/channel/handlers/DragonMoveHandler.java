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
import java.awt.Point;
import java.util.List;

import network.packet.DragonPacket;
import server.maps.MapleDragon;
import server.movement.LifeMovementFragment;
import tools.data.input.SeekableLittleEndianAccessor;


public class DragonMoveHandler extends AbstractMovementPacketHandler {
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter chr = c.getPlayer();
        int vx = slea.readShort();
        int vy = slea.readShort();
        Point p = new Point(vx, vy);
        List<LifeMovementFragment> res = parseMovement(slea);
        final MapleDragon dragon = chr.getDragon();
        
        if (dragon != null) {
            updatePosition(res, dragon, 0);
            if (chr.isHidden()) {
                chr.getMap().broadcastGMMessage(chr, DragonPacket.Packet.onMove(dragon, p, res));
            } else {
                chr.getMap().broadcastMessage(chr, DragonPacket.Packet.onMove(dragon, p, res), dragon.getPosition());
            }
        }
    }
}