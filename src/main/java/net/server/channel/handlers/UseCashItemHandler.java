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
import enums.FieldEffectType;
import network.packet.*;
import network.packet.field.CField;
import network.packet.wvscontext.WvsContext;
import server.skills.PlayerSkill;
import client.creator.veteran.*;
import client.inventory.Equip;
import client.inventory.Equip.ScrollResult;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import client.inventory.InventoryOperation;
import client.inventory.manipulator.MapleInventoryManipulator;
import client.inventory.manipulator.MapleKarmaManipulator;
import client.processor.AssignAPProcessor;
import client.processor.DueyProcessor;
import constants.GameConstants;
import constants.ItemConstants;
import constants.ServerConstants;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.AbstractMaplePacketHandler;
import net.server.Server;
import server.MapleItemInformationProvider;
import server.MapleShop;
import server.MapleShopFactory;
import server.TimerManager;
import server.maps.AbstractMapleMapObject;
import server.maps.FieldLimit;
import server.maps.MaplePlayerShopItem;
import server.maps.MessageBox;
import server.maps.MapleMap;
import server.maps.MapleTVEffect;
import server.skills.SkillFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

public final class UseCashItemHandler extends AbstractMaplePacketHandler {

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        final MapleCharacter player = c.getPlayer();

        long timeNow = currentServerTime();
        if (timeNow - player.getLastUsedCashItem() < 3000) {
            player.dropMessage(1, "You have used a cash item recently. Wait a moment, then try again.");
            c.announce(WvsContext.Packet.enableActions());
            return;
        }
        player.setLastUsedCashItem(timeNow);

        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        short position = slea.readShort();
        int itemId = slea.readInt();
        int itemType = itemId / 10000;

        MapleInventory cashInv = player.getInventory(MapleInventoryType.CASH);
        Item toUse = cashInv.getItem(position);
        if (toUse == null || toUse.getItemId() != itemId) {
            toUse = cashInv.findById(itemId);

            if (toUse == null) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }

