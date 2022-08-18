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
package constants;

import client.inventory.MapleInventoryType;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Jay Estrella
 * @author Ronan
 */
public final class ItemConstants {
    protected static Map<Integer, MapleInventoryType> inventoryTypeCache = new HashMap<>();
    
    public final static int LOCK = 0x01;
    public final static int SPIKES = 0x02;
    public final static int KARMA_USE = 0x02;
    public final static int COLD = 0x04;
    public final static int UNTRADEABLE = 0x08;
    public final static int KARMA_EQP = 0x10;
    public final static int SANDBOX = 0x40;             // let 0x40 until it's proven something uses this
    public final static int PET_COME = 0x80;
    public final static int ACCOUNT_SHARING = 0x100;
    public final static int MERGE_UNTRADEABLE = 0x200;

    public final static boolean EXPIRING_ITEMS = true;
    public final static Set<Integer> permanentItemids = new HashSet<>();

    static {
        int[] pi = {5000060, 5000100, 5000101, 5000102};    // i ain't going to open one gigantic itemid cache just for 4 perma itemids, no way!
        for(int i : pi) {
            permanentItemids.add(i);
        }
    }
    
    public static int getFlagByInt(int type) {
        if (type == 128) {
            return PET_COME;
        } else if (type == 256) {
            return ACCOUNT_SHARING;
        }
        return 0;
    }

    public static boolean isThrowingStar(int itemId) {
        return itemId / 10000 == 207;
    }

    public static boolean isBullet(int itemId) {
        return itemId / 10000 == 233;
    }
    
    public static boolean isPotion(int itemId) {
        return itemId / 1000 == 2000;
    }
    
    public static boolean isFood(int itemId) {
        int useType = itemId / 1000;
        return useType == 2022 || useType == 2010 || useType == 2020;
    }
    
    public static boolean isConsumable(int itemId) {
        return isPotion(itemId) || isFood(itemId);
    }

    public static boolean isRechargeable(int itemId) {
        return isThrowingStar(itemId) || isBullet(itemId);
    }

    public static boolean isArrowForCrossBow(int itemId) {
        return itemId / 1000 == 2061;
    }

    public static boolean isArrowForBow(int itemId) {
        return itemId / 1000 == 2060;
    }
    
    public static boolean isArrow(int itemId) {
        return isArrowForBow(itemId) || isArrowForCrossBow(itemId);
    }

    public static boolean isPet(int itemId) {
        return itemId / 1000 == 5000;
    }

    public static boolean isIncMaxHpItem(int itemid) {
        switch (itemid) {
            case 2022366:
            case 2022384:
            case 2022393:
            case 2022402:
            case 2022411:
            case 2022420:
            case 2022375:
                return true;
        }
        return false;
    }

    public static boolean isIncMaxMpItem(int itemid) {
        switch (itemid) {
            case 2022367:
            case 2022376:
            case 2022385:
            case 2022394:
            case 2022403:
            case 2022412:
            case 2022421:
                return true;
        }
        return false;
    }

    public static boolean isPermanentItem(int itemId) {
        return permanentItemids.contains(itemId);
    }
    
    public static boolean isNewYearCardEtc(int itemId) { 
        return itemId / 10000 == 430;
    } 
     
    public static boolean isNewYearCardUse(int itemId) { 
        return itemId / 10000 == 216;
    }
    
    public static boolean isAccessory(int itemId) {
        return itemId >= 1110000 && itemId < 1140000;
    }
    
    public static boolean isTaming(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 1902 || itemType == 1912;
    }
    
    public static boolean isTownScroll(int itemId) {
        return itemId >= 2030000 && itemId < 2030100;
    }
    
    public static boolean isAntibanishScroll(int itemId) {
        return itemId == 2030100;
    }
    
    public static boolean isCleanSlate(int scrollId) {
        return scrollId > 2048999 && scrollId < 2049004;
    }
    
