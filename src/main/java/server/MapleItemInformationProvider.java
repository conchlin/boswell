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
package server;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.HashSet;

import client.*;
import net.server.Server;
import provider.MapleData;
import provider.MapleDataDirectoryEntry;
import provider.MapleDataFileEntry;
import provider.MapleDataProvider;
import provider.MapleDataProviderFactory;
import provider.MapleDataTool;
import server.skills.PlayerSkill;
import server.skills.SkillFactory;
import net.database.DatabaseConnection;
import tools.FilePrinter;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import client.autoban.AutobanFactory;
import client.inventory.Equip;
import client.inventory.Item;
import client.inventory.LevelUpInformation;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.MapleWeaponType;
import constants.EquipSlot;
import constants.ItemConstants;
import constants.skills.Assassin;
import constants.skills.Gunslinger;
import constants.skills.NightWalker;
import java.sql.Connection;
import java.util.stream.IntStream;

import server.MakerItemFactory.MakerItemCreateEntry;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleLifeFactory;
import tools.StringUtil;

/**
 *
 * @author Matze
 *
 */
public class MapleItemInformationProvider {
    private final static MapleItemInformationProvider instance = new MapleItemInformationProvider();

    public static MapleItemInformationProvider getInstance() {
        return instance;
    }

    protected MapleDataProvider itemData;
    protected MapleDataProvider equipData;
    protected MapleDataProvider stringData;
    protected MapleDataProvider etcData;
    protected MapleData cashStringData;
    protected MapleData consumeStringData;
    protected MapleData eqpStringData;
    protected MapleData etcStringData;
    protected MapleData insStringData;
    protected MapleData petStringData;
    protected Map<Integer, Short> slotMaxCache = new HashMap<>();
    protected Map<Integer, MapleStatEffect> itemEffects = new HashMap<>();
    protected Map<Integer, Map<String, Integer>> equipStatsCache = new HashMap<>();
    protected Map<Integer, Equip> equipCache = new HashMap<>();
    protected Map<Integer, MapleData> equipLevelInfoCache = new HashMap<>();
    protected Map<Integer, Integer> equipLevelReqCache = new HashMap<>();
    protected Map<Integer, Integer> equipMaxLevelCache = new HashMap<>();
    protected Map<Integer, Integer> wholePriceCache = new HashMap<>();
    protected Map<Integer, Double> unitPriceCache = new HashMap<>();
    protected Map<Integer, Integer> projectileWatkCache = new HashMap<>();
    protected Map<Integer, String> nameCache = new HashMap<>();
    protected Map<Integer, String> descCache = new HashMap<>();
    protected Map<Integer, String> msgCache = new HashMap<>();
    protected Map<Integer, Boolean> accountItemRestrictionCache = new HashMap<>();
    protected Map<Integer, Boolean> dropRestrictionCache = new HashMap<>();
    protected Map<Integer, Boolean> pickupRestrictionCache = new HashMap<>();
    protected Map<Integer, Integer> getMesoCache = new HashMap<>();
    protected Map<Integer, Integer> monsterBookID = new HashMap<>();
    protected Map<Integer, Boolean> untradeableCache = new HashMap<>();
    protected Map<Integer, Boolean> onEquipUntradeableCache = new HashMap<>();
    protected Map<Integer, ScriptedItem> scriptedItemCache = new HashMap<>();
    protected Map<Integer, Boolean> karmaCache = new HashMap<>();
    protected Map<Integer, Integer> triggerItemCache = new HashMap<>();
    protected Map<Integer, Integer> expCache = new HashMap<>();
    protected Map<Integer, Integer> createItem = new HashMap<>();
    protected Map<Integer, Integer> mobItem = new HashMap<>();
    protected Map<Integer, Integer> useDelay = new HashMap<>();
    protected Map<Integer, Integer> mobHP = new HashMap<>();
    protected Map<Integer, Integer> levelCache = new HashMap<>();
    protected Map<Integer, Pair<Integer, List<RewardItem>>> rewardCache = new HashMap<>();
    protected LinkedHashMap<Integer, String> itemIdAndName = new LinkedHashMap<>();
    protected Map<Integer, Boolean> consumeOnPickupCache = new HashMap<>();
    protected Map<Integer, Boolean> isQuestItemCache = new HashMap<>();
    protected Map<Integer, Boolean> isPartyQuestItemCache = new HashMap<>();
    protected Map<Integer, Pair<Integer, String>> replaceOnExpireCache = new HashMap<>();
    protected Map<Integer, String> equipmentSlotCache = new HashMap<>();
    protected Map<Integer, Boolean> noCancelMouseCache = new HashMap<>();
    protected Map<Integer, Integer> mobCrystalMakerCache = new HashMap<>();
    protected Map<Integer, Pair<String, Integer>> statUpgradeMakerCache = new HashMap<>();
    protected Map<Integer, MakerItemFactory.MakerItemCreateEntry> makerItemCache = new HashMap<>();
    protected Map<Integer, Integer> makerCatalystCache = new HashMap<>();
    protected Map<Integer, Map<String, Integer>> skillUpgradeCache = new HashMap<>();
    protected Map<Integer, MapleData> skillUpgradeInfoCache = new HashMap<>();
    protected Map<Integer, Pair<Integer, Set<Integer>>> cashPetFoodCache = new HashMap<>();
    protected Map<Integer, QuestConsItem> questItemConsCache = new HashMap<>();
    protected Map<Integer, HashMap<Integer, LevelUpInformation>> levelUpCache = new HashMap<>();
    protected Set<Integer> aesthetics = new HashSet<>();

    private MapleItemInformationProvider() {
        loadCardIdData();
        itemData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Item.wz"));
        equipData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Character.wz"));
        stringData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/String.wz"));
        etcData = MapleDataProviderFactory.getDataProvider(new File(System.getProperty("wzpath") + "/Etc.wz"));
        cashStringData = stringData.getData("Cash.img");
        consumeStringData = stringData.getData("Consume.img");
        eqpStringData = stringData.getData("Eqp.img");
        etcStringData = stringData.getData("Etc.img");
        insStringData = stringData.getData("Ins.img");
        petStringData = stringData.getData("Pet.img");

        isQuestItemCache.put(0, false);
        isPartyQuestItemCache.put(0, false);
    }

