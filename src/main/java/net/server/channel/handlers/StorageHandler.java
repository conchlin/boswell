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
import client.autoban.AutobanFactory;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import constants.ItemConstants;
import enums.TrunkErrorType;
import net.AbstractMaplePacketHandler;
import network.packet.Trunk;
import network.packet.WvsContext;
import server.MapleItemInformationProvider;
import server.MapleStorage;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Matze
 */
public final class StorageHandler extends AbstractMaplePacketHandler {
	@Override
	public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
		MapleCharacter chr = c.getPlayer();
		MapleStorage storage = chr.getStorage();
		byte mode = slea.readByte();

		if (chr.getLevel() < 15) {
			chr.dropMessage(1, "You may only use the storage once you have reached level 15.");
			c.announce(WvsContext.Packet.enableActions());
			return;
		}
		if (chr.getTrade() != null || chr.getShop() != null) {
			//Apparently there is a dupe exploit that causes racing conditions when saving/retrieving from the db with stuff like trade open.
			c.announce(WvsContext.Packet.enableActions());
			return;
		}

		if (c.tryacquireClient()) {
			try {
				if (mode == 4) { // take out
					byte type = slea.readByte();
					byte slot = slea.readByte();
					if (slot < 0 || slot > storage.getSlots()) { // removal starts at zero
						AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
						FilePrinter.print(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to work with storage slot " + slot);
						c.disconnect(true, false);
						return;
					}
					slot = storage.getSlot(MapleInventoryType.getByType(type), slot);
					Item item = storage.getItem(slot);
					if (item != null) {
						if (MapleItemInformationProvider.getInstance().isPickupRestricted(item.getItemId()) && chr.haveItemWithId(item.getItemId(), true)) {
							c.announce(Trunk.Packet.getStorageError(TrunkErrorType.OneOfAKind.getError()));
							return;
						}

						int takeoutFee = storage.getTakeOutFee();
						if (chr.getMeso() < takeoutFee) {
							c.announce(Trunk.Packet.getStorageError(TrunkErrorType.InsufficientMesos.getError()));
							return;
						} else {
							chr.gainMeso(-takeoutFee, false);
						}

						if (MapleInventoryManipulator.checkSpace(c, item.getItemId(), item.getQuantity(), item.getOwner())) {
							item = storage.takeOut(slot);//actually the same but idc
							String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
							FilePrinter.print(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " took out " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
							chr.setUsedStorage();
							MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
							MapleInventoryManipulator.addFromDrop(c, item, false);
							storage.sendTakenOut(c, item.getInventoryType());
						} else {
							c.announce(Trunk.Packet.getStorageError(TrunkErrorType.FullInventory.getError()));
						}
					}
				} else if (mode == 5) { // store
					short slot = slea.readShort();
					int itemId = slea.readInt();
					short quantity = slea.readShort();
					MapleInventoryType invType = ItemConstants.getInventoryType(itemId);
					MapleInventory inv = chr.getInventory(invType);
					if (slot < 1 || slot > inv.getSlotLimit()) { //player inv starts at one
						AutobanFactory.PACKET_EDIT.alert(c.getPlayer(), c.getPlayer().getName() + " tried to packet edit with storage.");
						FilePrinter.print(FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + " tried to store item at slot " + slot);
						c.disconnect(true, false);
						return;
					}
					if (quantity < 1) {
						c.announce(WvsContext.Packet.enableActions());
						return;
					}
					if (storage.isFull()) {
						c.announce(Trunk.Packet.getStorageError(TrunkErrorType.FullTrunk.getError()));
						return;
					}

					int storeFee = storage.getStoreFee();
					if (chr.getMeso() < storeFee) {
						c.announce(Trunk.Packet.getStorageError(TrunkErrorType.InsufficientMesos.getError()));
					} else {
						Item item;

						inv.lockInventory();    // thanks imbee for pointing a dupe within storage
						try {
							item = inv.getItem(slot);
							if (item != null && item.getItemId() == itemId && (item.getQuantity() >= quantity || ItemConstants.isRechargeable(itemId))) {
								if (ItemConstants.isWeddingRing(itemId) || ItemConstants.isWeddingToken(itemId)) {
									c.announce(WvsContext.Packet.enableActions());
									return;
								}

								if (ItemConstants.isRechargeable(itemId)) {
									quantity = item.getQuantity();
								}

								MapleInventoryManipulator.removeFromSlot(c, invType, slot, quantity, false);
							} else {
								c.announce(WvsContext.Packet.enableActions());
								return;
							}
							item = item.copy();
						} finally {
							inv.unlockInventory();
						}

						chr.gainMeso(-storeFee, false, true, false);

						MapleKarmaManipulator.toggleKarmaFlagToUntradeable(item);
						item.setQuantity(quantity);
						storage.store(item);
						storage.sendStored(c, ItemConstants.getInventoryType(itemId));
						String itemName = MapleItemInformationProvider.getInstance().getName(item.getItemId());
						FilePrinter.print(FilePrinter.STORAGE + c.getAccountName() + ".txt", c.getPlayer().getName() + " stored " + item.getQuantity() + " " + itemName + " (" + item.getItemId() + ")");
						chr.setUsedStorage();
					}
				} else if (mode == 7) { // meso
					int meso = slea.readInt();
					int storageMesos = storage.getMeso();
					int playerMesos = chr.getMeso();
					if ((meso > 0 && storageMesos >= meso) || (meso < 0 && playerMesos >= -meso)) {
						if (meso < 0 && (storageMesos - meso) < 0) {
							meso = Integer.MIN_VALUE + storageMesos;
							if (meso < playerMesos) {
								c.announce(WvsContext.Packet.enableActions());
								return;
							}
						} else if (meso > 0 && (playerMesos + meso) < 0) {
							meso = Integer.MAX_VALUE - playerMesos;
							if (meso > storageMesos) {
								c.announce(WvsContext.Packet.enableActions());
								return;
							}
						}
						storage.setMeso(storageMesos - meso);
						chr.gainMeso(meso, false, true, false);
						FilePrinter.print(FilePrinter.STORAGE + c.getPlayer().getName() + ".txt", c.getPlayer().getName() + (meso > 0 ? " took out " : " stored ") + Math.abs(meso) + " mesos");
						chr.setUsedStorage();
					} else {
						c.announce(WvsContext.Packet.enableActions());
						return;
					}
					storage.sendMeso(c);
				} else if (mode == 8) {// close
					storage.close();
				}
			} finally {
				c.releaseClient();
			}
		}
	}
}