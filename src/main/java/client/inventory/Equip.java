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
package client.inventory;

import client.MapleBuffStat;
import client.MapleClient;
import client.MapleStat;
import constants.ExpTable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import server.MapleItemInformationProvider;
import tools.*;

public class Equip extends Item {

    public static enum ScrollResult {

        FAIL(0), SUCCESS(1), CURSE(2);
        private int value = -1;

        private ScrollResult(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
    
    public static enum StatUpgrade {

        incDEX(0), incSTR(1), incINT(2), incLUK(3),
        incMHP(4), incMMP(5), incPAD(6), incMAD(7),
        incPDD(8), incMDD(9), incEVA(10), incACC(11),
        incSpeed(12), incJump(13), incVicious(14), incSlot(15);
        private int value = -1;

        private StatUpgrade(int value) {
            this.value = value;
        }
    }
    
    private int upgradeSlots;
    private int level, flag, itemLevel;
    private short str, dex, _int, luk, hp, mp, watk, matk, wdef, mdef, acc, avoid, hands, speed, jump, vicious;
    private float itemExp;
    private int ringid = -1;
    private boolean skill = false; //nLevelUpType
    private boolean wear = false;
    private int exp = 0;

    public Equip(int id, short position) {
        this(id, position, 0);
    }

    public Equip(int id, short position, int slots) {
        super(id, position, (short) 1);
        this.upgradeSlots = (byte) slots;
        this.itemExp = 0;
        this.itemLevel = 1;
    }

    @Override
    public Item copy() {
        Equip ret = new Equip(getItemId(), getPosition(), getUpgradeSlots());
        ret.str = str;
        ret.dex = dex;
        ret._int = _int;
        ret.luk = luk;
        ret.hp = hp;
        ret.mp = mp;
        ret.matk = matk;
        ret.mdef = mdef;
        ret.watk = watk;
        ret.wdef = wdef;
        ret.acc = acc;
        ret.avoid = avoid;
        ret.hands = hands;
        ret.speed = speed;
        ret.jump = jump;
        ret.flag = flag;
        ret.vicious = vicious;
        ret.upgradeSlots = upgradeSlots;
        ret.itemLevel = itemLevel;
        ret.itemExp = itemExp;
        ret.level = level;
        ret.skill = skill;
        ret.log = new LinkedList<>(log);
        ret.setOwner(getOwner());
        ret.setQuantity(getQuantity());
        ret.setExpiration(getExpiration());
        ret.setGiftFrom(getGiftFrom());
        // TODO
        //ret.setDisappearsAtLogout(disappearsAtLogout());
        //ret.setRecovery(getRecovery());
        return ret;
    }

    @Override
    public int getFlag() {
        return flag;
    }

    @Override
    public byte getItemType() {
        return 1;
    }
    
    public int getUpgradeSlots() {
        return upgradeSlots;
    }

    public short getStr() {
        return str;
    }

    public short getDex() {
        return dex;
    }

    public short getInt() {
        return _int;
    }

    public short getLuk() {
        return luk;
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public short getWatk() {
        return watk;
    }

    public short getMatk() {
        return matk;
    }

    public short getWdef() {
        return wdef;
    }

    public short getMdef() {
        return mdef;
    }

    public short getAcc() {
        return acc;
    }

    public short getAvoid() {
        return avoid;
    }

    public short getHands() {
        return hands;
    }

    public short getSpeed() {
        return speed;
    }

    public short getJump() {
        return jump;
    }

    public short getVicious() {
        return vicious;
    }

    @Override
    public void setFlag(int flag) {
        this.flag = flag;
    }

    public void setStr(short str) {
        this.str = str;
    }

    public void setDex(short dex) {
        this.dex = dex;
    }

    public void setInt(short _int) {
        this._int = _int;
    }

    public void setLuk(short luk) {
        this.luk = luk;
    }

    public void setHp(short hp) {
        this.hp = hp;
    }

    public void setMp(short mp) {
        this.mp = mp;
    }

    public void setWatk(short watk) {
        this.watk = watk;
    }

    public void setMatk(short matk) {
        this.matk = matk;
    }

    public void setWdef(short wdef) {
        this.wdef = wdef;
    }

    public void setMdef(short mdef) {
        this.mdef = mdef;
    }

    public void setAcc(short acc) {
        this.acc = acc;
    }

    public void setAvoid(short avoid) {
        this.avoid = avoid;
    }

    public void setHands(short hands) {
        this.hands = hands;
    }

    public void setSpeed(short speed) {
        this.speed = speed;
    }

    public void setJump(short jump) {
        this.jump = jump;
    }

    public void setVicious(short vicious) {
        this.vicious = vicious;
    }

    public void setUpgradeSlots(byte upgradeSlots) {
        this.upgradeSlots = upgradeSlots;
    }

    public int getLevel() {
        return level;
    }
    
    public void gainLevel(MapleClient c) {
    	LevelUpInformation levelup = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel);
    	Map<String, Integer> stats = levelup.getStats();
    	int statProb = MapleItemInformationProvider.getInstance().getStatLevelupProbability(getItemId(), 0);
    	stats.entrySet().stream().forEach(stat -> {
			if (Randomizer.nextInt(10) <= statProb) {
				if (stat.getKey().contains("incSTRMax")) {
					str += Randomizer.rand(stats.get("incSTRMin"), stat.getValue());
				} else if (stat.getKey().contains("incDEXMax")) {
					dex += Randomizer.rand(stats.get("incDEXMin"), stat.getValue());
				} else if (stat.getKey().contains("incINTMax")) {
					_int += Randomizer.rand(stats.get("incINTMin"), stat.getValue());
				} else if (stat.getKey().contains("incLUKMax")) {
					luk += Randomizer.rand(stats.get("incLUKMin"), stat.getValue());
				} else if (stat.getKey().contains("incPADMax")) {
					watk += Randomizer.rand(stats.get("incPADMin"), stat.getValue());
				} else if (stat.getKey().contains("incMADMax")) {
					matk += Randomizer.rand(stats.get("incMADMin"), stat.getValue());
				} else if (stat.getKey().contains("incPDDMax")) {
					wdef += Randomizer.rand(stats.get("incPDDMin"), stat.getValue());
				} else if (stat.getKey().contains("incMDDMax")) {
					mdef += Randomizer.rand(stats.get("incMDDMin"), stat.getValue());
				} else if (stat.getKey().contains("incSPEEDMax")) {
					speed += Randomizer.rand(stats.get("incSPEEDMin"), stat.getValue());
				} else if (stat.getKey().contains("incJUMPMax")) {
					str += Randomizer.rand(stats.get("incJUMPMin"), stat.getValue());
				} else if (stat.getKey().contains("incEVAMax")) {
					avoid += Randomizer.rand(stats.get("incEVAMin"), stat.getValue());
				} else if (stat.getKey().contains("incACCMax")) {
					acc += Randomizer.rand(stats.get("incACCMin"), stat.getValue());
				}
			}    		
    	});
        this.itemLevel++;
        if (itemLevel == 6) {
        	int skillProb = MapleItemInformationProvider.getInstance().getStatLevelupProbability(getItemId(), 1);
        	int rand = Randomizer.nextInt(10);
        	if (rand <= skillProb) {
        		skill = true;
        	}
        }
        exp = 0;
        c.announce(MaplePacketCreator.showItemLevelup());
        c.getPlayer().getMap().broadcastMessage(c.getPlayer(),
        		MaplePacketCreator.showItemLevelup()); // TODO make sure this actually broadcasts to all players in map 
        c.getPlayer().forceUpdateItem(this);
    }

    public void setLevel(int level) {
        this.level = level;
    }

       public void setStat(MapleStat stat, short amount) {
           switch (stat) {
               case INT -> setInt(amount);
               case STR -> setStr(amount);
               case LUK -> setLuk(amount);
               case DEX -> setDex(amount);
               case HP -> setHp(amount);
               case MP -> setMp(amount);
               default -> FilePrinter.printError(FilePrinter.ITEM, "invalid stat to get. use its setter instead.");
           }
    }

    public void setStat(MapleBuffStat stat, short amount) {
        switch (stat) {
            case WATK -> setWatk(amount);
            case MATK -> setMatk(amount);
            case ACC -> setAcc(amount);
            case SPEED -> setSpeed(amount);
            case JUMP -> setJump(amount);
            case AVOID -> setAvoid(amount);
            case WDEF -> setWdef(amount);
            case MDEF -> setMdef(amount);
            case HANDS -> setHands(amount);
            default -> FilePrinter.printError(FilePrinter.ITEM, "invalid stat to get. use its setter instead.");
        }
    }

      public short getStat(MapleStat stat) {
        switch (stat) {
            case INT:
                return getInt();
            case STR:
                return getStr();
            case LUK:
                return getLuk();
            case DEX:
                return getDex();
            case HP:
                return getHp();
            case MP:
                return getMp();
            default:
                FilePrinter.printError(FilePrinter.ITEM, "invalid stat to get. use its getter instead.");
        }
        return 0;
    }

    public Map<StatUpgrade, Short> getStats() {
        Map<StatUpgrade, Short> stats = new HashMap<>(5);

        if(dex > 0) stats.put(StatUpgrade.incDEX, dex);
        if(str > 0) stats.put(StatUpgrade.incSTR, str);
        if(_int > 0) stats.put(StatUpgrade.incINT,_int);
        if(luk > 0) stats.put(StatUpgrade.incLUK, luk);
        if(hp > 0) stats.put(StatUpgrade.incMHP, hp);
        if(mp > 0) stats.put(StatUpgrade.incMMP, mp);
        if(watk > 0) stats.put(StatUpgrade.incPAD, watk);
        if(matk > 0) stats.put(StatUpgrade.incMAD, matk);
        if(wdef > 0) stats.put(StatUpgrade.incPDD, wdef);
        if(mdef > 0) stats.put(StatUpgrade.incMDD, mdef);
        if(avoid > 0) stats.put(StatUpgrade.incEVA, avoid);
        if(acc > 0) stats.put(StatUpgrade.incACC, acc);
        if(speed > 0) stats.put(StatUpgrade.incSpeed, speed);
        if(jump > 0) stats.put(StatUpgrade.incJump, jump);

        return stats;
    }

  public short getStat(MapleBuffStat stat) {
        switch (stat) {
            case WATK:
                return getWatk();
            case MATK:
                return getMatk();
            case ACC:
                return getAcc();
            case SPEED:
                return getSpeed();
            case JUMP:
                return getJump();
            case AVOID:
                return getAvoid();
            case WDEF:
                return getWdef();
            case MDEF:
                return getMdef();
            default:
                FilePrinter.printError(FilePrinter.ITEM, "invalid stat to get. use its getter instead.");
        }
        return 0;
    }

    public int getItemExp() {
        return (int) itemExp;
    }
    
    public void gainItemExp(MapleClient c, int gain) {
    	int expMod = getExpMultiplier();
    	if (expMod == 0) {
    		return;
    	}
    	int itemReqLevel = MapleItemInformationProvider.getInstance().getEquipLevelReq(getItemId());
    	if (itemReqLevel < 1) {
    		itemReqLevel = 1;
    	}
    	int reqExp = ExpTable.INSTANCE.getExpNeededForLevel(itemReqLevel);
		float modifier = (150 - (float) expMod) * 0.01f;
    		itemExp += (int) gain * modifier;
    	if (itemExp >= reqExp) {
    		float overflow = itemExp - reqExp;
			itemExp = 0;
    		gainLevel(c);
    		if (overflow > 0) {
    			gainItemExp(c, (int) overflow);
    		}
    	} else {
    		c.getPlayer().updateExpOnItem(this);
    	}
    }

    public int getExpMultiplier() {
    	if (exp == 0) {
    		Map<String, Integer> stat = MapleItemInformationProvider.getInstance().getItemLevelupStats(getItemId(), itemLevel).getStats();   		
        	Integer e = null;
    		if (stat != null) {
    			e = stat.get("exp");
    		}
    		exp = e == null ? 80 : e;
    	}
		return exp;
    }
    
    public void setItemExp(int exp) {
        this.itemExp = exp;
    }

    public void setItemLevel(byte level) {
        this.itemLevel = level;
    }

    @Override
    public void setQuantity(short quantity) {
        if (quantity < 0 || quantity > 1) {
            throw new RuntimeException("Setting the quantity to " + quantity + " on an equip (itemid: " + getItemId() + ")");
        }
        super.setQuantity(quantity);
    }

    public void setUpgradeSlots(int i) {
        this.upgradeSlots = (byte) i;
    }

    public void setVicious(int i) {
        this.vicious = (short) i;
    }

    public int getRingId() {
        return ringid;
    }

    public void setRingId(int id) {
        this.ringid = id;
    }

    public boolean hasSkill() {
            return skill;
    }

    public void setSkill(boolean skill) {
            this.skill = skill;
    }

    public boolean isWearing() {
        return wear;
    }

    public void wear(boolean yes) {
        wear = yes;
    }

    public int getItemLevel() {
        return itemLevel;
    }
}