    public void loadAesthetics() {
        MapleDataDirectoryEntry root = equipData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            if (topDir.getName().equals("Hair") || topDir.getName().equals("Face")) {
                for (MapleDataFileEntry iFile : topDir.getFiles()) {
                    String cosmetic = iFile.getName().split(".img")[0];
                    if (cosmetic != null) {
                        aesthetics.add(Integer.parseInt(cosmetic));
                    }
                }
            }
        }
        for (MapleDataFileEntry iFile : root.getFiles()) {
            String cosmetic = iFile.getName().split(".img")[0];
            if (cosmetic != null) {
                int skin = Integer.parseInt(cosmetic) % 100;
                if (!aesthetics.contains(skin)) {
                    aesthetics.add(skin);
                }
            }
        }
    }

    public boolean aestheticExists(int aestheticId) {
        if (aesthetics == null) {
            return false;
        }
        return aesthetics.contains(aestheticId);
    }


    //improved, used linkedhashmap instead for better performance, and easier and less costly retrieval (no external loops needed)
    public LinkedHashMap<Integer, String> getAllItems() {
        if (!itemIdAndName.isEmpty()) {
            return itemIdAndName;
        }

        MapleData[] itemsData = {
                stringData.getData("Eqp.img").getChildByPath("Eqp"),
                stringData.getData("Cash.img"),
                stringData.getData("Consume.img"),
                stringData.getData("Etc.img").getChildByPath("Etc"),
                stringData.getData("Ins.img"), stringData.getData("Pet.img")
        };

        for (int i = 0; i < itemsData.length; i++) { //get and map all item ids and names via their child paths
            for (MapleData itemFolder : itemsData[i].getChildren()) {
                if (i == 0) { //eqp, needs to traverse its child paths
                    for (MapleData equipItemFolder : itemFolder.getChildren()) {
                        itemIdAndName.put(Integer.parseInt(equipItemFolder.getName()),
                                MapleDataTool.getString("name", equipItemFolder, "NO-NAME"));
                    }
                } else {
                    itemIdAndName.put(Integer.parseInt(itemFolder.getName()),
                            MapleDataTool.getString("name", itemFolder, "NO-NAME"));
                }
            }
        }
        return itemIdAndName;
    }

    private MapleData getStringData(int itemId) {
        String cat = "null";
        MapleData theData;
        if (itemId >= 5010000) {
            theData = cashStringData;
        } else if (itemId >= 2000000 && itemId < 3000000) {
            theData = consumeStringData;
        } else if ((itemId >= 1010000 && itemId < 1040000) || (itemId >= 1122000 && itemId < 1123000) || (itemId >= 1132000 && itemId < 1133000) || (itemId >= 1142000 && itemId < 1143000)) {
            theData = eqpStringData;
            cat = "Eqp/Accessory";
        } else if (itemId >= 1000000 && itemId < 1010000) {
            theData = eqpStringData;
            cat = "Eqp/Cap";
        } else if (itemId >= 1102000 && itemId < 1103000) {
            theData = eqpStringData;
            cat = "Eqp/Cape";
        } else if (itemId >= 1040000 && itemId < 1050000) {
            theData = eqpStringData;
            cat = "Eqp/Coat";
        } else if (itemId >= 20000 && itemId < 22000) {
            theData = eqpStringData;
            cat = "Eqp/Face";
        } else if (itemId >= 1080000 && itemId < 1090000) {
            theData = eqpStringData;
            cat = "Eqp/Glove";
        } else if (itemId >= 30000 && itemId < 49999) {
            theData = eqpStringData;
            cat = "Eqp/Hair";
        } else if (itemId >= 1050000 && itemId < 1060000) {
            theData = eqpStringData;
            cat = "Eqp/Longcoat";
        } else if (itemId >= 1060000 && itemId < 1070000) {
            theData = eqpStringData;
            cat = "Eqp/Pants";
        } else if (itemId >= 1802000 && itemId < 1842000) {
            theData = eqpStringData;
            cat = "Eqp/PetEquip";
        } else if (itemId >= 1112000 && itemId < 1120000) {
            theData = eqpStringData;
            cat = "Eqp/Ring";
        } else if (itemId >= 1092000 && itemId < 1100000) {
            theData = eqpStringData;
            cat = "Eqp/Shield";
        } else if (itemId >= 1070000 && itemId < 1080000) {
            theData = eqpStringData;
            cat = "Eqp/Shoes";
        } else if (itemId >= 1900000 && itemId < 2000000) {
            theData = eqpStringData;
            cat = "Eqp/Taming";
        } else if (itemId >= 1300000 && itemId < 1800000) {
            theData = eqpStringData;
            cat = "Eqp/Weapon";
        } else if (itemId >= 4000000 && itemId < 5000000) {
            theData = etcStringData;
            cat = "Etc";
        } else if (itemId >= 3000000 && itemId < 4000000) {
            theData = insStringData;
        } else if (ItemConstants.isPet(itemId)) {
            theData = petStringData;
        } else {
            return null;
        }
        if (cat.equalsIgnoreCase("null")) {
            return theData.getChildByPath(String.valueOf(itemId));
        } else {
            return theData.getChildByPath(cat + "/" + itemId);
        }
    }

    public boolean noCancelMouse(int itemId) {
        if (noCancelMouseCache.containsKey(itemId)) {
            return noCancelMouseCache.get(itemId);
        }

        MapleData item = getItemData(itemId);
        if (item == null) {
            noCancelMouseCache.put(itemId, false);
            return false;
        }

        boolean blockMouse = MapleDataTool.getIntConvert("info/noCancelMouse", item, 0) == 1;
        noCancelMouseCache.put(itemId, blockMouse);
        return blockMouse;
    }

    private MapleData getItemData(int itemId) {
        MapleData ret = null;
        String idStr = "0" + String.valueOf(itemId);
        MapleDataDirectoryEntry root = itemData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr.substring(0, 4) + ".img")) {
                    ret = itemData.getData(topDir.getName() + "/" + iFile.getName());
                    if (ret == null) {
                        return null;
                    }
                    ret = ret.getChildByPath(idStr);
                    return ret;
                } else if (iFile.getName().equals(idStr.substring(1) + ".img")) {
                    return itemData.getData(topDir.getName() + "/" + iFile.getName());
                }
            }
        }
        root = equipData.getRoot();
        for (MapleDataDirectoryEntry topDir : root.getSubdirectories()) {
            for (MapleDataFileEntry iFile : topDir.getFiles()) {
                if (iFile.getName().equals(idStr + ".img")) {
                    return equipData.getData(topDir.getName() + "/" + iFile.getName());
                }
            }
        }
        return ret;
    }

    public List<Integer> getItemIdsInRange(int minId, int maxId, boolean ignoreCashItem) {
        List<Integer> list = new ArrayList<>();

        if(ignoreCashItem) {
            for(int i = minId; i <= maxId; i++) {
                if(getItemData(i) != null && !isCash(i)) {
                    list.add(i);
                }
            }
        }
        else {
            for(int i = minId; i <= maxId; i++) {
                if(getItemData(i) != null) {
                    list.add(i);
                }
            }
        }


        return list;
    }

    /**
     * Add extra slots for bullets and stars based on mastery level
     * @param c
     * @param itemId
     * @return  slots to add
     */
    private static short getExtraSlotMaxFromPlayer(MapleClient c, int itemId) {
        short ret = 0;
        if (ItemConstants.isThrowingStar(itemId)) {
            if(c.getPlayer().getJob().isA(MapleJob.NIGHTWALKER1)) {
                ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(NightWalker.CLAW_MASTERY)) * 10;
            } else {
                ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(Assassin.CLAW_MASTERY)) * 10;
            }
        } else if (ItemConstants.isBullet(itemId)) {
            ret += c.getPlayer().getSkillLevel(SkillFactory.getSkill(Gunslinger.GUN_MASTERY)) * 10;
        }

        return ret;
    }

    public short getSlotMax(MapleClient c, int itemId) {
        Short slotMax = slotMaxCache.get(itemId);
        if (slotMax != null) {
            return (short)(slotMax + getExtraSlotMaxFromPlayer(c, itemId));
        }

        short ret = 0;
        MapleData item = getItemData(itemId);
        if (item != null) {
            MapleData smEntry = item.getChildByPath("info/slotMax");
            if (smEntry == null) {
                if (ItemConstants.getInventoryType(itemId).getType() == MapleInventoryType.EQUIP.getType()) {
                    ret = 1;
                } else {
                    ret = 1000; // 1000 is default instead of 100
                }
            } else {
                ret = ItemConstants.isRechargeable(itemId) ? (short) MapleDataTool.getInt(smEntry) : 1000;
            }
        }

        slotMaxCache.put(itemId, ret);
        return (short)(ret + getExtraSlotMaxFromPlayer(c, itemId));
    }

    public int getMeso(int itemId) {
        if (getMesoCache.containsKey(itemId)) {
            return getMesoCache.get(itemId);
        }
        MapleData item = getItemData(itemId);
        if (item == null) {
            return -1;
        }
        int pEntry;
        MapleData pData = item.getChildByPath("info/meso");
        if (pData == null) {
            return -1;
        }
        pEntry = MapleDataTool.getInt(pData);
        getMesoCache.put(itemId, pEntry);
        return pEntry;
    }

    private static double getRoundedUnitPrice(double unitPrice, int max) {
        double intPart = Math.floor(unitPrice);
        double fractPart = unitPrice - intPart;
        if(fractPart == 0.0) return intPart;

        double fractMask = 0.0;
        double lastFract, curFract = 1.0;
        int i = 1;

        do {
            lastFract = curFract;
            curFract /= 2;

            if(fractPart == curFract) {
                break;
            } else if(fractPart > curFract) {
                fractMask += curFract;
                fractPart -= curFract;
            }

            i++;
        } while(i <= max);

        if(i > max) {
            lastFract = curFract;
            curFract = 0.0;
        }

        if(Math.abs(fractPart - curFract) < Math.abs(fractPart - lastFract)) {
            return intPart + fractMask + curFract;
        } else {
            return intPart + fractMask + lastFract;
        }
    }

    private Pair<Integer, Double> getItemPriceData(int itemId) {
        MapleData item = getItemData(itemId);
        if (item == null) {
            wholePriceCache.put(itemId, -1);
            unitPriceCache.put(itemId, 0.0);
            return new Pair<>(-1, 0.0);
        }

        int pEntry = -1;
        MapleData pData = item.getChildByPath("info/price");
        if (pData != null) {
            pEntry = MapleDataTool.getInt(pData);
        }

        double fEntry = 0.0f;
        pData = item.getChildByPath("info/unitPrice");
        if (pData != null) {
            try {
                fEntry = getRoundedUnitPrice(MapleDataTool.getDouble(pData), 5);
            } catch (Exception e) {
                fEntry = (double) MapleDataTool.getInt(pData);
            }
        }

        wholePriceCache.put(itemId, pEntry);
        unitPriceCache.put(itemId, fEntry);
        return new Pair<>(pEntry, fEntry);
    }

    public int getWholePrice(int itemId) {
        if (wholePriceCache.containsKey(itemId)) {
            return wholePriceCache.get(itemId);
        }

        return getItemPriceData(itemId).getLeft();
    }

    public double getUnitPrice(int itemId) {
        if (unitPriceCache.containsKey(itemId)) {
            return unitPriceCache.get(itemId);
        }

        return getItemPriceData(itemId).getRight();
    }

    public int getPrice(int itemId, int quantity) {
        int retPrice = getWholePrice(itemId);
        if(retPrice == -1) {
            return -1;
        }

        if(!ItemConstants.isRechargeable(itemId)) {
            retPrice *= quantity;
        } else {
            retPrice += Math.ceil(quantity * getUnitPrice(itemId));
        }

        return retPrice;
    }

    public Pair<Integer, String> getReplaceOnExpire(int itemId) {   // thanks to GabrielSin
        if (replaceOnExpireCache.containsKey(itemId)) {
            return replaceOnExpireCache.get(itemId);
        }

        MapleData data = getItemData(itemId);
        int itemReplacement = MapleDataTool.getInt("info/replace/itemid", data, 0);
        String msg = MapleDataTool.getString("info/replace/msg", data, "");

        Pair<Integer, String> ret = new Pair<>(itemReplacement, msg);
        replaceOnExpireCache.put(itemId, ret);

        return ret;
    }

    protected String getEquipmentSlot(int itemId) {
        if (equipmentSlotCache.containsKey(itemId)) {
            return equipmentSlotCache.get(itemId);
        }

        String ret = "";

        MapleData item = getItemData(itemId);

        if (item == null) {
            return null;
        }

        MapleData info = item.getChildByPath("info");

        if (info == null) {
            return null;
        }

        ret = MapleDataTool.getString("islot", info, "");

        equipmentSlotCache.put(itemId, ret);

        return ret;
    }

    public Map<String, Integer> getEquipStats(int itemId) {
        if (equipStatsCache.containsKey(itemId)) {
            return equipStatsCache.get(itemId);
        }
        Map<String, Integer> ret = new LinkedHashMap<>();
        MapleData item = getItemData(itemId);
        if (item == null) {
            return null;
        }
        MapleData info = item.getChildByPath("info");
        if (info == null) {
            return null;
        }
        info.getChildren().stream().filter(data -> (data.getName().startsWith("inc"))).forEachOrdered(data -> {
            ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
        }); /*else if (data.getName().startsWith("req"))
        ret.put(data.getName(), MapleDataTool.getInt(data.getName(), info, 0));*/
        ret.put("reqJob", MapleDataTool.getInt("reqJob", info, 0));
        ret.put("reqLevel", MapleDataTool.getInt("reqLevel", info, 0));
        ret.put("reqDEX", MapleDataTool.getInt("reqDEX", info, 0));
        ret.put("reqSTR", MapleDataTool.getInt("reqSTR", info, 0));
        ret.put("reqINT", MapleDataTool.getInt("reqINT", info, 0));
        ret.put("reqLUK", MapleDataTool.getInt("reqLUK", info, 0));
        ret.put("reqPOP", MapleDataTool.getInt("reqPOP", info, 0));
        ret.put("cash", MapleDataTool.getInt("cash", info, 0));
        ret.put("tuc", MapleDataTool.getInt("tuc", info, 0));
        ret.put("cursed", MapleDataTool.getInt("cursed", info, 0));
        ret.put("success", MapleDataTool.getInt("success", info, 0));
        ret.put("fs", MapleDataTool.getInt("fs", info, 0));
        ret.put("expireOnLogout", MapleDataTool.getInt("expireOnLogout", info, 0));
        ret.put("level", info.getChildByPath("level") != null ? 1 : 0);
        ret.put("tradeBlock", MapleDataTool.getInt("tradeBlock", info, 0));
        ret.put("only", MapleDataTool.getInt("only", info, 0));
        ret.put("accountSharable", MapleDataTool.getInt("accountSharable", info, 0));
        ret.put("quest", MapleDataTool.getInt("quest", info, 0));
        equipStatsCache.put(itemId, ret);
        return ret;
    }

    public Integer getEquipLevelReq(int itemId) {
        if (equipLevelReqCache.containsKey(itemId)) {
            return equipLevelReqCache.get(itemId);
        }

        int ret = 0;
        MapleData item = getItemData(itemId);
        if (item != null) {
            MapleData info = item.getChildByPath("info");
            if (info != null) {
                ret = MapleDataTool.getInt("reqLevel", info, 0);
            }
        }

        equipLevelReqCache.put(itemId, ret);
        return ret;
    }

    public List<Integer> getScrollReqs(int itemId) {
        List<Integer> ret = new ArrayList<>();
        MapleData data = getItemData(itemId);
        data = data.getChildByPath("req");
        if (data == null) {
            return ret;
        }
        for (MapleData req : data.getChildren()) {
            ret.add(MapleDataTool.getInt(req));
        }
        return ret;
    }

    public MapleWeaponType getWeaponType(int itemId) {
        int cat = (itemId / 10000) % 100;
        MapleWeaponType[] type = {MapleWeaponType.SWORD1H, MapleWeaponType.GENERAL1H_SWING, MapleWeaponType.GENERAL1H_SWING, MapleWeaponType.DAGGER_OTHER, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.WAND, MapleWeaponType.STAFF, MapleWeaponType.NOT_A_WEAPON, MapleWeaponType.SWORD2H, MapleWeaponType.GENERAL2H_SWING, MapleWeaponType.GENERAL2H_SWING, MapleWeaponType.SPEAR_STAB, MapleWeaponType.POLE_ARM_SWING, MapleWeaponType.BOW, MapleWeaponType.CROSSBOW, MapleWeaponType.CLAW, MapleWeaponType.KNUCKLE, MapleWeaponType.GUN};
        if (cat < 30 || cat > 49) {
            return MapleWeaponType.NOT_A_WEAPON;
        }
        return type[cat - 30];
    }

    private static short getShortMaxIfOverflow(int value) {
        return (short) Math.min(Short.MAX_VALUE, value);
    }

    public boolean canUseCleanSlate(Equip nEquip) {
        Map<String, Integer> eqstats = this.getEquipStats(nEquip.getItemId());
        return nEquip.getUpgradeSlots() < (byte) (eqstats.get("tuc") + nEquip.getVicious());  // issue with clean slate found thanks to Masterrulax, vicious added in the check thanks to Crypter (CrypterDEV)
    }

    public Item scrollEquipWithId(Item equip, int scrollId, boolean usingWhiteScroll, int vegaItemId, boolean isGM) {
        if (equip instanceof Equip nEquip) {
            Map<String, Integer> stats = this.getEquipStats(scrollId);

            if (((nEquip.getUpgradeSlots() > 0 || ItemConstants.isCleanSlate(scrollId)) && Math.ceil(Math.random() * 100.0) <= stats.get("success")) || isGM) {
                double prop = (double) stats.get("success");
                switch(vegaItemId) {
                  case 5610000:
                    if (prop == 10.0f) {
                      prop = 30.0f;
                    }
                    break;
                  case 5610001:
                    if (prop == 60.0f) {
                      prop = 90.0f;
                    }
                    break;
                  case 2049100:
                    prop = 100.0f;
                    break;
                }

                int flag = nEquip.getFlag();
                switch (scrollId) {
                    case 2040727:
                        flag |= ItemConstants.SPIKES;
                        nEquip.setFlag((byte) flag);
                        break;
                    case 2041058:
                        flag |= ItemConstants.COLD;
                        nEquip.setFlag((byte) flag);
                        break;
                    case 2049000:
                    case 2049001:
                    case 2049002:
                    case 2049003:
                        if (canUseCleanSlate(nEquip)) {
                            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() + 1));
                        }
                        break;
                    case 2049100:
                    case 2049101:
                    case 2049102:
                        int inc = 1;
                        if (Randomizer.nextInt(2) == 0) {
                            inc = -1;
                        }
                        if (nEquip.getStr() > 0) {
                            nEquip.setStr((short) Math.max(0, (nEquip.getStr() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getDex() > 0) {
                            nEquip.setDex((short) Math.max(0, (nEquip.getDex() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getInt() > 0) {
                            nEquip.setInt((short) Math.max(0, (nEquip.getInt() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getLuk() > 0) {
                            nEquip.setLuk((short) Math.max(0, (nEquip.getLuk() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getWatk() > 0) {
                            nEquip.setWatk((short) Math.max(0, (nEquip.getWatk() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getWdef() > 0) {
                            nEquip.setWdef((short) Math.max(0, (nEquip.getWdef() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getMatk() > 0) {
                            nEquip.setMatk((short) Math.max(0, (nEquip.getMatk() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getMdef() > 0) {
                            nEquip.setMdef((short) Math.max(0, (nEquip.getMdef() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getAcc() > 0) {
                            nEquip.setAcc((short) Math.max(0, (nEquip.getAcc() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getAvoid() > 0) {
                            nEquip.setAvoid((short) Math.max(0, (nEquip.getAvoid() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getSpeed() > 0) {
                            nEquip.setSpeed((short) Math.max(0, (nEquip.getSpeed() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getJump() > 0) {
                            nEquip.setJump((short) Math.max(0, (nEquip.getJump() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getHp() > 0) {
                            nEquip.setHp((short) Math.max(0, (nEquip.getHp() + Randomizer.nextInt(6) * inc)));
                        }
                        if (nEquip.getMp() > 0) {
                            nEquip.setMp((short) Math.max(0, (nEquip.getMp() + Randomizer.nextInt(6) * inc)));
                        }
                        break;
                    case 2049199:
                        if (nEquip.getStr() > 0) {
                            nEquip.setStr((short) Math.max(0, (nEquip.getStr() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getDex() > 0) {
                            nEquip.setDex((short) Math.max(0, (nEquip.getDex() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getInt() > 0) {
                            nEquip.setInt((short) Math.max(0, (nEquip.getInt() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getLuk() > 0) {
                            nEquip.setLuk((short) Math.max(0, (nEquip.getLuk() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getWatk() > 0) {
                            nEquip.setWatk((short) Math.max(0, (nEquip.getWatk() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getWdef() > 0) {
                            nEquip.setWdef((short) Math.max(0, (nEquip.getWdef() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getMatk() > 0) {
                            nEquip.setMatk((short) Math.max(0, (nEquip.getMatk() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getMdef() > 0) {
                            nEquip.setMdef((short) Math.max(0, (nEquip.getMdef() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getAcc() > 0) {
                            nEquip.setAcc((short) Math.max(0, (nEquip.getAcc() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getAvoid() > 0) {
                            nEquip.setAvoid((short) Math.max(0, (nEquip.getAvoid() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getSpeed() > 0) {
                            nEquip.setSpeed((short) Math.max(0, (nEquip.getSpeed() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getJump() > 0) {
                            nEquip.setJump((short) Math.max(0, (nEquip.getJump() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getHp() > 0) {
                            nEquip.setHp((short) Math.max(0, (nEquip.getHp() + Randomizer.nextInt(6))));
                        }
                        if (nEquip.getMp() > 0) {
                            nEquip.setMp((short) Math.max(0, (nEquip.getMp() + Randomizer.nextInt(6))));
                        }
                        break;
                default:
                    improveEquipStats(nEquip, stats);
                    break;
                }
                    if (!ItemConstants.isCleanSlate(scrollId)) {
                        if (!isGM && !ItemConstants.isModifierScroll(scrollId)) {   // issue with modifier scrolls taking slots found thanks to Masterrulax, justin, BakaKnyx
                            nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                        }
                        nEquip.setLevel((byte) (nEquip.getLevel() + 1));
                    }
                } else {
                    if (!usingWhiteScroll && !ItemConstants.isCleanSlate(scrollId) && !isGM && !ItemConstants.isModifierScroll(scrollId)) {
                        nEquip.setUpgradeSlots((byte) (nEquip.getUpgradeSlots() - 1));
                    }
                    if (Randomizer.nextInt(100) < stats.get("cursed")) {
                        return null;
                    }
                }
            }
        return equip;
    }

    public static void improveEquipStats(Equip nEquip, Map<String, Integer> stats) {
        for (Entry<String, Integer> stat : stats.entrySet()) {
            switch (stat.getKey()) {
                case "STR":
                    nEquip.setStr(getShortMaxIfOverflow(nEquip.getStr() + stat.getValue().intValue()));
                    break;
                case "DEX":
                    nEquip.setDex(getShortMaxIfOverflow(nEquip.getDex() + stat.getValue().intValue()));
                    break;
                case "INT":
                    nEquip.setInt(getShortMaxIfOverflow(nEquip.getInt() + stat.getValue().intValue()));
                    break;
                case "LUK":
                    nEquip.setLuk(getShortMaxIfOverflow(nEquip.getLuk() + stat.getValue().intValue()));
                    break;
                case "PAD":
                    nEquip.setWatk(getShortMaxIfOverflow(nEquip.getWatk() + stat.getValue().intValue()));
                    break;
                case "PDD":
                    nEquip.setWdef(getShortMaxIfOverflow(nEquip.getWdef() + stat.getValue().intValue()));
                    break;
                case "MAD":
                    nEquip.setMatk(getShortMaxIfOverflow(nEquip.getMatk() + stat.getValue().intValue()));
                    break;
                case "MDD":
                    nEquip.setMdef(getShortMaxIfOverflow(nEquip.getMdef() + stat.getValue().intValue()));
                    break;
                case "ACC":
                    nEquip.setAcc(getShortMaxIfOverflow(nEquip.getAcc() + stat.getValue().intValue()));
                    break;
                case "EVA":
                    nEquip.setAvoid(getShortMaxIfOverflow(nEquip.getAvoid() + stat.getValue().intValue()));
                    break;
                case "Speed":
                    nEquip.setSpeed(getShortMaxIfOverflow(nEquip.getSpeed() + stat.getValue().intValue()));
                    break;
                case "Jump":
                    nEquip.setJump(getShortMaxIfOverflow(nEquip.getJump() + stat.getValue().intValue()));
                    break;
                case "MHP":
                    nEquip.setHp(getShortMaxIfOverflow(nEquip.getHp() + stat.getValue().intValue()));
                    break;
                case "MMP":
                    nEquip.setMp(getShortMaxIfOverflow(nEquip.getMp() + stat.getValue().intValue()));
                    break;
                case "dOption": // Black Crystal, +/- Random, (1, 2, 3) (Atk, M. Atk, Speed, or Jump)
                    final int ma = nEquip.getMatk(), wa = nEquip.getWatk(), speed = nEquip.getSpeed(), jump = nEquip.getJump();
                    if(wa > 0){
                        nEquip.setWatk(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (wa + stat.getValue().intValue()) : (wa - stat.getValue().intValue())));
                    }
                    if(ma > 0){
                        nEquip.setMatk(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (ma + stat.getValue().intValue()) : (ma - stat.getValue().intValue())));
                    }
                    if(speed > 0){
                        nEquip.setSpeed(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (speed + stat.getValue().intValue()) : (speed - stat.getValue().intValue())));
                    }
                    if(jump > 0){
                        nEquip.setJump(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (jump + stat.getValue().intValue()) : (jump - stat.getValue().intValue())));
                    }
                    break;
                case "dStat": // Dark Crystal, +/- Random, (2, 3, 5) (Str, Dex, Int, Luk, Accuracy, or Avoid)
                    final int str1 = nEquip.getStr(), dex1 = nEquip.getDex(), luk1 = nEquip.getLuk(), int_1 = nEquip.getInt(),
                            acc = nEquip.getAcc(), avoid = nEquip.getAvoid();
                    if(str1 > 0){
                        nEquip.setStr(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (str1 + stat.getValue().intValue()) : (str1 - stat.getValue().intValue())));
                    }
                    if(dex1 > 0){
                        nEquip.setDex(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (dex1 + stat.getValue().intValue()) : (dex1 - stat.getValue().intValue())));
                    }
                    if(int_1 > 0){
                        nEquip.setInt(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (int_1 + stat.getValue().intValue()) : (int_1 - stat.getValue().intValue())));
                    }
                    if(luk1 > 0){
                        nEquip.setLuk(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (luk1 + stat.getValue().intValue()) : (luk1 - stat.getValue().intValue())));
                    }
                    if(acc > 0){
                        nEquip.setAcc(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (acc + stat.getValue().intValue()) : (acc - stat.getValue().intValue())));
                    }
                    if(avoid > 0){
                        nEquip.setAvoid(getShortMaxIfOverflow(Randomizer.nextBoolean() ? (avoid + stat.getValue().intValue()) : (avoid - stat.getValue().intValue())));
                    }
                    break;
                case "afterImage":
                    break;
            }
        }
    }

    public Item getEquipById(int equipId) {
        return getEquipById(equipId, -1);
    }

    // static item
    Item getEquipById(int equipId, int ringId) {
        Equip nEquip;
        nEquip = new Equip(equipId, (byte) 0, ringId);
        nEquip.setQuantity((short) 1);
        Map<String, Integer> stats = this.getEquipStats(equipId);
        if (stats != null) {
            for (Entry<String, Integer> stat : stats.entrySet()) {
                if (stat.getKey().equals("STR")) {
                    nEquip.setStr((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("DEX")) {
                    nEquip.setDex((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("INT")) {
                    nEquip.setInt((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("LUK")) {
                    nEquip.setLuk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("PAD")) {
                    nEquip.setWatk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("PDD")) {
                    nEquip.setWdef((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MAD")) {
                    nEquip.setMatk((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MDD")) {
                    nEquip.setMdef((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("ACC")) {
                    nEquip.setAcc((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("EVA")) {
                    nEquip.setAvoid((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("Speed")) {
                    nEquip.setSpeed((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("Jump")) {
                    nEquip.setJump((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MHP")) {
                    nEquip.setHp((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("MMP")) {
                    nEquip.setMp((short) stat.getValue().intValue());
                } else if (stat.getKey().equals("tuc")) {
                    nEquip.setUpgradeSlots((byte) stat.getValue().intValue());
                } else if (isUntradeableRestricted(equipId)) {  // thanks Hyun & Thora for showing an issue with more than only "Untradeable" items being flagged as such here
                    int flag = nEquip.getFlag();
                    flag |= ItemConstants.UNTRADEABLE;
                    nEquip.setFlag(flag);
                } else if (stats.get("fs") > 0) {
                    int flag = nEquip.getFlag();
                    flag |= ItemConstants.SPIKES;
                    nEquip.setFlag(flag);
                    equipCache.put(equipId, nEquip);
                }
            }
        }
        return nEquip.copy();
    }

    public static short getRandStat(short defaultValue, int maxRange) {
        if (defaultValue == 0) {
            return 0;
        }
        int lMaxRange = (int) Math.min(Math.ceil(defaultValue * 0.1), maxRange);
        return (short) ((defaultValue - lMaxRange) + Math.floor(Randomizer.nextDouble() * (lMaxRange * 2 + 1)));
    }

    //default, all the equip stats.
    public Equip randomizeStats(Equip equip) {
        MapleStat[] stats = {
                MapleStat.INT,
                MapleStat.STR,
                MapleStat.LUK,
                MapleStat.DEX,
                MapleStat.HP,
                MapleStat.MP};

        MapleBuffStat[] buffStats = {
                MapleBuffStat.WATK,
                MapleBuffStat.MATK,
                MapleBuffStat.ACC,
                MapleBuffStat.AVOID,
                MapleBuffStat.SPEED,
                MapleBuffStat.JUMP,
                MapleBuffStat.MDEF,
                MapleBuffStat.WDEF};

        return randomizeStats(equip, stats, buffStats);
    }

    public Equip randomizeStats(Equip equip, MapleStat[] stats, MapleBuffStat[] buffStats) {
        equip = randomizeStats(equip, stats);
        equip = randomizeStats(equip, buffStats);
        return equip;
    }

    public Equip randomizeStats(Equip equip, MapleStat[] stats) {
        for (MapleStat stat : stats) {
            switch (stat) {
                case HP, MP -> equip.setStat(stat, getRandStat(equip.getStat(stat), 10));
                default -> equip.setStat(stat, getRandStat(equip.getStat(stat), 5));
            }
        }
        return equip;
    }

    public Equip randomizeStats(Equip equip, MapleBuffStat[] buffStats) {
        for (MapleBuffStat buffStat : buffStats) {
            switch (buffStat) {
                case WDEF, MDEF -> equip.setStat(buffStat, getRandStat(equip.getStat(buffStat), 10));
                default -> equip.setStat(buffStat, getRandStat(equip.getStat(buffStat), 5));
            }
        }
        return equip;
    }

    public Equip addGodlyStats(Equip equip) {
        if (equip.getStr() != 0)
            equip.setStr((short) (equip.getStr() + Math.random() * 5 + 1));
        if (equip.getLuk() != 0)
            equip.setLuk((short) (equip.getLuk() + Math.random() * 5 + 1));
        if (equip.getInt() != 0)
            equip.setInt((short) (equip.getInt() + Math.random() * 5 + 1));
        if (equip.getDex() != 0)
            equip.setDex((short) (equip.getDex() + Math.random() * 5 + 1));
        if (equip.getMatk() != 0)
            equip.setMatk((short) (equip.getMatk() + Math.random() * 5 + 1));
        if (equip.getWatk() != 0)
            equip.setWatk((short) (equip.getWatk() + Math.random() * 5 + 1));
        if (equip.getAcc() != 0)
            equip.setAcc((short) (equip.getAcc() + Math.random() * 5 + 1));
        if (equip.getAvoid() != 0)
            equip.setAvoid((short) (equip.getAvoid() + Math.random() * 5 + 1));
        if (equip.getHp() != 0)
            equip.setHp((short) (equip.getHp() + Math.random() * 5 + 1));
        if (equip.getJump() != 0)
            equip.setJump((short) (equip.getJump() + Math.random() * 5 + 1));
        if (equip.getSpeed() != 0)
            equip.setSpeed((short) (equip.getSpeed() + Math.random() * 5 + 1));
        if (equip.getMdef() != 0)
            equip.setMdef((short) (equip.getMdef() + Math.random() * 5 + 1));
        if (equip.getWdef() != 0) // hp and mp have a max 10 range instead of 5
            equip.setWdef((short) (equip.getWdef() + Math.random() * 10 + 1));
        if (equip.getMp() != 0)
            equip.setMp((short) (equip.getMp() + Math.random() * 10 + 1));;
        return equip;
    }

    public boolean isGodlyRestrictedItem(int id) {
      int[] restricted = {};
      boolean contains = IntStream.of(restricted).anyMatch(x -> x == id);

      if (contains) return true;
      return false;
    }

    public MapleStatEffect getItemEffect(int itemId) {
        MapleStatEffect ret = itemEffects.get(Integer.valueOf(itemId));
        if (ret == null) {
            MapleData item = getItemData(itemId);
            if (item == null) {
                return null;
            }
            MapleData spec = item.getChildByPath("specEx");
            if (spec == null) {
                spec = item.getChildByPath("spec");
            }
            ret = MapleStatEffect.loadItemEffectFromData(spec, itemId);
            itemEffects.put(Integer.valueOf(itemId), ret);
        }
        return ret;
    }

    public int[][] getSummonMobs(int itemId) {
        MapleData data = getItemData(itemId);
        int theInt = data.getChildByPath("mob").getChildren().size();
        int[][] mobs2spawn = new int[theInt][2];
        for (int x = 0; x < theInt; x++) {
            mobs2spawn[x][0] = MapleDataTool.getIntConvert("mob/" + x + "/id", data);
            mobs2spawn[x][1] = MapleDataTool.getIntConvert("mob/" + x + "/prob", data);
        }
        return mobs2spawn;
    }

    public int getWatkForProjectile(int itemId) {
        Integer atk = projectileWatkCache.get(itemId);
        if (atk != null) {
            return atk.intValue();
        }
        MapleData data = getItemData(itemId);
        atk = Integer.valueOf(MapleDataTool.getInt("info/incPAD", data, 0));
        projectileWatkCache.put(itemId, atk);
        return atk.intValue();
    }

    //since getAllItems has changed thus we have static itemIdAndName and it's initialized on server startup
    //plus the old one cannot get etc names, something to do with not traversing the child paths
    public String getName(int itemId) {
        if (itemIdAndName.isEmpty()) {
            getAllItems();
        }
        return itemIdAndName.get(itemId);
    }

    // safety first
    public boolean isItemValid(int itemId) {
        if (itemId / 1000000 < 1)
            return false;
        getAllItems();
        return itemIdAndName.containsKey(itemId);
    }

    public String getMsg(int itemId) {
        if (msgCache.containsKey(itemId)) {
            return msgCache.get(itemId);
        }
        MapleData strings = getStringData(itemId);
        if (strings == null) {
            return null;
        }
        String ret = MapleDataTool.getString("msg", strings, null);
        msgCache.put(itemId, ret);
        return ret;
    }

    public boolean isUntradeableRestricted(int itemId) {
        if (untradeableCache.containsKey(itemId)) {
            return untradeableCache.get(itemId);
        }

        boolean bRestricted = false;
        if(itemId != 0) {
            MapleData data = getItemData(itemId);
            if (data != null) {
                bRestricted = MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1;
            }
        }

        untradeableCache.put(itemId, bRestricted);
        return bRestricted;
    }

    public boolean isLootRestricted(int itemId) {
        if (dropRestrictionCache.containsKey(itemId)) {
            return dropRestrictionCache.get(itemId);
        }

        boolean bRestricted = false;
        if(itemId != 0) {
            MapleData data = getItemData(itemId);
            if (data != null) {
                bRestricted = MapleDataTool.getIntConvert("info/tradeBlock", data, 0) == 1;
                if (!bRestricted) {
                    bRestricted = MapleDataTool.getIntConvert("info/accountSharable", data, 0) == 1;
                }
            }
        }

        dropRestrictionCache.put(itemId, bRestricted);
        return bRestricted;
    }

    public boolean isDropRestricted(int itemId) {
        return isLootRestricted(itemId) || isQuestItem(itemId);
    }

    public boolean isPickupRestricted(int itemId) {
        if (pickupRestrictionCache.containsKey(itemId)) {
            return pickupRestrictionCache.get(itemId);
        }

        boolean bRestricted = false;
        if(itemId != 0) {
            MapleData data = getItemData(itemId);
            if (data != null) {
                bRestricted = MapleDataTool.getIntConvert("info/only", data, 0) == 1;
            }
        }

        pickupRestrictionCache.put(itemId, bRestricted);
        return bRestricted;
    }

    private Pair<Map<String, Integer>, MapleData> getSkillStatsInternal(int itemId) {
        Map<String, Integer> ret = skillUpgradeCache.get(itemId);
        MapleData retSkill = skillUpgradeInfoCache.get(itemId);

        if(ret != null) return new Pair<>(ret, retSkill);

        retSkill = null;
        ret = new LinkedHashMap<>();
        MapleData item = getItemData(itemId);
        if (item != null) {
            MapleData info = item.getChildByPath("info");
            if (info != null) {
                for (MapleData data : info.getChildren()) {
                    if (data.getName().startsWith("inc")) {
                        ret.put(data.getName().substring(3), MapleDataTool.getIntConvert(data));
                    }
                }
                ret.put("masterLevel", MapleDataTool.getInt("masterLevel", info, 0));
                ret.put("reqSkillLevel", MapleDataTool.getInt("reqSkillLevel", info, 0));
                ret.put("success", MapleDataTool.getInt("success", info, 0));

                retSkill = info.getChildByPath("skill");
            }
        }

        skillUpgradeCache.put(itemId, ret);
        skillUpgradeInfoCache.put(itemId, retSkill);
        return new Pair<>(ret, retSkill);
    }

    public Map<String, Integer> getSkillStats(int itemId, double playerJob) {
        Pair<Map<String, Integer>, MapleData> retData = getSkillStatsInternal(itemId);
        if(retData.getLeft().isEmpty()) return null;

        Map<String, Integer> ret = new LinkedHashMap<>(retData.getLeft());
        MapleData skill = retData.getRight();
        int curskill;
        for (int i = 0; i < skill.getChildren().size(); i++) {
            curskill = MapleDataTool.getInt(Integer.toString(i), skill, 0);
            if (curskill == 0) {
                break;
            }
            if (curskill / 10000 == playerJob) {
                ret.put("skillid", curskill);
                break;
            }
        }
        if (ret.get("skillid") == null) {
            ret.put("skillid", 0);
        }
        return ret;
    }

    public Pair<Integer, Boolean> canPetConsume(Integer petId, Integer itemId) {
        Pair<Integer, Set<Integer>> foodData = cashPetFoodCache.get(itemId);

        if(foodData == null) {
            Set<Integer> pets = new HashSet<>(4);
            int inc = 1;

            MapleData data = getItemData(itemId);
            if(data != null) {
                MapleData specData = data.getChildByPath("spec");
                for(MapleData specItem : specData.getChildren()) {
                    String itemName = specItem.getName();

                    try {
                        Integer.parseInt(itemName); // check if it's a petid node

                        Integer petid = MapleDataTool.getInt(specItem, 0);
                        pets.add(petid);
                    } catch(NumberFormatException npe) {
                        if(itemName.contentEquals("inc")) {
                            inc = MapleDataTool.getInt(specItem, 1);
                        }
                    }
                }
            }

            foodData = new Pair<>(inc, pets);
            cashPetFoodCache.put(itemId, foodData);
        }

        return new Pair<>(foodData.getLeft(), foodData.getRight().contains(petId));
    }

    public boolean isQuestItem(int itemId) {
        if (isQuestItemCache.containsKey(itemId)) {
            return isQuestItemCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean questItem = (data != null && MapleDataTool.getIntConvert("info/quest", data, 0) == 1);
        isQuestItemCache.put(itemId, questItem);
        return questItem;
    }

    public boolean isPartyQuestItem(int itemId) {
        if (isPartyQuestItemCache.containsKey(itemId)) {
            return isPartyQuestItemCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean partyquestItem = (data != null && MapleDataTool.getIntConvert("info/pquest", data, 0) == 1);
        isPartyQuestItemCache.put(itemId, partyquestItem);
        return partyquestItem;
    }

    private void loadCardIdData() {
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT cardid, mobid FROM monster_card_data")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        monsterBookID.put(rs.getInt(1), rs.getInt(2));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getCardMobId(int id) {
        return monsterBookID.get(id);
    }

    public boolean isUntradeableOnEquip(int itemId) {
        if (onEquipUntradeableCache.containsKey(itemId)) {
            return onEquipUntradeableCache.get(itemId);
        }
        boolean untradeableOnEquip = MapleDataTool.getIntConvert("info/equipTradeBlock", getItemData(itemId), 0) > 0;
        onEquipUntradeableCache.put(itemId, untradeableOnEquip);
        return untradeableOnEquip;
    }

    public ScriptedItem getScriptedItemInfo(int itemId) {
        if (scriptedItemCache.containsKey(itemId)) {
            return scriptedItemCache.get(itemId);
        }
        if ((itemId / 10000) != 243) {
            return null;
        }
        MapleData itemInfo = getItemData(itemId);
        ScriptedItem script = new ScriptedItem(MapleDataTool.getInt("spec/npc", itemInfo, 0),
        MapleDataTool.getString("spec/script", itemInfo, ""),
        MapleDataTool.getInt("spec/runOnPickup", itemInfo, 0) == 1);
        scriptedItemCache.put(itemId, script);
        return scriptedItemCache.get(itemId);
    }

    public boolean isKarmaAble(int itemId) {
        if (karmaCache.containsKey(itemId)) {
            return karmaCache.get(itemId);
        }
        boolean bRestricted = MapleDataTool.getIntConvert("info/tradeAvailable", getItemData(itemId), 0) > 0;
        karmaCache.put(itemId, bRestricted);
        return bRestricted;
    }

    public int getStateChangeItem(int itemId) {
        if (triggerItemCache.containsKey(itemId)) {
            return triggerItemCache.get(itemId);
        } else {
            int triggerItem = MapleDataTool.getIntConvert("info/stateChangeItem", getItemData(itemId), 0);
            triggerItemCache.put(itemId, triggerItem);
            return triggerItem;
        }
    }

    public int getCreateItem(int itemId) {
        if (createItem.containsKey(itemId)) {
            return createItem.get(itemId);
        } else {
            int itemFrom = MapleDataTool.getIntConvert("info/create", getItemData(itemId), 0);
            createItem.put(itemId, itemFrom);
            return itemFrom;
        }
    }

    public int getMobItem(int itemId) {
        if (mobItem.containsKey(itemId)) {
            return mobItem.get(itemId);
        } else {
            int mobItemCatch = MapleDataTool.getIntConvert("info/mob", getItemData(itemId), 0);
            mobItem.put(itemId, mobItemCatch);
            return mobItemCatch;
        }
    }

    public int getUseDelay(int itemId) {
        if (useDelay.containsKey(itemId)) {
            return useDelay.get(itemId);
        } else {
            int mobUseDelay = MapleDataTool.getIntConvert("info/useDelay", getItemData(itemId), 0);
            useDelay.put(itemId, mobUseDelay);
            return mobUseDelay;
        }
    }

    public int getMobHP(int itemId) {
        if (mobHP.containsKey(itemId)) {
            return mobHP.get(itemId);
        } else {
            int mobHPItem = MapleDataTool.getIntConvert("info/mobHP", getItemData(itemId), 0);
            mobHP.put(itemId, mobHPItem);
            return mobHPItem;
        }
    }

    public int getExpById(int itemId) {
        if (expCache.containsKey(itemId)) {
            return expCache.get(itemId);
        } else {
            int exp = MapleDataTool.getIntConvert("spec/exp", getItemData(itemId), 0);
            expCache.put(itemId, exp);
            return exp;
        }
    }

    public int getMaxLevelById(int itemId) {
        if (levelCache.containsKey(itemId)) {
            return levelCache.get(itemId);
        } else {
            int level = MapleDataTool.getIntConvert("info/maxLevel", getItemData(itemId), 256);
            levelCache.put(itemId, level);
            return level;
        }
    }

    public Pair<Integer, List<RewardItem>> getItemReward(int itemId) {//Thanks Celino, used some stuffs :)
        if (rewardCache.containsKey(itemId)) {
            return rewardCache.get(itemId);
        }
        int totalprob = 0;
        List<RewardItem> rewards = new ArrayList<>();
        for (MapleData child : getItemData(itemId).getChildByPath("reward").getChildren()) {
            RewardItem reward = new RewardItem();
            reward.itemid = MapleDataTool.getInt("item", child, 0);
            reward.prob = (byte) MapleDataTool.getInt("prob", child, 0);
            reward.quantity = (short) MapleDataTool.getInt("count", child, 0);
            reward.effect = MapleDataTool.getString("Effect", child, "");
            reward.worldmsg = MapleDataTool.getString("worldMsg", child, null);
            reward.period = MapleDataTool.getInt("period", child, -1);

            totalprob += reward.prob;

            rewards.add(reward);
        }
        Pair<Integer, List<RewardItem>> hmm = new Pair<>(totalprob, rewards);
        rewardCache.put(itemId, hmm);
        return hmm;
    }

    public boolean isConsumeOnPickup(int itemId) {
        if (consumeOnPickupCache.containsKey(itemId)) {
            return consumeOnPickupCache.get(itemId);
        }
        MapleData data = getItemData(itemId);
        boolean consume = MapleDataTool.getIntConvert("spec/consumeOnPickup", data, 0) == 1 || MapleDataTool.getIntConvert("specEx/consumeOnPickup", data, 0) == 1;
        consumeOnPickupCache.put(itemId, consume);
        return consume;
    }

    public int getPetLife(int petId) {
        MapleData data = itemData.getData("Pet/" + petId + ".img").getChildByPath("info").getChildByPath("life");
        if (data == null) {
            return 90;
        }
        return MapleDataTool.getInt(data);
    }

    public final boolean isTwoHanded(int itemId) {
        return switch (getWeaponType(itemId)) {
            case GENERAL2H_SWING, BOW, CLAW, CROSSBOW, POLE_ARM_SWING, SPEAR_STAB, SWORD2H, GUN, KNUCKLE -> true;
            default -> false;
        };
    }

    public boolean isCash(int itemId) {
        int itemType = itemId / 1000000;
        if (itemType == 5) return true;
        if (itemType != 1) return false;

        Map<String, Integer> eqpStats = getEquipStats(itemId);
        return eqpStats != null && eqpStats.get("cash") == 1;
    }

    public boolean isUpgradeable(int itemId) {
        Item it = this.getEquipById(itemId);
        Equip eq = (Equip)it;

        return (eq.getUpgradeSlots() > 0 || eq.getStr() > 0 || eq.getDex() > 0 || eq.getInt() > 0 || eq.getLuk() > 0 ||
                eq.getWatk() > 0 || eq.getMatk() > 0 || eq.getWdef() > 0 || eq.getMdef() > 0 || eq.getAcc() > 0 ||
                eq.getAvoid() > 0 || eq.getSpeed() > 0 || eq.getJump() > 0 || eq.getHp() > 0 || eq.getMp() > 0);
    }

    public Collection<Item> canWearEquipment(MapleCharacter chr, Collection<Item> items) {
        MapleInventory inv = chr.getInventory(MapleInventoryType.EQUIPPED);
        if (inv.checked()) {
            return items;
        }
        Collection<Item> itemz = new LinkedList<>();
        if (chr.getJob() == MapleJob.SUPERGM || chr.getJob() == MapleJob.GM) {
            for (Item item : items) {
                Equip equip = (Equip) item;
                equip.wear(true);
                itemz.add(item);
            }
            return itemz;
        }
        boolean highfivestamp = false;
        /* Removed because players shouldn't even get this, and gm's should just be gm job.
         try {
         for (Pair<Item, MapleInventoryType> ii : ItemFactory.INVENTORY.loadItems(chr.getId(), false)) {
         if (ii.getRight() == MapleInventoryType.CASH) {
         if (ii.getLeft().getItemId() == 5590000) {
         highfivestamp = true;
         }
         }
         }
         } catch (SQLException ex) {
            ex.printStackTrace();
         }*/
        int tdex = chr.getDex(), tstr = chr.getStr(), tint = chr.getInt(), tluk = chr.getLuk(), fame = chr.getFame();
        if (chr.getJob() != MapleJob.SUPERGM || chr.getJob() != MapleJob.GM) {
            for (Item item : inv.list()) {
                Equip equip = (Equip) item;
                tdex += equip.getDex();
                tstr += equip.getStr();
                tluk += equip.getLuk();
                tint += equip.getInt();
            }
        }
        for (Item item : items) {
            Equip equip = (Equip) item;
            int reqLevel = getEquipLevelReq(equip.getItemId());
            if (highfivestamp) {
                reqLevel -= 5;
                if (reqLevel < 0) {
                    reqLevel = 0;
                }
            }
            /*
             int reqJob = getEquipStats(equip.getItemId()).get("reqJob");
             if (reqJob != 0) {
             Really hard check, and not really needed in this one
             Gm's should just be GM job, and players cannot change jobs.
             }*/
            if (reqLevel > chr.getLevel()) {
                continue;
            } else if (getEquipStats(equip.getItemId()).get("reqDEX") > tdex) {
                continue;
            } else if (getEquipStats(equip.getItemId()).get("reqSTR") > tstr) {
                continue;
            } else if (getEquipStats(equip.getItemId()).get("reqLUK") > tluk) {
                continue;
            } else if (getEquipStats(equip.getItemId()).get("reqINT") > tint) {
                continue;
            }
            int reqPOP = getEquipStats(equip.getItemId()).get("reqPOP");
            if (reqPOP > 0) {
                if (getEquipStats(equip.getItemId()).get("reqPOP") > fame) {
                    continue;
                }
            }
            equip.wear(true);
            itemz.add(equip);
        }
        inv.checked(true);
        return itemz;
    }

    public boolean canWearEquipment(MapleCharacter chr, Equip equip, int dst) {
        int id = equip.getItemId();

        if(ItemConstants.isWeddingRing(id) && chr.hasJustMarried()) {
            chr.dropMessage(5, "The Wedding Ring cannot be equipped on this map.");  // will dc everyone due to doubled couple effect
            return false;
        }

        if (id == 1142998) { // special case for crusader codex
            equip.wear(true);
            return true;
        }
        String islot = getEquipmentSlot(id);
        if (!EquipSlot.getFromTextSlot(islot).isAllowed(dst, isCash(id))) {
            equip.wear(false);
            String itemName = MapleItemInformationProvider.getInstance().getName(equip.getItemId());
            Server.getInstance().broadcastGMMessage(chr.getWorld(), MaplePacketCreator.sendYellowTip("[Warning]: " + chr.getName() + " tried to equip " + itemName + " into slot " + dst + "."));
            AutobanFactory.PACKET_EDIT.alert(chr, chr.getName() + " tried to forcibly equip an item.");
            AutobanFactory.PACKET_EDIT.autoban(chr, chr.getName() + " tried to forcibly equip an item.");
            FilePrinter.printError(FilePrinter.EXPLOITS + chr.getName() + ".txt", chr.getName() + " tried to equip " + itemName + " into " + dst + " slot.");
            return false;
        }

        if (chr.getJob() == MapleJob.SUPERGM || chr.getJob() == MapleJob.GM) {
            equip.wear(true);
            return true;
        }

        boolean highfivestamp = false;
        /* Removed check above for message ><
         try {
         for (Pair<Item, MapleInventoryType> ii : ItemFactory.INVENTORY.loadItems(chr.getId(), false)) {
         if (ii.getRight() == MapleInventoryType.CASH) {
         if (ii.getLeft().getItemId() == 5590000) {
         highfivestamp = true;
         }
         }
         }
         } catch (SQLException ex) {
            ex.printStackTrace();
         }*/

        int reqLevel = getEquipLevelReq(equip.getItemId());
        if (highfivestamp) {
            reqLevel -= 5;
        }
        int i = 0; //lol xD
        //Removed job check. Shouldn't really be needed.
        if (reqLevel > chr.getLevel()) {
            i++;
        } else if (getEquipStats(equip.getItemId()).get("reqDEX") > chr.getTotalDex()) {
            i++;
        } else if (getEquipStats(equip.getItemId()).get("reqSTR") > chr.getTotalStr()) {
            i++;
        } else if (getEquipStats(equip.getItemId()).get("reqLUK") > chr.getTotalLuk()) {
            i++;
        } else if (getEquipStats(equip.getItemId()).get("reqINT") > chr.getTotalInt()) {
            i++;
        }
        int reqPOP = getEquipStats(equip.getItemId()).get("reqPOP");
        if (reqPOP > 0) {
            if (getEquipStats(equip.getItemId()).get("reqPOP") > chr.getFame()) {
                i++;
            }
        }

        if (i > 0) {
            equip.wear(false);
            return false;
        }
        equip.wear(true);
        return true;
    }

    public ArrayList<Pair<Integer, String>> getItemDataByName(String name) {
        ArrayList<Pair<Integer, String>> ret = new ArrayList<>();
        name = name.toLowerCase();

        for (Entry<Integer, String> itemEntry : itemIdAndName.entrySet()) {
            if (itemEntry.getValue().toLowerCase().contains(name)) {
                ret.add(new Pair<>(itemEntry.getKey(), itemEntry.getValue()));
            }
        }
        return ret;
    }

    private MapleData getEquipLevelInfo(int itemId) {
        MapleData equipLevelData = equipLevelInfoCache.get(itemId);
        if (equipLevelData == null) {
            if (equipLevelInfoCache.containsKey(itemId)) return null;

            MapleData iData = getItemData(itemId);
            if (iData != null) {
                MapleData data = iData.getChildByPath("info/level");
                if (data != null) {
                    equipLevelData = data.getChildByPath("info");
                }
            }

            equipLevelInfoCache.put(itemId, equipLevelData);
        }

        return equipLevelData;
    }

    public int getEquipLevel(int itemId, boolean getMaxLevel) {
        Integer eqLevel = equipMaxLevelCache.get(itemId);
        if (eqLevel == null) {
            eqLevel = 1;    // greater than 1 means that it was supposed to levelup on GMS

            MapleData data = getEquipLevelInfo(itemId);
            if (data != null) {
                if (getMaxLevel) {
                    int curLevel = 1;

                    while (true) {
                        MapleData data2 = data.getChildByPath(Integer.toString(curLevel));
                        if (data2 == null || data2.getChildren().size() <= 1) {
                            eqLevel = curLevel;
                            equipMaxLevelCache.put(itemId, eqLevel);
                            break;
                        }

                        curLevel++;
                    }
                } else {
                    MapleData data2 = data.getChildByPath("1");
                    if (data2 != null && data2.getChildren().size() > 1) {
                        eqLevel = 2;
                    }
                }
            }
        }

        return eqLevel;
    }
    
    public boolean canLevelUp(int itemId) {
        return getEquipStats(itemId).get("level") > 0;
    }
    
    public int getStatLevelupProbability(int itemId, int type) {
        MapleData data = getItemData(itemId);
        MapleData data1 = data.getChildByPath("info").getChildByPath("level").getChildByPath("case").getChildByPath("" + type).getChildByPath("prob");
    	return MapleDataTool.getIntConvert(data1, 0);
    }

    public LevelUpInformation getItemLevelupStats(int itemId, int level) {
        LevelUpInformation info = null;
        if (levelUpCache.containsKey(itemId)) {
                info = levelUpCache.get(itemId).get(level);
                if (info != null) {
                        return info;
                }
        }
        HashMap<Integer, LevelUpInformation> information = new HashMap<>();
        MapleData data = getItemData(itemId);
        MapleData data1 = data.getChildByPath("info").getChildByPath("level");
        if (data1 != null) {
                for (int i = 1; i <= 6; i++) {
                    MapleData data2 = data1.getChildByPath("info").getChildByPath(Integer.toString(i));
                    if (data2 != null) {
                        info = new LevelUpInformation();
                        for (MapleData da : data2.getChildren()) {
                                info.getStats().put(da.getName(), MapleDataTool.getInt(da));
                        }
                        information.put(i, info);
                    } else {
                        break;
                    }
                }
        }
        levelUpCache.put(itemId, information);
        return information.get(level);
    }
 
    private static int getCrystalForLevel(int level) {
        int range = level / 10; // (level - 1)?

        if(range < 5) {
            return 4260000;
        } else if(range > 11) {
            return 4260008;
        } else {
            return switch (range) {
                case 5 -> 4260001;
                case 6 -> 4260002;
                case 7 -> 4260003;
                case 8 -> 4260004;
                case 9 -> 4260005;
                case 10 -> 4260006;
                default -> 4260007;
            };
        }
    }

    public Pair<String, Integer> getMakerReagentStatUpgrade(int itemId) {
        try {
            Pair<String, Integer> statUpgd = statUpgradeMakerCache.get(itemId);
            if (statUpgd != null) {
                return statUpgd;
            } else if (statUpgradeMakerCache.containsKey(itemId)) {
                return null;
            }

            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT stat, value FROM maker_reagent_data WHERE itemid = ?")) {
                    ps.setInt(1, itemId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String statType = rs.getString("stat");
                            int statGain = rs.getInt("value");

                            statUpgd = new Pair<>(statType, statGain);
                        }
                    }
                }
            }
            statUpgradeMakerCache.put(itemId, statUpgd);
            return statUpgd;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getMakerCrystalFromLeftover(Integer leftoverId) {
        try {
            Integer itemid = mobCrystalMakerCache.get(leftoverId);
            if (itemid != null) {
                return itemid;
            }

            itemid = -1;

            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT dropperid FROM drop_data WHERE itemid = ? ORDER BY dropperid;")) {
                    ps.setInt(1, leftoverId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int dropperid = rs.getInt("dropperid");
                            itemid = getCrystalForLevel(MapleLifeFactory.getMonsterLevel(dropperid) - 1);
                        }
                    }
                }
            }

            mobCrystalMakerCache.put(leftoverId, itemid);
            return itemid;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public MakerItemCreateEntry getMakerItemEntry(int toCreate) {
        MakerItemCreateEntry makerEntry;
        int reqLevel = -1;
        int reqMakerLevel = -1;
        int cost = -1;
        int toGive = -1;

        if ((makerEntry = makerItemCache.get(toCreate)) != null) {
            return new MakerItemCreateEntry(makerEntry);
        } else {
            try (Connection con = DatabaseConnection.getConnection()) {
                try (PreparedStatement ps = con.prepareStatement("SELECT req_level, req_maker_level, req_meso, quantity FROM maker_create_data WHERE itemid = ?")) {
                    ps.setInt(1, toCreate);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            reqLevel = rs.getInt("req_level");
                            reqMakerLevel = rs.getInt("req_maker_level");
                            cost = rs.getInt("req_meso");
                            toGive = rs.getInt("quantity");
                        }
                    }
                }

                makerEntry = new MakerItemCreateEntry(cost, reqLevel, reqMakerLevel);
                makerEntry.addGainItem(toCreate, toGive);
                try (PreparedStatement ps = con.prepareStatement("SELECT req_item, count FROM maker_recipe_data WHERE itemid = ?")) {
                    ps.setInt(1, toCreate);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            makerEntry.addReqItem(rs.getInt("req_item"), rs.getInt("count"));
                        }
                    }
                }

                makerItemCache.put(toCreate, new MakerItemCreateEntry(makerEntry));
            } catch (SQLException sqle) {
                sqle.printStackTrace();
                makerEntry = null;
            }
        }

        return makerEntry;
    }

    public int getMakerCrystalFromEquip(Integer equipId) {
        try {
            return getCrystalForLevel(getEquipLevelReq(equipId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public int getMakerStimulantFromEquip(Integer equipId) {
        try {
            return getCrystalForLevel(getEquipLevelReq(equipId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<Pair<Integer, Integer>> getMakerDisassembledItems(Integer itemId) {
        List<Pair<Integer, Integer>> items = new LinkedList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT req_item, count FROM maker_recipe_data WHERE itemid = ? AND req_item >= 4260000 AND req_item < 4270000")) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // TODO im not sure whether this value is actually half the crystals needed for creation or slightly randomized
                        items.add(new Pair<>(rs.getInt("req_item"), rs.getInt("count") / 2));   // return to the player half of the crystals needed
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public int getMakerDisassembledFee(Integer itemId) {
        int fee = -1;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT req_meso FROM maker_create_data WHERE itemid = ?")) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {   // cost is 13.6363~ % of the original value trimmed by 1000.
                        float val = (float) (rs.getInt("req_meso") * 0.13636363636364);
                        fee = (int) (val / 1000);
                        fee *= 1000;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fee;
    }

    public int getMakerStimulant(int itemId) {  // thanks to Arnah
        Integer itemid = makerCatalystCache.get(itemId);
        if(itemid != null) {
            return itemid;
        }

        itemid = -1;
        for(MapleData md : etcData.getData("ItemMake.img").getChildren()) {
            MapleData me = md.getChildByPath(StringUtil.getLeftPaddedStr(Integer.toString(itemId), '0', 8));

            if(me != null) {
                itemid = MapleDataTool.getInt(me.getChildByPath("catalyst"), -1);
                break;
            }
        }

        makerCatalystCache.put(itemId, itemid);
        return itemid;
    }

    public Set<String> getWhoDrops(Integer itemId) {
        Set<String> list = new HashSet<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT dropperid FROM drop_data WHERE itemid = ? LIMIT 50")) {
                ps.setInt(1, itemId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String resultName = MapleMonsterInformationProvider.getInstance().getMobNameFromId(rs.getInt("dropperid"));
                        if (resultName != null) {
                            list.add(resultName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private boolean canUseSkillBook(MapleCharacter player, Integer skillBookId) {
        Map<String, Integer> skilldata = getSkillStats(skillBookId, player.getJob().getId());
        if(skilldata == null || skilldata.get("skillid") == 0) return false;

        PlayerSkill skill2 = SkillFactory.getSkill(skilldata.get("skillid"));
        return (skilldata.get("skillid") != 0 && ((player.getSkillLevel(skill2) >= skilldata.get("reqSkillLevel") || skilldata.get("reqSkillLevel") == 0) && player.getMasterLevel(skill2) < skilldata.get("masterLevel")));
    }

    public List<Integer> usableMasteryBooks(MapleCharacter player) {
        List<Integer> masterybook = new LinkedList<>();
        for(Integer i = 2290000; i <= 2290139; i++) {
            if(canUseSkillBook(player, i)) {
                masterybook.add(i);
            }
        }

        return masterybook;
    }

    public List<Integer> usableSkillBooks(MapleCharacter player) {
        List<Integer> skillbook = new LinkedList<>();
        for(Integer i = 2280000; i <= 2280019; i++) {
            if(canUseSkillBook(player, i)) {
                skillbook.add(i);
            }
        }

        return skillbook;
    }

    public class ScriptedItem {

        private boolean runOnPickup;
        private int npc;
        private String script;

        public ScriptedItem(int npc, String script, boolean rop) {
            this.npc = npc;
            this.script = script;
            this.runOnPickup = rop;
        }

        public int getNpc() {
            return npc;
        }

        public String getScript() {
            return script;
        }

        public boolean runOnPickup() {
            return runOnPickup;
        }
    }

    public static final class RewardItem {

        public int itemid, period;
        public short prob, quantity;
        public String effect, worldmsg;
    }
    
    public final QuestConsItem getQuestConsumablesInfo(final int itemId) {
        if (questItemConsCache.containsKey(itemId)) {
            return questItemConsCache.get(itemId);
        }
        MapleData data = getItemData(itemId);

        MapleData infoData = data.getChildByPath("info");
        MapleData ciData = infoData.getChildByPath("consumeItem");
        QuestConsItem qcItem = null;
        if (ciData != null) {
            qcItem = new QuestConsItem();
            qcItem.exp = MapleDataTool.getInt("exp", infoData);
            qcItem.grade = MapleDataTool.getInt("grade", infoData);
            qcItem.questid = MapleDataTool.getInt("questId", infoData);
            qcItem.items = new HashMap<>(2);

            Map<Integer, Integer> cItems = qcItem.items;
            for (MapleData ciItem : ciData.getChildren()) {
                int itemid = MapleDataTool.getInt("0", ciItem);
                int qty = MapleDataTool.getInt("1", ciItem);

                cItems.put(itemid, qty);
            }
        }

        questItemConsCache.put(itemId, qcItem);
        return qcItem;
    }
    
     public static final class QuestConsItem {

        public int questid, exp, grade;
        public Map<Integer, Integer> items;

        public Integer getItemRequirement(int itemid) {
            return items.get(itemid);
        }
    }
     
    private static double testYourLuck(double prop) {
        return Math.pow(1.0 - prop, 1); // 1 is number of attempts
    }

    public static boolean rollSuccessChance(double propPercent) {
        return Math.random() >= testYourLuck(propPercent / 100.0); // one attempt at this (x/100)
    }
    
  public boolean isAccountRestricted(int itemId) {
    if (accountItemRestrictionCache.containsKey(itemId)) {
        return accountItemRestrictionCache.get(itemId);
    }

    boolean bRestricted = false;
    if(itemId != 0) {
        MapleData data = getItemData(itemId);
        if (data != null) {
            bRestricted = isAccountRestricted(itemId);
        }
    }

        accountItemRestrictionCache.put(itemId, bRestricted);
        return bRestricted;
    }
}