    public static boolean isModifierScroll(int scrollId) {
        return scrollId == 2040727 || scrollId == 2041058;
    }
    
    public static boolean isFlagModifier(int scrollId, int flag) {
        if(scrollId == 2041058 && ((flag & ItemConstants.COLD) == ItemConstants.COLD)) return true;
        if(scrollId == 2040727 && ((flag & ItemConstants.SPIKES) == ItemConstants.SPIKES)) return true;
        return false;
    }
    
    public static boolean isChaosScroll(int scrollId) {
    	return (scrollId >= 2049100 && scrollId <= 2049103) || scrollId == 2049199;
    }
    
    public static boolean isRateCoupon(int itemId) {
        int itemType = itemId / 1000;
        return itemType == 5211 || itemType == 5360;
    }
    
    public static boolean isExpCoupon(int couponId) {
        return couponId / 1000 == 5211;
    }
    
    public static boolean isPartyItem(int itemId) {
        return itemId >= 2022430 && itemId <= 2022433;
    }
    
    public static boolean isPartyAllcure(int itemId) {
        return itemId == 2022433;
    }
    
    public static boolean isHiredMerchant(int itemId) {
        return itemId / 10000 == 503;
    }
    
    public static boolean isPlayerShop(int itemId) {
        return itemId / 10000 == 514;
    }
    
    public static MapleInventoryType getInventoryType(final int itemId) {
        if (inventoryTypeCache.containsKey(itemId)) {
            return inventoryTypeCache.get(itemId);
        }
        
        MapleInventoryType ret = MapleInventoryType.UNDEFINED;
        
	final byte type = (byte) (itemId / 1000000);
	if (type >= 1 && type <= 5) {
	    ret = MapleInventoryType.getByType(type);
	}
        
        inventoryTypeCache.put(itemId, ret);
        return ret;
    }
    
    public static boolean isMakerReagent(int itemId) {
        return itemId / 10000 == 425;
    }
    
    public static boolean isOverall(int itemId) {
        return itemId / 10000 == 105;
    }
    
    public static boolean isCashStore(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 503 || itemType == 514;
    }
    
    public static boolean isMapleLife(int itemId) {
        int itemType = itemId / 10000;
        return itemType == 543 && itemId != 5430000;
    }

    public static boolean isWeapon(int itemId) {
        return itemId >= 1302000 && itemId < 1493000;
    }
    
    public static boolean isEquipment(int itemId) {
        return itemId < 2000000 && itemId != 0;
    }

    public static boolean isFishingChair(int itemId) {
        return itemId == 3011000;
    }
    
    public static boolean isMedal(int itemId) {
        return itemId >= 1140000 && itemId <= 1149999;
    }
    
    public static boolean isWeddingRing(int itemId) {
        return itemId >= 1112803 && itemId <= 1112809;
    }
    
    public static boolean isWeddingToken(int itemId) {
        return itemId >= 4031357 && itemId <= 4031364;
    }
    
    public static boolean isFace(int itemId) {
        return itemId >= 20000 && itemId < 22000;
    }
    
    public static boolean isHair(int itemId) {
        return itemId >= 30000 && itemId < 43000;
    }
    
    public static boolean isRestrictedId(int itemId) {
        // Full list of GM !item/drop restricted items are: alpha medal, Alpha bandanas
        return itemId == 1142104 || itemId >= 1002680 && itemId <= 1002684;
    }
    
    public static boolean isFaceExpression(int itemId) {
        return itemId / 10000 == 516;
    }

    public static boolean isChair(int itemId) {
        return itemId / 10000 == 301;
    }
    
    public static boolean isGachNotificationPrize(int itemId) {
        switch(itemId) {
            case 2340000: // white scroll
            case 2049100: // chaos scroll
            case 1102084: // pink gaia cape
            case 1102041: // pink adventurer cape
            case 1102042: // purple adventurer cape
            case 1102086: // purple gaia cape
            case 1082149: // brown work gloves
                return true;
            default:
                return false;
        }
    }
}
