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

import java.awt.*;
import java.util.List;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.MaplePet;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import tools.exceptions.EmptyMovementException;

public final class MovePetHandler extends AbstractMovementPacketHandler {
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int petId = slea.readInt();
        slea.readInt();
        int vx = slea.readShort();
        int vy = slea.readShort();
        Point p = new Point(vx, vy);
        List<LifeMovementFragment> res = parseMovement(slea);
        MapleCharacter player = c.getPlayer();

        if (player == null) return;

        byte slot = player.getPetIndex(petId);
        if (slot == -1) return;

        MaplePet pet = player.getPet(slot);
        pet.updatePosition(res);
        player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player, pet, p, res), player.getPet(slot).getPos());
        //player.getMap().broadcastMessage(player, MaplePacketCreator.movePet(player.getId(), petId, slot, res), false);
    }
}