            position = toUse.getPosition();
        }

        if (toUse.getQuantity() < 1) {
            c.announce(WvsContext.Packet.enableActions());
            return;
        }

        String medal = "";
        Item medalItem = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -49);
        if (medalItem != null) {
            medal = "<" + ii.getName(medalItem.getItemId()) + "> ";
        }

        if (itemType == 504) { // vip teleport rock
            String error1 = "Either the player could not be found or you were trying to teleport to an illegal location.";
            boolean vip = slea.readByte() == 1 && itemId / 1000 >= 5041;
            remove(c, position, itemId);
            boolean success = false;
            if (!vip) {
                int mapId = slea.readInt();
                if (itemId / 1000 >= 5041 || mapId / 100000000 == player.getMapId() / 100000000) { //check vip or same continent
                    MapleMap targetMap = c.getChannelServer().getMapFactory().getMap(mapId);
                    if (!FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit()) && (targetMap.getForcedReturnId() == 999999999 || mapId < 100000000)) {
                        player.forceChangeMap(targetMap, targetMap.getRandomPlayerSpawnpoint());
                        success = true;
                    } else {
                        player.dropMessage(1, error1);
                    }
                } else {
                    player.dropMessage(1, "You cannot teleport between continents with this teleport rock.");
                }
            } else {
                String name = slea.readMapleAsciiString();
                MapleCharacter victim = c.getChannelServer().getPlayerStorage().getCharacterByName(name);

                if (victim != null) {
                    MapleMap targetMap = victim.getMap();
                    if (!FieldLimit.CANNOTVIPROCK.check(targetMap.getFieldLimit()) && (targetMap.getForcedReturnId() == 999999999 || targetMap.getId() < 100000000)) {
                        if (!victim.isGM() || victim.gmLevel() <= player.gmLevel()) { // cant vip rock to staff now!
                            player.forceChangeMap(targetMap, targetMap.findClosestPlayerSpawnpoint(victim.getPosition()));
                            success = true;
                        } else {
                            player.dropMessage(1, error1);
                        }
                    } else {
                        player.dropMessage(1, "You cannot teleport to this map.");
                    }
                } else {
                    player.dropMessage(1, "Player could not be found in this channel.");
                }
            }

            if (!success) {
                MapleInventoryManipulator.addById(c, itemId, (short) 1);
                c.announce(WvsContext.Packet.enableActions());
            }
        } else if (itemType == 505) { // AP/SP reset
            if (!player.isAlive()) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }

            if (itemId > 5050000) {
                int SPTo = slea.readInt();
                int SPFrom = slea.readInt();
                PlayerSkill skillSPTo = SkillFactory.getSkill(SPTo);
                PlayerSkill skillSPFrom = SkillFactory.getSkill(SPFrom);
                byte curLevel = player.getSkillLevel(skillSPTo);
                byte curLevelSPFrom = player.getSkillLevel(skillSPFrom);
                if ((curLevel < skillSPTo.getMaxLevel()) && curLevelSPFrom > 0) {
                    player.changeSkillLevel(skillSPFrom, (byte) (curLevelSPFrom - 1), player.getMasterLevel(skillSPFrom), -1);
                    player.changeSkillLevel(skillSPTo, (byte) (curLevel + 1), player.getMasterLevel(skillSPTo), -1);
                }
            } else {
                int APTo = slea.readInt();
                int APFrom = slea.readInt();

                if (!AssignAPProcessor.APResetAction(c, APFrom, APTo)) {
                    return;
                }
            }
            remove(c, position, itemId);
        } else if (itemType == 506) {
            Item eq = null;
            if (itemId == 5060000) { // Item tag.
                int equipSlot = slea.readShort();
                if (equipSlot == 0) {
                    return;
                }
                eq = player.getInventory(MapleInventoryType.EQUIPPED).getItem((short) equipSlot);
                eq.setOwner(player.getName());
            } else if (itemId == 5060001 || itemId == 5061000 || itemId == 5061001 || itemId == 5061002 || itemId == 5061003) { // Sealing lock
                MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
                eq = player.getInventory(type).getItem((short) slea.readInt());
                if (eq == null) { //Check if the type is EQUIPMENT?
                    return;
                }
                int flag = eq.getFlag();
                flag |= ItemConstants.LOCK;
                if (eq.getExpiration() > -1) {
                    return; //No perma items pls
                }
                eq.setFlag(flag);

                long period = 0;
                if (itemId == 5061000) {
                    period = 7;
                } else if (itemId == 5061001) {
                    period = 30;
                } else if (itemId == 5061002) {
                    period = 90;
                } else if (itemId == 5061003) {
                    period = 365;
                }

                if (period > 0) {
                    eq.setExpiration(currentServerTime() + (period * 60 * 60 * 24 * 1000));
                }

                remove(c, position, itemId);
            } else if (itemId == 5060002) { // Incubator
                byte inventory2 = (byte) slea.readInt();
                short slot2 = (short) slea.readInt();
                Item item2 = player.getInventory(MapleInventoryType.getByType(inventory2)).getItem(slot2);
                if (item2 == null) // hacking
                {
                    return;
                }
                if (getIncubatedItem(c, itemId)) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.getByType(inventory2), slot2, (short) 1, false);
                    remove(c, position, itemId);
                }
                return;
            }
            slea.readInt(); // time stamp
            if (eq != null) {
                player.forceUpdateItem(eq);
                remove(c, position, itemId);
            }
        } else if (itemType == 507) {
            boolean whisper;
            switch ((itemId / 1000) % 10) {
                case 1: // Megaphone
                    if (player.getLevel() > 9) {
                        player.getClient().getChannelServer().broadcastPacket(MaplePacketCreator.serverNotice(2, medal + player.getName() + " : " + slea.readMapleAsciiString()));
                    } else {
                        player.dropMessage(1, "You may not use this until you're level 10.");
                        return;
                    }
                    break;
                case 2: // Super megaphone
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + slea.readMapleAsciiString(), (slea.readByte() != 0)));
                    break;
                case 5: // Maple TV
                    int tvType = itemId % 10;
                    boolean megassenger = false;
                    boolean ear = false;
                    MapleCharacter victim = null;
                    if (tvType != 1) {
                        if (tvType >= 3) {
                            megassenger = true;
                            if (tvType == 3) {
                                slea.readByte();
                            }
                            ear = 1 == slea.readByte();
                        } else if (tvType != 2) {
                            slea.readByte();
                        }
                        if (tvType != 4) {
                            victim = c.getChannelServer().getPlayerStorage().getCharacterByName(slea.readMapleAsciiString());
                        }
                    }
                    List<String> messages = new LinkedList<>();
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        String message = slea.readMapleAsciiString();
                        if (megassenger) {
                            builder.append(" ").append(message);
                        }
                        messages.add(message);
                    }
                    slea.readInt();

                    if (!MapleTVEffect.broadcastMapleTVIfNotActive(player, victim, messages, tvType)) {
                        player.dropMessage(1, "MapleTV is already in use.");
                        return;
                    }

                    if (megassenger) {
                        Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.serverNotice(3, c.getChannel(), medal + player.getName() + " : " + builder.toString(), ear));
                    }

                    break;
                case 6: //item megaphone
                    String msg = medal + player.getName() + " : " + slea.readMapleAsciiString();
                    whisper = slea.readByte() == 1;
                    Item item = null;
                    if (slea.readByte() == 1) { //item
                        item = player.getInventory(MapleInventoryType.getByType((byte) slea.readInt())).getItem((short) slea.readInt());
                        if (item == null) //hack
                        {
                            return;
                        }
                    }
                    try {
                        Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.itemMegaphone(msg, whisper, c.getChannel(), item));
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case 7: //triple megaphone
                    int lines = slea.readByte();
                    if (lines < 1 || lines > 3) //hack
                    {
                        return;
                    }
                    String[] msg2 = new String[lines];
                    for (int i = 0; i < lines; i++) {
                        msg2[i] = medal + player.getName() + " : " + slea.readMapleAsciiString();
                    }
                    whisper = slea.readByte() == 1;
                    Server.getInstance().broadcastMessage(c.getWorld(), MaplePacketCreator.getMultiMegaphone(msg2, c.getChannel(), whisper));
                    break;
            }
            remove(c, position, itemId);
        } else if (itemType == 508) {   // graduation banner, thanks to tmskdl12. Also, thanks ratency for first pointing lack of Kite handling
            MessageBox kite = new MessageBox(player, slea.readMapleAsciiString(), itemId);

            if (!GameConstants.isFreeMarketRoom(player.getMapId())) {
                player.getMap().spawnKite(kite);
                remove(c, position, itemId);
            } else {
                c.announce(MessageBoxPool.Packet.onCreateFailed());
            }
        } else if (itemType == 509) {
            String sendTo = slea.readMapleAsciiString();
            String msg = slea.readMapleAsciiString();
            try {
                player.sendNote(sendTo, msg, (byte) 0);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            remove(c, position, itemId);
        } else if (itemType == 510) {
            player.getMap().broadcastMessage(CField.Packet.onFieldEffect(FieldEffectType.Music.getMode(), "Jukebox/Congratulation"));
            remove(c, position, itemId);
        } else if (itemType == 512) {
            if (ii.getStateChangeItem(itemId) != 0) {
                for (MapleCharacter mChar : player.getMap().getCharacters()) {
                    ii.getItemEffect(ii.getStateChangeItem(itemId)).applyTo(mChar);
                }
            }
            player.getMap().startMapEffect(ii.getMsg(itemId).replaceFirst("%s", player.getName()).replaceFirst("%s", slea.readMapleAsciiString()), itemId);
            remove(c, position, itemId);
        } else if (itemType == 517) {
            MaplePet pet = player.getPet(0);
            if (pet == null) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }
            String newName = slea.readMapleAsciiString();
            pet.setName(newName);
            pet.saveToDb();

            Item item = player.getInventory(MapleInventoryType.CASH).getItem(pet.getPosition());
            if (item != null)
                player.forceUpdateItem(item);

            player.getMap().broadcastMessage(player, PetPacket.Packet.onNameChange(player, newName), true);
            c.announce(WvsContext.Packet.enableActions());
            remove(c, position, itemId);
        } else if (itemType == 520) {
            player.gainMeso(ii.getMeso(itemId), true, false, true);
            remove(c, position, itemId);
            c.announce(WvsContext.Packet.enableActions());
        } else if (itemType == 523) {
            int itemid = slea.readInt();

            if (!ServerConstants.USE_ENFORCE_ITEM_SUGGESTION) c.getWorldServer().addOwlItemSearch(itemid);
            player.setOwlSearch(itemid);
            List<Pair<MaplePlayerShopItem, AbstractMapleMapObject>> hmsAvailable = c.getWorldServer().getAvailableItemBundles(itemid);
            if (!hmsAvailable.isEmpty()) remove(c, position, itemId);

            c.announce(MaplePacketCreator.owlOfMinerva(c, itemid, hmsAvailable));
            c.announce(WvsContext.Packet.enableActions());

        } else if (itemType == 524) {
            for (byte i = 0; i < 3; i++) {
                MaplePet pet = player.getPet(i);
                if (pet != null) {
                    Pair<Integer, Boolean> p = pet.canConsume(itemId);

                    if (p.getRight()) {
                        pet.gainClosenessFullness(player, p.getLeft(), 100, 1);
                        remove(c, position, itemId);
                        break;
                    }
                } else {
                    break;
                }
            }
            c.announce(WvsContext.Packet.enableActions());
        } else if (itemType == 530) {
            ii.getItemEffect(itemId).applyTo(player);
            remove(c, position, itemId);
        } else if (itemType == 533) {
            DueyProcessor.dueySendTalk(c, true);
        } else if (itemType == 537) {
            if (GameConstants.isFreeMarketRoom(player.getMapId())) {
                player.dropMessage(5, "You cannot use the chalkboard here.");
                player.getClient().announce(WvsContext.Packet.enableActions());
                return;
            }

            player.setChalkboard(slea.readMapleAsciiString());
            player.getMap().broadcastMessage(UserCommon.Packet.onADBoard(player, false));
            player.getClient().announce(WvsContext.Packet.enableActions());
            //remove(c, position, itemId);
        } else if (itemType == 539) {
            List<String> strLines = new LinkedList<>();
            for (int i = 0; i < 4; i++) {
                strLines.add(slea.readMapleAsciiString());
            }

            final int world = c.getWorld();
            try {
                Server.getInstance().broadcastMessage(world, WvsContext.Packet.onSetAvatarMegaphone(player, medal, c.getChannel(), itemId, strLines, (slea.readByte() != 0)));
                TimerManager.getInstance().schedule(() -> Server.getInstance().broadcastMessage(world, WvsContext.Packet.onClearAvatarMegaphone()), 1000 * 10);
                remove(c, position, itemId);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        } else if (itemType == 543) {
            if (itemId == 5432000 && !c.gainCharacterSlot()) {
                player.dropMessage(1, "You have already used up all 12 extra character slots.");
                c.announce(WvsContext.Packet.enableActions());
                return;
            }

            String name = slea.readMapleAsciiString();
            int face = slea.readInt();
            int hair = slea.readInt();
            int haircolor = slea.readInt();
            int skin = slea.readInt();
            int gender = slea.readInt();
            int jobid = slea.readInt();
            int improveSp = slea.readInt();

            int createStatus = switch (jobid) {
                case 0 -> WarriorCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                case 1 -> MagicianCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                case 2 -> BowmanCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                case 3 -> ThiefCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
                default -> PirateCreator.createCharacter(c, name, face, hair + haircolor, skin, gender, improveSp);
            };

            if (createStatus == 0) {
                c.announce(MaplePacketCreator.sendMapleLifeError(0));   // success!

                player.showHint("#bSuccess#k on creation of the new character through the Maple Life card.");
                remove(c, position, itemId);
            } else {
                if (createStatus == -1) {    // check name
                    c.announce(MaplePacketCreator.sendMapleLifeNameError());
                } else {
                    c.announce(MaplePacketCreator.sendMapleLifeError(-1 * createStatus));
                }
            }
        } else if (itemType == 545) { // MiuMiu's travel store
            if (player.getShop() == null) {
                MapleShop shop = MapleShopFactory.getInstance().getShop(1338);
                if (shop != null) {
                    shop.sendShop(c);
                    remove(c, position, itemId);
                }
            } else {
                c.announce(WvsContext.Packet.enableActions());
            }
        } else if (itemType == 550) { //Extend item expiration
            c.announce(WvsContext.Packet.enableActions());
        } else if (itemType == 552) {
            MapleInventoryType type = MapleInventoryType.getByType((byte) slea.readInt());
            short slot = (short) slea.readInt();
            Item item = player.getInventory(type).getItem(slot);
            if (item == null || item.getQuantity() <= 0 || MapleKarmaManipulator.hasKarmaFlag(item) || !ii.isKarmaAble(item.getItemId())) {
                c.announce(WvsContext.Packet.enableActions());
                return;
            }

            MapleKarmaManipulator.setKarmaFlag(item);
            player.forceUpdateItem(item);
            remove(c, position, itemId);
            c.announce(WvsContext.Packet.enableActions());
        } else if (itemType == 552) { //DS EGG THING
            c.announce(WvsContext.Packet.enableActions());
        } else if (itemType == 557) {
            slea.readInt();
            int itemSlot = slea.readInt();
            slea.readInt();
            final Equip equip = (Equip) player.getInventory(MapleInventoryType.EQUIP).getItem((short) itemSlot);
            if (equip.getVicious() >= 2 || player.getInventory(MapleInventoryType.CASH).findById(5570000) == null) {
                return;
            }
            equip.setVicious(equip.getVicious() + 1);
            equip.setUpgradeSlots(equip.getUpgradeSlots() + 1);
            remove(c, position, itemId);
            c.announce(WvsContext.Packet.enableActions());
            c.announce(MaplePacketCreator.sendHammerData(equip.getVicious()));
            player.forceUpdateItem(equip);
        } else if (itemType == 561) { //VEGA'S SPELL
            if (slea.readInt() != 1) {
                return;
            }

            final byte eSlot = (byte) slea.readInt();
            final Item eitem = player.getInventory(MapleInventoryType.EQUIP).getItem(eSlot);

            if (slea.readInt() != 2) {
                return;
            }

            final byte uSlot = (byte) slea.readInt();
            final Item uitem = player.getInventory(MapleInventoryType.USE).getItem(uSlot);
            if (eitem == null || uitem == null) {
                return;
            }

            Equip toScroll = (Equip) eitem;
            if (toScroll.getUpgradeSlots() < 1) {
                c.announce(WvsContext.Packet.onInventoryOperation(true, Collections.emptyList()));
                return;
            }

            //should have a check here against PE hacks
            if (itemId / 1000000 != 5) itemId = 0;

            player.toggleBlockCashShop();

            final int curlevel = toScroll.getLevel();
            c.announce(MaplePacketCreator.sendVegaScroll(0x40));

            final Equip scrolled = (Equip) ii.scrollEquipWithId(toScroll, uitem.getItemId(), false, itemId, player.isGM());
            c.announce(MaplePacketCreator.sendVegaScroll(scrolled.getLevel() > curlevel ? 0x41 : 0x43));
            //opcodes 0x42, 0x44: "this item cannot be used"; 0x39, 0x45: crashes

            MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.USE, uSlot, (short) 1, false);
            remove(c, position, itemId);

            final MapleClient client = c;
            TimerManager.getInstance().schedule(() -> {
                if (!player.isLoggedin()) return;

                player.toggleBlockCashShop();

                final List<InventoryOperation> mods = new ArrayList<>();
                mods.add(new InventoryOperation(3, scrolled));
                mods.add(new InventoryOperation(0, scrolled));
                client.announce(WvsContext.Packet.onInventoryOperation(true, mods));

                ScrollResult scrollResult = scrolled.getLevel() > curlevel ? ScrollResult.SUCCESS : ScrollResult.FAIL;
                player.getMap().broadcastMessage(UserCommon.Packet.onShowItemUpgradeEffect(player.getId(), scrollResult, false, false));
                if (eSlot < 0 && (scrollResult == ScrollResult.SUCCESS)) {
                    player.equipChanged();
                }

                client.announce(WvsContext.Packet.enableActions());
            }, 1000 * 3);
        } else {
            System.out.println("NEW CASH ITEM: " + itemType + "\n" + slea.toString());
            c.announce(WvsContext.Packet.enableActions());
        }
    }

    private static void remove(MapleClient c, short position, int itemid) {
        MapleInventory cashInv = c.getPlayer().getInventory(MapleInventoryType.CASH);
        cashInv.lockInventory();
        try {
            Item it = cashInv.getItem(position);
            if (it == null || it.getItemId() != itemid) {
                it = cashInv.findById(itemid);
                if (it != null) {
                    position = it.getPosition();
                }
            }
        } finally {
            cashInv.unlockInventory();
        }

        MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.CASH, position, (short) 1, true, false);
    }

    private static boolean getIncubatedItem(MapleClient c, int id) {
        final int[] ids = {1012070, 1302049, 1302063, 1322027, 2000004, 2000005, 2020013, 2020015, 2040307, 2040509, 2040519, 2040521, 2040533, 2040715, 2040717, 2040810, 2040811, 2070005, 2070006, 4020009,};
        final int[] quantitys = {1, 1, 1, 1, 240, 200, 200, 200, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3};
        int amount = 0;
        for (int i = 0; i < ids.length; i++) {
            if (i == id) {
                amount = quantitys[i];
            }
        }
        if (c.getPlayer().getInventory(MapleInventoryType.getByType((byte) (id / 1000000))).isFull()) {
            return false;
        }
        MapleInventoryManipulator.addById(c, id, (short) amount);
        return true;
    }
}
