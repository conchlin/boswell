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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import client.MapleClient;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.ModifyInventory;
import client.MapleCharacter;
import constants.ItemConstants;
import net.AbstractMaplePacketHandler;
import client.inventory.manipulator.MapleInventoryManipulator;
import network.packet.WvsContext;
import server.MapleItemInformationProvider;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;

public final class InventoryMergeHandler extends AbstractMaplePacketHandler {

    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.getAutobanManager().setTimestamp(2, slea.readInt(), 3);
        MapleInventoryType inventoryType = MapleInventoryType.getByType(slea.readByte());
        if (inventoryType.equals(MapleInventoryType.UNDEFINED)) {
            c.getSession().write(WvsContext.Packet.enableActions());
            return;
        }

        /*
         * free_slots is a queue that logs free spaces and pops first in first out style
         * items stores instances of an item that are currently under the maximum slot limit so
         * you can fill it up afterwards when you encounter another item later on
         */
        MapleInventory inventory = c.getPlayer().getInventory(inventoryType);
        Queue<Short> free_slots = new LinkedList<>();
        Map<Integer, LinkedList<Short>> items = new HashMap<>();

        // Iterating over slot limit by integer is much safer then iterating over items
        for (short x = 1; x <= inventory.getSlotLimit(); x++) {
            if (!inventory.contains(x)) {  // If empty space add it to the queue
                free_slots.add(x);
            } else { // Otherwise if there's an item
                Item item = inventory.getItem(x);

                // Create key value pair for non-rechargable items to transfer into non-filled item slots
                if (!ItemConstants.isRechargeable(item.getItemId()) && !items.containsKey(item.getItemId()))
                    items.put(item.getItemId(), new LinkedList<Short>());

                // Get max item quantity
                short max_slot = MapleItemInformationProvider.getInstance().getSlotMax(c, inventory.getItem(x).getItemId());

                // Iterator to go over all unfilled items
                Iterator<Short> it = null;
                if (items.get(item.getItemId()) != null)
                    it = items.get(item.getItemId()).iterator();

                // Sometimes items such as ilbis cant be combined so you have to have a null check
                while (it != null && it.hasNext()) {
                    List<ModifyInventory> mods = new ArrayList<>();
                    short entry_slot = it.next();
                    Item entry = inventory.getItem(entry_slot);
                    int difference = max_slot - entry.getQuantity();
                    if (item.getQuantity() > difference) {
                        entry.setQuantity(max_slot);
                        item.setQuantity((short) (item.getQuantity() - difference));
                        mods.add(new ModifyInventory(1, item));
                        mods.add(new ModifyInventory(1, entry));
                    } else {
                        entry.setQuantity((short) (entry.getQuantity() + item.getQuantity()));
                        MapleInventoryManipulator.removeFromSlot(c, inventoryType, x, item.getQuantity(), false);
                        mods.add(new ModifyInventory(1, entry));
                        c.announce(MaplePacketCreator.modifyInventory(true, mods));
                        break;
                    }
                    c.announce(MaplePacketCreator.modifyInventory(true, mods));
                }

                // If the item even still exists or is rechargable
                if (item.getQuantity() > 0 || ItemConstants.isRechargeable(item.getItemId())) {
                    if (item.getQuantity() < max_slot && !ItemConstants.isRechargeable(item.getItemId()))
                        if (items.get(item.getItemId()) != null) {
                            if (free_slots.peek() != null)
                                items.get(item.getItemId()).add(free_slots.peek());
                            else
                                items.get(item.getItemId()).add(x);
                        }

                    if (free_slots.peek() != null) {
                        MapleInventoryManipulator.move(c, inventoryType, x, free_slots.poll());
                        free_slots.add(x);
                    }
                }
            }
        }



        c.getSession().write(MaplePacketCreator.finishedGather(inventoryType.getType()));
        c.getSession().write(WvsContext.Packet.enableActions());
    }
}
