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
import client.MapleCharacter;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.MapleInventoryType;
import constants.ItemConstants;
import client.inventory.manipulator.MapleInventoryManipulator;
import constants.GameConstants;

import java.util.*;

import enums.BroadcastMessageType;
import enums.UserEffectType;
import net.AbstractMaplePacketHandler;
import network.packet.UserLocal;
import network.packet.UserRemote;
import network.packet.context.BroadcastMsgPacket;
import server.MakerItemFactory;
import server.MakerItemFactory.MakerItemCreateEntry;
import server.MapleItemInformationProvider;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;


/**
 * @author Jay Estrella, Ronan
 */
public final class MakerSkillHandler extends AbstractMaplePacketHandler {

    private static MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int type = slea.readInt();
        int toCreate = slea.readInt();
        int toDisassemble = -1, pos = -1;
        boolean makerSucceeded = true;

        MakerItemFactory.MakerItemCreateEntry recipe;
        Map<Integer, Short> reagentids = new LinkedHashMap<>();
        int stimulantid = -1;

        if (type == 3) {    // building monster crystal
            int fromLeftover = toCreate;
            toCreate = ii.getMakerCrystalFromLeftover(toCreate);
            if (toCreate == -1) {
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        ii.getName(fromLeftover) + " is unavailable for Monster Crystal conversion."));
                c.announce(UserLocal.Packet.makerEnableActions());
                return;
            }

            recipe = MakerItemFactory.generateLeftoverCrystalEntry(fromLeftover, toCreate);
        } else if (type == 4) {  // disassembling
            slea.readInt(); // 1... probably inventory type
            pos = slea.readInt();

            Item it = c.getPlayer().getInventory(MapleInventoryType.EQUIP).getItem((short) pos);
            if (it != null && it.getItemId() == toCreate) {
                toDisassemble = toCreate;

                Pair<Integer, List<Pair<Integer, Integer>>> p = generateDisassemblyInfo(toDisassemble);
                if (p != null) {
                    recipe = MakerItemFactory.generateDisassemblyCrystalEntry(toDisassemble, p.getLeft(), p.getRight());
                } else {
                    c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                            ii.getName(toCreate) + " is unavailable for Monster Crystal disassembly."));
                    c.announce(UserLocal.Packet.makerEnableActions());
                    return;
                }
            } else {
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "An unknown error occurred when trying to apply that item for disassembly."));
                c.announce(UserLocal.Packet.makerEnableActions());
                return;
            }
        } else {
            if (ItemConstants.isEquipment(toCreate)) {   // only equips uses stimulant and reagents
                if (slea.readByte() != 0) {  // stimulant
                    stimulantid = ii.getMakerStimulant(toCreate);
                    if (!c.getAbstractPlayerInteraction().haveItem(stimulantid)) {
                        stimulantid = -1;
                    }
                }

                int reagents = Math.min(slea.readInt(), getMakerReagentSlots(toCreate));
                for (int i = 0; i < reagents; i++) {  // crystals
                    int reagentid = slea.readInt();
                    if (ItemConstants.isMakerReagent(reagentid)) {
                        Short rs = reagentids.get(reagentid);
                        if (rs == null) {
                            reagentids.put(reagentid, (short) 1);
                        } else {
                            reagentids.put(reagentid, (short) (rs + 1));
                        }
                    }
                }

                List<Pair<Integer, Short>> toUpdate = new LinkedList<>();
                for (Map.Entry<Integer, Short> r : reagentids.entrySet()) {
                    int qty = c.getAbstractPlayerInteraction().getItemQuantity(r.getKey());

                    if (qty < r.getValue()) {
                        toUpdate.add(new Pair<>(r.getKey(), (short) qty));
                    }
                }

                // remove those not present on player inventory
                if (!toUpdate.isEmpty()) {
                    for (Pair<Integer, Short> rp : toUpdate) {
                        if (rp.getRight() > 0) {
                            reagentids.put(rp.getLeft(), rp.getRight());
                        } else {
                            reagentids.remove(rp.getLeft());
                        }
                    }
                }

                if (!reagentids.isEmpty()) {
                    if (!removeOddMakerReagents(toCreate, reagentids)) {
                        c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                                "You can only use WATK and MATK Strengthening Gems on weapon items."));
                        c.announce(UserLocal.Packet.makerEnableActions());
                        return;
                    }
                }
            }

            recipe = MakerItemFactory.getItemCreateEntry(toCreate, stimulantid, reagentids);
        }

        short createStatus = getCreateStatus(c, recipe);

        switch (createStatus) {
            case -1 -> {// non-available for Maker itemid has been tried to forge
                FilePrinter.printError(FilePrinter.EXPLOITS, "Player " + c.getPlayer().getName() + " tried to craft itemid " + toCreate + " using the Maker skill.");
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "The requested item could not be crafted on this operation."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            case 1 -> { // no items
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "You don't have all required items in your inventory to make " + ii.getName(toCreate) + "."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            case 2 -> { // no meso
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "You don't have enough mesos (" + GameConstants.numberWithCommas(recipe.getCost()) + ") to complete this operation."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            case 3 -> { // no req level
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "You don't have enough level to complete this operation."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            case 4 -> { // no req skill level
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "You don't have enough Maker level to complete this operation."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            case 5 -> { // inventory full
                c.announce(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.Popup.getType(),
                        "Your inventory is full."));
                c.announce(UserLocal.Packet.makerEnableActions());
            }
            default -> {
                if (toDisassemble != -1) {
                    MapleInventoryManipulator.removeFromSlot(c, MapleInventoryType.EQUIP, (short) pos, (short) 1, false);
                } else {
                    for (Pair<Integer, Integer> p : recipe.getReqItems()) {
                        c.getAbstractPlayerInteraction().gainItem(p.getLeft(), (short) -p.getRight(), false);

                    }
                }
                int cost = recipe.getCost();
                boolean strengthen = false;
                if (stimulantid == -1 && reagentids.isEmpty()) {
                    if (cost > 0) c.getPlayer().gainMeso(-cost, false);

                    for (Pair<Integer, Integer> p : recipe.getGainItems()) {
                        //strengthening
                        // left is id, right is quantity
                        if (p.getLeft() >= 4250000 && p.getLeft() <= 4251402) { // gem check bc maybe other cases apply here
                            int newGem = calculateGemReward(p.getLeft());
                            short gemAmt = getGemQuantity(newGem, p.getLeft());
                            c.getAbstractPlayerInteraction().gainItem(newGem, gemAmt, false);
                            c.announce(UserLocal.Packet.makerResult(makerSucceeded, newGem, gemAmt, recipe.getCost(), recipe.getReqItems(), stimulantid, new LinkedList<>(reagentids.keySet())));
                            strengthen = true;
                        } else {
                            c.getAbstractPlayerInteraction().gainItem(p.getLeft(), p.getRight().shortValue(), false);
                        }
                    }
                } else { // reagentid list wouldnt be empty for equips
                    toCreate = recipe.getGainItems().get(0).getLeft();

                    if (stimulantid != -1) c.getAbstractPlayerInteraction().gainItem(stimulantid, (short) -1, false);
                    if (!reagentids.isEmpty()) {
                        for (Map.Entry<Integer, Short> r : reagentids.entrySet()) {
                            c.getAbstractPlayerInteraction().gainItem(r.getKey(), (short) (-1 * r.getValue()), false);
                        }
                    }

                    if (cost > 0) c.getPlayer().gainMeso(-cost, false);
                    makerSucceeded = addBoostedMakerItem(c, toCreate, stimulantid, reagentids);
                }

                // thanks inhyuk for noticing missing MAKER_RESULT packets
                if (type == 3) {
                    c.announce(UserLocal.Packet.makerResultCrystal(recipe.getGainItems().get(0).getLeft(), recipe.getReqItems().get(0).getLeft()));
                } else if (type == 4) {
                    c.announce(UserLocal.Packet.makerResultDesynth(recipe.getReqItems().get(0).getLeft(), recipe.getCost(), recipe.getGainItems()));
                } else {
                    if (!strengthen) {
                        c.announce(UserLocal.Packet.makerResult(makerSucceeded, recipe.getGainItems().get(0).getLeft(), recipe.getGainItems().get(0).getRight(), recipe.getCost(), recipe.getReqItems(), stimulantid, new LinkedList<>(reagentids.keySet())));
                    }
                }

                int effect = makerSucceeded ? 1 : 0;
                c.announce(UserLocal.Packet.onEffect(UserEffectType.MAKER.getEffect(), "", effect));
                c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
                        UserRemote.Packet.onRemoteUserEffect(c.getPlayer().getId(), UserEffectType.MAKER.getEffect(), effect), false);
                if (toCreate == 4260003 && type == 3 && c.getPlayer().getQuestStatus(6033) == 1) {
                    c.getAbstractPlayerInteraction().setQuestProgress(6033, 1);
                }
            }
        }
    }

    private static int calculateGemReward(int id) {
        int newGem;
        Random rand = new Random();
        int chance = rand.nextInt(99);
        /*
        STRENGTHENING %
        1 Gem ~ 1 Basic 85%
        1 Gem ~ 1 Intermediate 10%
        1 Gem ~ 1 Advanced 5%
        10 Basic ~ 1 Intermediate 85%
        10 Basic ~ 9 Basic 10%
        10 Basic ~ 1 Advanced 5%
        10 Intermediate ~ 1 Advanced 90%
        10 Intermediate ~ 9 Intermediate 10%
        */

        if((id % 10) == 1) { // intermediate create
            if (chance <= 85) {
                newGem = id;
            } else if (chance <= 95) {
                newGem = id - 1;
            } else {
                newGem = id + 1;
            }
        } else if ((id % 10) == 0) { // basic create
            if (chance <= 85) {
                newGem = id;
            } else if (chance <= 95) {
                newGem = id + 1;
            } else {
                newGem = id + 2;
            }
        } else  { // advanced
            if (chance <= 90) {
                newGem = id;
            } else {
                newGem = id - 1;
            }
        }

        return newGem;
    }

    private static short getGemQuantity(final int gem, final int gemToMake){
        short qty = 1;
        if(gem == gemToMake || gem == gemToMake + 1){ // If you get the same gem or the next one up
            qty = 1;
        }else if(gem == gemToMake - 1){ // If what's chosen is one level down
            qty = 9;
        }
        return qty;
    }



    // checks and prevents hackers from PE'ing Maker operations with invalid operations
    private static boolean removeOddMakerReagents(int toCreate, Map<Integer, Short> reagentids) {
        Map<Integer, Integer> reagentType = new LinkedHashMap<>();
        List<Integer> toRemove = new LinkedList<>();

        boolean isWeapon = ItemConstants.isWeapon(toCreate);  // thanks Vcoc for finding a case where a weapon wouldn't be counted as such due to a bounding on isWeapon

        for (Map.Entry<Integer, Short> r : reagentids.entrySet()) {
            int curRid = r.getKey();
            int type = r.getKey() / 100;

            if (type < 42502 && !isWeapon) {     // only weapons should gain w.att/m.att from these.
                return false;   //toRemove.add(curRid);
            } else {
                Integer tableRid = reagentType.get(type);

                if (tableRid != null) {
                    if (tableRid < curRid) {
                        toRemove.add(tableRid);
                        reagentType.put(type, curRid);
                    } else {
                        toRemove.add(curRid);
                    }
                } else {
                    reagentType.put(type, curRid);
                }
            }
        }

        // removing less effective gems of repeated type
        for (Integer i : toRemove) {
            reagentids.remove(i);
        }

        // only quantity 1 of each gem will be accepted by the Maker skill
        for (Integer i : reagentids.keySet()) {
            reagentids.put(i, (short) 1);
        }

        return true;
    }

    private static int getMakerReagentSlots(int itemId) {
        try {
            int eqpLevel = ii.getEquipLevelReq(itemId);

            if (eqpLevel < 78) {
                return 1;
            } else if (eqpLevel >= 78 && eqpLevel < 108) {
                return 2;
            } else {
                return 3;
            }
        } catch (NullPointerException npe) {
            return 0;
        }
    }

    private static Pair<Integer, List<Pair<Integer, Integer>>> generateDisassemblyInfo(int itemId) {
        int recvFee = ii.getMakerDisassembledFee(itemId);
        if (recvFee > -1) {
            List<Pair<Integer, Integer>> gains = ii.getMakerDisassembledItems(itemId);
            if (!gains.isEmpty()) {
                return new Pair<>(recvFee, gains);
            }
        }

        return null;
    }

    public static int getMakerSkillLevel(MapleCharacter chr) {
        return chr.getSkillLevel((chr.getJob().getId() / 1000) * 10000000 + 1007);
    }

    private static short getCreateStatus(MapleClient c, MakerItemCreateEntry recipe) {
        if (recipe.isInvalid()) {
            return -1;
        }

        if (!hasItems(c, recipe)) {
            return 1;
        }

        if (c.getPlayer().getMeso() < recipe.getCost()) {
            return 2;
        }

        if (c.getPlayer().getLevel() < recipe.getReqLevel()) {
            return 3;
        }

        if (getMakerSkillLevel(c.getPlayer()) < recipe.getReqSkillLevel()) {
            return 4;
        }

        List<Integer> addItemids = new LinkedList<>();
        List<Integer> addQuantity = new LinkedList<>();
        List<Integer> rmvItemids = new LinkedList<>();
        List<Integer> rmvQuantity = new LinkedList<>();

        for (Pair<Integer, Integer> p : recipe.getReqItems()) {
            rmvItemids.add(p.getLeft());
            rmvQuantity.add(p.getRight());
        }

        for (Pair<Integer, Integer> p : recipe.getGainItems()) {
            addItemids.add(p.getLeft());
            addQuantity.add(p.getRight());
        }

        if (!c.getAbstractPlayerInteraction().canHoldAllAfterRemoving(addItemids, addQuantity, rmvItemids, rmvQuantity)) {
            return 5;
        }

        return 0;
    }

    private static boolean hasItems(MapleClient c, MakerItemCreateEntry recipe) {
        for (Pair<Integer, Integer> p : recipe.getReqItems()) {
            int itemId = p.getLeft();
            if (c.getPlayer().getInventory(ItemConstants.getInventoryType(itemId)).countById(itemId) < p.getRight()) {
                return false;
            }
        }
        return true;
    }

    private static boolean addBoostedMakerItem(MapleClient c, int itemid, int stimulantid, Map<Integer, Short> reagentids) {
        if (stimulantid != -1 && !ii.rollSuccessChance(90.0)) {
            return false;
        }

        Item item = ii.getEquipById(itemid);
        if (item == null) return false;

        Equip eqp = (Equip) item;
        if (ItemConstants.isAccessory(item.getItemId()) && eqp.getUpgradeSlots() <= 0) eqp.setUpgradeSlots(3);

        if (stimulantid == -1) ii.randomizeStats(eqp); // no stim - randomize before adding stats

        if (!reagentids.isEmpty()) {
            Map<String, Integer> stats = new LinkedHashMap<>();
            //List<Short> randOption = new LinkedList<>();
            //List<Short> randStat = new LinkedList<>();

            for (Map.Entry<Integer, Short> r : reagentids.entrySet()) {
                Pair<String, Integer> reagentBuff = ii.getMakerReagentStatUpgrade(r.getKey());

                if (reagentBuff != null) {
                    String s = reagentBuff.getLeft();
                    // ori check for randOption and randStat
                    // instead let's add those into ii.improveequipstats()
                   /* if (s.substring(0, 4).contains("rand")) {
                        if (s.substring(4).equals("Stat")) {
                            randStat.add((short) (reagentBuff.getRight() * r.getValue()));
                        } else {
                            randOption.add((short) (reagentBuff.getRight() * r.getValue()));
                        }
                    } else { */
                        String stat = s.substring(3);

                        if (!stat.equals("ReqLevel")) {    // improve req level... really?
                            switch (stat) {
                                case "MaxHP" -> stat = "MHP";
                                case "MaxMP" -> stat = "MMP";
                            }

                            Integer d = stats.get(stat);
                            if (d == null) {
                                stats.put(stat, reagentBuff.getRight() * r.getValue());
                            } else {
                                stats.put(stat, d + (reagentBuff.getRight() * r.getValue()));
                            }
                        }
                    //}
                }
            }

            ii.improveEquipStats(eqp, stats);
        }

        MapleInventoryManipulator.addFromDrop(c, item, false, -1);

        return true;
    }
}
