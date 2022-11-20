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
package server.life;

import client.MapleClient;
import network.packet.NpcPool;
import server.MapleShopFactory;
import server.maps.MapleMapObjectType;
import tools.MaplePacketCreator;

public class MapleNPC extends AbstractLoadedMapleLife {
    private MapleNPCStats stats;

    public MapleNPC(int id, MapleNPCStats stats) {
        super(id);
        this.stats = stats;
    }

    public boolean hasShop() {
        return MapleShopFactory.getInstance().getShopForNPC(getId()) != null;
    }

    public void sendShop(MapleClient c) {
        MapleShopFactory.getInstance().getShopForNPC(getId()).sendShop(c);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        // removes the following NPC: Maple TV's, Inkwell, Mimi, Abdullah, Gaga, first job statues, power b. fore
        int[] npcToRemove = {9201066, 9250023, 9250024, 9250025, 9250026, 9250042, 9250043, 9250044, 9250045, 9250046, 
            9270000, 9270001, 9270002, 9270003, 9270004, 9270005, 9270006, 9270007, 9270008, 9270009, 9270010, 9270011, 9270012, 
            9270013, 9270014, 9270015, 9270016, 9270040, 9270066, 9209001, 9209000, 9000036, 9000069, 9000017, 2041017,
            1012119, 1052114, 1032114, 1022105, 1095002, 2006, 9201123, 9201124, 9201125, 9201126, 9201127, 1002103};
        for (int i : npcToRemove) {
            if (getId() == i) {
                return;
            }
        }
        client.announce(NpcPool.Packet.onEnterField(this));
        client.announce(NpcPool.Packet.spawnNPCRequestController(this, true));
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(NpcPool.Packet.onLeaveField(getObjectId()));
        client.announce(NpcPool.Packet.removeNPCController(getObjectId()));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.NPC;
    }

    public String getName() {
        return stats.getName();
    }
}
