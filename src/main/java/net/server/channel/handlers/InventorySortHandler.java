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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.MapleCharacter;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import network.packet.WvsContext;
import tools.LogHelper;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author BubblesDev
 */
public final class InventorySortHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.getAutobanManager().setTimestamp(4, slea.readInt(), 3);
        byte inventoryType = slea.readByte();

        if (inventoryType < 1 || inventoryType > 5) {
            c.disconnect(false, false);
            // log packet editing attempt
            return;
        }

        final MapleInventoryType invType = MapleInventoryType.getByType(inventoryType);
        MapleInventory inventory = chr.getInventory(invType);
        ArrayList<Item> items = new ArrayList<Item>();

        HashMap<Item, Short> original_items = new HashMap<>();
        for (short x = 1; x <= inventory.getSlotLimit(); x++)
            if (inventory.contains(x))
                original_items.put(inventory.getItem(x), x);

        Collections.sort(items);

        try {
            for (short x = 0; x < items.size(); x++) {
                Item item  = items.get(x);
                Item prev  = inventory.getItem((short) (x + 1));
                short slot = original_items.get(item);
                if (slot != (x + 1)) {
                    MapleInventoryManipulator.move(c, invType, (short) (x + 1), slot);
                    original_items.put(prev, slot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        c.announce(MaplePacketCreator.finishedSort2(inventoryType));
        c.announce(WvsContext.Packet.enableActions());
    }

}
