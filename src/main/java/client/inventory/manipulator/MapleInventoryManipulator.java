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
package client.inventory.manipulator;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.InventoryOperation;
import client.newyear.NewYearCardRecord;
import constants.ItemConstants;
import constants.ServerConstants;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import network.packet.UserRemote;
import network.packet.context.WvsContext;
import server.MapleItemInformationProvider;
import server.maps.MapleMap;

import tools.MaplePacketCreator;

/**
 *
 * @author Matze
 * @author Ronan - improved check space feature & removed redundant object calls
 */
public class MapleInventoryManipulator {

    public static boolean addById(MapleClient c, int itemId, short quantity) {
        return addById(c, itemId, quantity, null, -1, -1);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, long expiration) {
        return addById(c, itemId, quantity, null, -1, (byte) 0, expiration);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid) {
        return addById(c, itemId, quantity, owner, petid, -1);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid, long expiration) {
        return addById(c, itemId, quantity, owner, petid, (byte) 0, expiration);
    }

    public static boolean addById(MapleClient c, int itemId, short quantity, String owner, int petid, int flag, long expiration) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ItemConstants.getInventoryType(itemId);
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, itemId);
            List<Item> existing = c.getPlayer().getInventory(type).listById(itemId);
            if (!ItemConstants.isRechargeable(itemId)) { // pretty sure we dont need a petid check here
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (!i.hasNext()) {
                            break;
                        }
                        Item eItem = (Item) i.next();
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && ((eItem.getOwner().equals(owner) || owner == null)
                                && eItem.getFlag() == flag)) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                            eItem.setQuantity(newQ);
                            eItem.setExpiration(expiration);
                            c.announce(WvsContext.Packet.onInventoryOperation(true,
                                    Collections.singletonList(new InventoryOperation(1, eItem))));
                        }
                    }
                }
                boolean sandboxItem = (flag & ItemConstants.SANDBOX) == ItemConstants.SANDBOX;
                while (quantity > 0 || ItemConstants.isRechargeable(itemId)) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    if (newQ != 0) {
                        quantity -= newQ;
                        Item nItem = new Item(itemId, (short) 0, newQ, petid);
                        nItem.setFlag(flag);
                        nItem.setExpiration(expiration);
                        short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                        if (newSlot == -1) {
                            c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                            c.announce(MaplePacketCreator.getShowInventoryFull());
                            return false;
                        }
                        if (owner != null) {
                            nItem.setOwner(owner);
                        }
                        c.announce(WvsContext.Packet.onInventoryOperation(true,
                                Collections.singletonList(new InventoryOperation(0, nItem))));
                        if(sandboxItem) c.getPlayer().setHasSandboxItem();
                        if ((ItemConstants.isRechargeable(itemId)) && quantity == 0) {
                            break;
                        }
                    } else {
                        c.announce(WvsContext.Packet.enableActions());
                        return false;
                    }
                }
            } else {
                Item nItem = new Item(itemId, (short) 0, quantity, petid);
                nItem.setFlag(flag);
                nItem.setExpiration(expiration);
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                c.announce(WvsContext.Packet.onInventoryOperation(true,
                        Collections.singletonList(new InventoryOperation(0, nItem))));
                if(MapleInventoryManipulator.isSandboxItem(nItem)) c.getPlayer().setHasSandboxItem();
            }
        } else if (quantity == 1) { // one equip per 'transaction'
            Item nEquip = ii.getEquipById(itemId);
            nEquip.setFlag(flag);
            nEquip.setExpiration(expiration);
            if (owner != null) {
                nEquip.setOwner(owner);
            }
            short newSlot = c.getPlayer().getInventory(type).addItem(nEquip);
            if (newSlot == -1) {
                c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                c.announce(MaplePacketCreator.getShowInventoryFull());
                return false;
            }
            c.announce(WvsContext.Packet.onInventoryOperation(true,
                    Collections.singletonList(new InventoryOperation(0, nEquip))));
            if(MapleInventoryManipulator.isSandboxItem(nEquip)) c.getPlayer().setHasSandboxItem();
        } else {
            throw new RuntimeException("Trying to create equip with non-one quantity");
        }
        return true;
    }
    
    public static boolean addFromDrop(MapleClient c, Item item) {
        return addFromDrop(c, item, true);
    }
    
    public static boolean addFromDrop(MapleClient c, Item item, boolean show) {
        return addFromDrop(c, item, show, item.getPetId());
    }

    public static boolean addFromDrop(MapleClient c, Item item, boolean show, int petId) {
        if (item == null) { // no null item gang
            return false;
        }

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ItemConstants.getInventoryType(item.getItemId());

        if (ii.isPickupRestricted(item.getItemId()) && c.getPlayer().haveItemWithId(item.getItemId(), true)) {
            c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
            c.announce(MaplePacketCreator.showItemUnavailable());
            return false;
        }
        short quantity = item.getQuantity();

        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, item.getItemId());
            List<Item> existing = c.getPlayer().getInventory(type).listById(item.getItemId());
            if (!ItemConstants.isRechargeable(item.getItemId()) && petId == -1) {
                if (existing.size() > 0) { // first update all existing slots to slotMax
                    Iterator<Item> i = existing.iterator();
                    while (quantity > 0) {
                        if (!i.hasNext()) break;
                        Item eItem = (Item) i.next();
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && item.getFlag() == eItem.getFlag()
                                && item.getOwner().equals(eItem.getOwner())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                            eItem.setQuantity(newQ);
                            item.setPosition(eItem.getPosition());
                            c.announce(WvsContext.Packet.onInventoryOperation(true,
                                    Collections.singletonList(new InventoryOperation(1, eItem))));
                        }
                    }
                }
                while (quantity > 0) {
                    short newQ = (short) Math.min(quantity, slotMax);
                    quantity -= newQ;
                    Item nItem = new Item(item.getItemId(), (short) 0, newQ, petId);
                    nItem.setExpiration(item.getExpiration());
                    nItem.setOwner(item.getOwner());
                    nItem.setFlag(item.getFlag());
                    short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                    if (newSlot == -1) {
                        c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                        c.announce(MaplePacketCreator.getShowInventoryFull());
                        item.setQuantity((short) (quantity + newQ));
                        return false;
                    }
                    nItem.setPosition(newSlot);
                    item.setPosition(newSlot);
                    c.announce(WvsContext.Packet.onInventoryOperation(true,
                            Collections.singletonList(new InventoryOperation(0, nItem))));
                    if (MapleInventoryManipulator.isSandboxItem(nItem)) c.getPlayer().setHasSandboxItem();
                }
            } else {
                Item nItem = new Item(item.getItemId(), (short) 0, quantity);
                short newSlot = c.getPlayer().getInventory(type).addItem(nItem);
                if (newSlot == -1) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return false;
                }
                nItem.setPosition(newSlot);
                item.setPosition(newSlot);
                c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.singletonList(new InventoryOperation(0, nItem))));
                if (MapleInventoryManipulator.isSandboxItem(nItem)) c.getPlayer().setHasSandboxItem();
            }
        } else if (quantity == 1) {
            short newSlot = c.getPlayer().getInventory(type).addItem(item);
            if (newSlot == -1) {
                c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                c.announce(MaplePacketCreator.getShowInventoryFull());
                return false;
            }
            c.announce(WvsContext.Packet.onInventoryOperation(true,
                    Collections.singletonList(new InventoryOperation(0, item))));
        } else {
            return false;
        }
        if (show) {
            c.announce(MaplePacketCreator.getShowItemGain(item.getItemId(), item.getQuantity()));
        }

        return true;
    }
    
    private static boolean haveItemWithId(MapleInventory inv, int itemid) {
        return inv.findById(itemid) != null;
    }
    
    public static boolean checkSpace(MapleClient c, int itemid, int quantity, String owner) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = ItemConstants.getInventoryType(itemid);
        MapleCharacter chr = c.getPlayer();
        MapleInventory inv = chr.getInventory(type);
        
        if (ii.isPickupRestricted(itemid)) {
            if (haveItemWithId(inv, itemid)) {
                return false;
            } else if (ItemConstants.isEquipment(itemid) && haveItemWithId(chr.getInventory(MapleInventoryType.EQUIPPED), itemid)) {
                return false;
            }
        }
        
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, itemid);
            List<Item> existing = inv.listById(itemid);
            
            final int numSlotsNeeded;
            if (ItemConstants.isRechargeable(itemid)) {
                numSlotsNeeded = 1;
            } else {
                if (existing.size() > 0) // first update all existing slots to slotMax
                {
                    for (Item eItem : existing) {
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
                
                if (slotMax > 0) {
                    numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
                } else {
                    numSlotsNeeded = 1;
                }
            }
            
            return !inv.isFull(numSlotsNeeded - 1);
        } else {
            return !inv.isFull();
        }
    }

    public static int checkSpaceProgressively(MapleClient c, int itemid, int quantity, String owner, int usedSlots, boolean useProofInv) {
        // return value --> bit0: if has space for this one;
        //                  value after: new slots filled;
        // assumption: equipments always have slotMax == 1.
        
        int returnValue;
        
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        MapleInventoryType type = !useProofInv ? ItemConstants.getInventoryType(itemid) : MapleInventoryType.CANHOLD;
        MapleCharacter chr = c.getPlayer();
        MapleInventory inv = chr.getInventory(type);
        
        if (ii.isPickupRestricted(itemid)) {
            if (haveItemWithId(inv, itemid)) {
                return 0;
            } else if (ItemConstants.isEquipment(itemid) && haveItemWithId(chr.getInventory(MapleInventoryType.EQUIPPED), itemid)) {
                return 0;   // thanks Captain & Aika & Vcoc for pointing out inventory checkup on player trades missing out one-of-a-kind items.
            }
        }
        
        if (!type.equals(MapleInventoryType.EQUIP)) {
            short slotMax = ii.getSlotMax(c, itemid);
            final int numSlotsNeeded;
            
            if (ItemConstants.isRechargeable(itemid)) {
                numSlotsNeeded = 1;
            } else {
                List<Item> existing = inv.listById(itemid);
                
                if (existing.size() > 0) // first update all existing slots to slotMax
                {
                    for (Item eItem : existing) {
                        short oldQ = eItem.getQuantity();
                        if (oldQ < slotMax && owner.equals(eItem.getOwner())) {
                            short newQ = (short) Math.min(oldQ + quantity, slotMax);
                            quantity -= (newQ - oldQ);
                        }
                        if (quantity <= 0) {
                            break;
                        }
                    }
                }
                
                if (slotMax > 0) {
                    numSlotsNeeded = (int) (Math.ceil(((double) quantity) / slotMax));
                } else {
                    numSlotsNeeded = 1;
                }
            }
            
            returnValue = ((numSlotsNeeded + usedSlots) << 1);
            returnValue += (numSlotsNeeded == 0 || !inv.isFullAfterSomeItems(numSlotsNeeded - 1, usedSlots)) ? 1 : 0;
            //System.out.print(" needed " + numSlotsNeeded + " used " + usedSlots + " rval " + returnValue);
        } else {
            returnValue = ((quantity + usedSlots) << 1);
            returnValue += (!inv.isFullAfterSomeItems(0, usedSlots)) ? 1 : 0;
            //System.out.print(" eqpneeded " + 1 + " used " + usedSlots + " rval " + returnValue);
        }
        
        return returnValue;
    }

    public static void removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop) {
        removeFromSlot(c, type, slot, quantity, fromDrop, false);
    }

    public static void removeFromSlot(MapleClient c, MapleInventoryType type, short slot, short quantity, boolean fromDrop, boolean consume) {
        MapleCharacter chr = c.getPlayer();
        MapleInventory inv = chr.getInventory(type);
        Item item = inv.getItem(slot);
        boolean allowZero = consume && ItemConstants.isRechargeable(item.getItemId());
        
        if(type == MapleInventoryType.EQUIPPED) {
            inv.lockInventory();
            try {
                chr.unequippedItem((Equip) item);
                inv.removeItem(slot, quantity, allowZero);
            } finally {
                inv.unlockInventory();
            }
            
            announceModifyInventory(c, item, fromDrop, allowZero);
        } else {
            int petid = item.getPetId();
            if (petid > -1) { // thanks Vcoc for finding a d/c issue with equipped pets & pets remaining on DB here
                int petIdx = chr.getPetIndex(petid);
                if(petIdx > -1) {
                    MaplePet pet = chr.getPet(petIdx);
                    chr.unequipPet(pet, true);
                }
            
                inv.removeItem(slot, quantity, allowZero);
                if(type != MapleInventoryType.CANHOLD) {
                    announceModifyInventory(c, item, fromDrop, allowZero);
                }
                
                // thanks Robin Schulz for noticing pet issues when moving pets out of inventory
            } else {
                inv.removeItem(slot, quantity, allowZero);
                if(type != MapleInventoryType.CANHOLD) {
                    announceModifyInventory(c, item, fromDrop, allowZero);
                }
            }
        }
    }
    
    private static void announceModifyInventory(MapleClient c, Item item, boolean fromDrop, boolean allowZero) {
        if (item.getQuantity() == 0 && !allowZero) {
            c.announce(WvsContext.Packet.onInventoryOperation(fromDrop, Collections.singletonList(new InventoryOperation(3, item))));
        } else {
            c.announce(WvsContext.Packet.onInventoryOperation(fromDrop, Collections.singletonList(new InventoryOperation(1, item))));
        }
    }

    public static void removeById(MapleClient c, MapleInventoryType type, int itemId, int quantity, boolean fromDrop, boolean consume) {
        int removeQuantity = quantity;
        MapleInventory inv = c.getPlayer().getInventory(type);
	int slotLimit = type == MapleInventoryType.EQUIPPED ? 128 : inv.getSlotLimit();
	
        for (short i = 0; i <= slotLimit; i++) {
            Item item = inv.getItem((short) (type == MapleInventoryType.EQUIPPED ? -i : i));
            if (item != null) {
                if (item.getItemId() == itemId || item.getCashId() == itemId) {
                    if (removeQuantity <= item.getQuantity()) {
                        removeFromSlot(c, type, item.getPosition(), (short) removeQuantity, fromDrop, consume);
                        removeQuantity = 0;
                        break;
                    } else {
                        removeQuantity -= item.getQuantity();
                        removeFromSlot(c, type, item.getPosition(), item.getQuantity(), fromDrop, consume);
                    }
                }
            }
        }
        if (removeQuantity > 0 && type != MapleInventoryType.CANHOLD) {
            throw new RuntimeException("[Hack] Not enough items available of Item:" + itemId + ", Quantity (After Quantity/Over Current Quantity): " + (quantity - removeQuantity) + "/" + quantity);
        }
    }

    private static boolean isSameOwner(Item source, Item target) {
        return source.getOwner().equals(target.getOwner());
    }
    
    public static void move(MapleClient c, MapleInventoryType type, short src, short dst) {
        MapleInventory inv = c.getPlayer().getInventory(type);
        
        if (src < 0 || dst < 0) {
            return;
        }
        if(dst > inv.getSlotLimit()) {
            return;
        }
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item source = inv.getItem(src);
        Item initialTarget = inv.getItem(dst);
        if (source == null) {
            return;
        }
        short olddstQ = -1;
        if (initialTarget != null) {
            olddstQ = initialTarget.getQuantity();
        }
        short oldsrcQ = source.getQuantity();
        short slotMax = ii.getSlotMax(c, source.getItemId());
        inv.move(src, dst, slotMax);
        final List<InventoryOperation> mods = new ArrayList<>();
        if (!(type.equals(MapleInventoryType.EQUIP) || type.equals(MapleInventoryType.CASH)) && initialTarget != null && initialTarget.getItemId() == source.getItemId() && !ItemConstants.isRechargeable(source.getItemId()) && isSameOwner(source, initialTarget)) {
            if ((olddstQ + oldsrcQ) > slotMax) {
                mods.add(new InventoryOperation(1, source));
                mods.add(new InventoryOperation(1, initialTarget));
            } else {
                mods.add(new InventoryOperation(3, source));
                mods.add(new InventoryOperation(1, initialTarget));
            }
        } else {
            mods.add(new InventoryOperation(2, source, src));
        }
        c.announce(WvsContext.Packet.onInventoryOperation(true, mods));
    }

    /**
     * Binds equipment to specified equipment slot (dst) from specified inventory slot (src)
     * and removes any other equips if deemed necessary such as an overall removing a bottom equip, etc.
     * @param c client
     * @param src the inventory slot id
     * @param dst the id of the equipment slot
     */
    public static void equip(MapleClient c, short src, short dst) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        
        MapleCharacter chr = c.getPlayer();
        MapleInventory eqpInv = chr.getInventory(MapleInventoryType.EQUIP);
        MapleInventory eqpdInv = chr.getInventory(MapleInventoryType.EQUIPPED);
        
        Equip source = (Equip) eqpInv.getItem(src);
        if (source == null || !ii.canWearEquipment(chr, source, dst)) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        } else if ((((source.getItemId() >= 1902000 && source.getItemId() <= 1902002) || source.getItemId() == 1912000) && chr.isCygnus()) || ((source.getItemId() >= 1902005 && source.getItemId() <= 1902007) || source.getItemId() == 1912005) && !chr.isCygnus()) {// Adventurer taming equipment
            return;
        }
        boolean itemChanged = false;
        if (ii.isUntradeableOnEquip(source.getItemId())) {
            source.setFlag((byte) ItemConstants.UNTRADEABLE);
            itemChanged = true;
        }
        if (dst == -6) { // unequip the overall
            Item top = eqpdInv.getItem((short) -5);
            if (top != null && ItemConstants.isOverall(top.getItemId())) {
                if (eqpInv.isFull()) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -5, eqpInv.getNextFreeSlot());
            }
        } else if (dst == -5) {
            final Item bottom = eqpdInv.getItem((short) -6);
            if (bottom != null && ItemConstants.isOverall(source.getItemId())) {
                if (eqpInv.isFull()) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -6, eqpInv.getNextFreeSlot());
            }
        } else if (dst == -10) {// check if weapon is two-handed
            Item weapon = eqpdInv.getItem((short) -11);
            if (weapon != null && ii.isTwoHanded(weapon.getItemId())) {
                if (eqpInv.isFull()) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -11, eqpInv.getNextFreeSlot());
            }
        } else if (dst == -11) {
            Item shield = eqpdInv.getItem((short) -10);
            if (shield != null && ii.isTwoHanded(source.getItemId())) {
                if (eqpInv.isFull()) {
                    c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                    c.announce(MaplePacketCreator.getShowInventoryFull());
                    return;
                }
                unequip(c, (byte) -10, eqpInv.getNextFreeSlot());
            }
        }
        if (dst == -18) {
            if (chr.getMount() != null) {
                chr.getMount().setItemId(source.getItemId());
            }
        }
        
        //1112413, 1112414, 1112405 (Lilin's Ring)
        source = (Equip) eqpInv.getItem(src);
        eqpInv.removeSlot(src);
        
        Equip target;
        eqpdInv.lockInventory();
        try {
            target = (Equip) eqpdInv.getItem(dst);
            if (target != null) {
                chr.unequippedItem(target);
                eqpdInv.removeSlot(dst);
            }
        } finally {
            eqpdInv.unlockInventory();
        }

        final List<InventoryOperation> mods = new ArrayList<>();
        if (itemChanged) {
            mods.add(new InventoryOperation(3, source));
            mods.add(new InventoryOperation(0, source.copy()));//to prevent crashes
        }
        
        if (source.getItemId() == 1142998) {
            source.setPosition((short)-20);
        } else {
            source.setPosition(dst);
        }
        
        eqpdInv.lockInventory();
        try {
            if (source.getRingId() > -1) {
                chr.getRingById(source.getRingId()).equip();
            }
            chr.equippedItem(source);
            eqpdInv.addItemFromDB(source);
        } finally {
            eqpdInv.unlockInventory();
        }
        
        if (target != null) {
            target.setPosition(src);
            eqpInv.addItemFromDB(target);
        }
        if (chr.getBuffedValue(MapleBuffStat.BOOSTER) != null && ItemConstants.isWeapon(source.getItemId())) {
            chr.cancelBuffStats(MapleBuffStat.BOOSTER);
        }
        
        mods.add(new InventoryOperation(2, source, src));
        c.announce(WvsContext.Packet.onInventoryOperation(true, mods));
        chr.equipChanged();
    }

    public static void unequip(MapleClient c, short src, short dst) {
        MapleCharacter chr = c.getPlayer();
        MapleInventory eqpInv = chr.getInventory(MapleInventoryType.EQUIP);
        MapleInventory eqpdInv = chr.getInventory(MapleInventoryType.EQUIPPED);
        
        Equip source = (Equip) eqpdInv.getItem(src);
        Equip target = (Equip) eqpInv.getItem(dst);
        if (dst < 0) {
            return;
        }
        if (source == null || source.getItemId() == 1142998) { // crusader codex shouldn't be unequipped
            return;
        }
        if (target != null && src <= 0) {
            c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
            return;
        }
        
        eqpdInv.lockInventory();
        try {
            if (source.getRingId() > -1) {
                chr.getRingById(source.getRingId()).unequip();
            }
            chr.unequippedItem(source);
            eqpdInv.removeSlot(src);
        } finally {
            eqpdInv.unlockInventory();
        }
        
        if (target != null) {
            eqpInv.removeSlot(dst);
        }
        source.setPosition(dst);
        eqpInv.addItemFromDB(source);
        if (target != null) {
            target.setPosition(src);
            eqpdInv.addItemFromDB(target);
        }
        c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.singletonList(new InventoryOperation(2, source, src))));
        chr.equipChanged();
    }

    public static void drop(MapleClient c, MapleInventoryType type, short src, short quantity) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (src < 0) {
            type = MapleInventoryType.EQUIPPED;
        }
        
        MapleCharacter chr = c.getPlayer();
        MapleInventory inv = chr.getInventory(type);
        Item source = inv.getItem(src);

        if (chr.getTrade() != null || chr.getMiniGame() != null || source == null) { //Only check needed would prob be merchants (to see if the player is in one)
        	return;
        }
        int itemId = source.getItemId();
        if (ItemConstants.isPet(itemId) || source.getItemId() == 1142998) { // codex should not be dropped
            return;
        }
        
        MapleMap map = chr.getMap();
        if ((!ItemConstants.isRechargeable(itemId) && source.getQuantity() < quantity) || quantity < 0) {
            return;
        }
        Point dropPos = new Point(chr.getPosition());
        if (quantity < source.getQuantity() && !ItemConstants.isRechargeable(itemId)) {
            Item target = source.copy();
            target.setQuantity(quantity);
            source.setQuantity((short) (source.getQuantity() - quantity));
            c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.singletonList(new InventoryOperation(1, source))));
            
            if(ItemConstants.isNewYearCardEtc(itemId)) {
                if(itemId == 4300000) {
                    NewYearCardRecord.removeAllNewYearCard(true, chr);
                    //c.getAbstractPlayerInteraction().removeAll(4300000);
                } else {
                    NewYearCardRecord.removeAllNewYearCard(false, chr);
                    //c.getAbstractPlayerInteraction().removeAll(4301000);
                }
            } else if (ItemConstants.isWeddingRing(source.getItemId())) {
                map.disappearingItemDrop(chr, chr, target, dropPos);
            } else if (map.getEverlast()) {
                if (ii.isDropRestricted(target.getItemId()) || ii.isCash(target.getItemId()) || isDroppedItemRestricted(target)) {
                    map.disappearingItemDrop(chr, chr, target, dropPos);
                } else {
                    map.spawnItemDrop(chr, chr, target, dropPos, true, true);
                }
            } else if (ii.isDropRestricted(target.getItemId()) || ii.isCash(target.getItemId()) || isDroppedItemRestricted(target)) {
                map.disappearingItemDrop(chr, chr, target, dropPos);
            } else {
                map.spawnItemDrop(chr, chr, target, dropPos, true, true);
            }
        } else {
            if (type == MapleInventoryType.EQUIPPED) {
                inv.lockInventory();
                try {
                    chr.unequippedItem((Equip) source);
                    inv.removeSlot(src);
                } finally {
                    inv.unlockInventory();
                }
            } else {
                inv.removeSlot(src);
            }
            
            c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.singletonList(new InventoryOperation(3, source))));
            if (src < 0) {
                chr.equipChanged();
            } else if(ItemConstants.isNewYearCardEtc(itemId)) {
                if(itemId == 4300000) {
                    NewYearCardRecord.removeAllNewYearCard(true, chr);
                    //c.getAbstractPlayerInteraction().removeAll(4300000);
                } else {
                    NewYearCardRecord.removeAllNewYearCard(false, chr);
                    //c.getAbstractPlayerInteraction().removeAll(4301000);
                }
            }
            
            if (map.getEverlast()) {
                if (ii.isDropRestricted(itemId) || ii.isCash(itemId) || isDroppedItemRestricted(source)) {
                    map.disappearingItemDrop(chr, chr, source, dropPos);
                } else {
                    map.spawnItemDrop(chr, chr, source, dropPos, true, true);
                }
            } else if (ii.isDropRestricted(itemId) || ii.isCash(itemId) || isDroppedItemRestricted(source)) {
                map.disappearingItemDrop(chr, chr, source, dropPos);           
            } else {
                map.spawnItemDrop(chr, chr, source, dropPos, true, true);
            }
        }
        
        int quantityNow = chr.getItemQuantity(itemId, false);
        if (itemId == chr.getItemEffect()) {
            if (quantityNow <= 0) {
                chr.setItemEffect(0);
                map.broadcastMessage(UserRemote.Packet.onSetActiveEffectItem(chr.getId(), 0));
            }
        } else if (itemId == 5370000 || itemId == 5370001) {
            if (source.getQuantity() <= 0) {
                chr.setChalkboard(null);
            }
        } else if (itemId == 4031868) {
            chr.updateAriantScore(quantityNow);
        }
    }

    private static boolean isDroppedItemRestricted(Item it) {
        return ServerConstants.USE_ERASE_UNTRADEABLE_DROP && it.isUntradeable();
    }
    
    public static boolean isSandboxItem(Item it) {
        return (it.getFlag() & ItemConstants.SANDBOX) == ItemConstants.SANDBOX;
    }
}
