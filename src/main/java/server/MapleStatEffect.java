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

import client.*;
import client.inventory.manipulator.MapleInventoryManipulator;
import java.awt.Point;
import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import net.server.Server;
import provider.MapleData;
import provider.MapleDataTool;
import server.life.MapleMonster;
import server.maps.FieldLimit;
import server.maps.MapleDoor;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.MapleMist;
import server.maps.MapleSummon;
import server.maps.SummonMovementType;
import server.skills.*;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Pair;
import client.inventory.Item;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.ItemConstants;
import constants.ServerConstants;
import constants.skills.Aran;
import constants.skills.Assassin;
import constants.skills.Bandit;
import constants.skills.Beginner;
import constants.skills.Bishop;
import constants.skills.BlazeWizard;
import constants.skills.Bowmaster;
import constants.skills.Brawler;
import constants.skills.Buccaneer;
import constants.skills.ChiefBandit;
import constants.skills.Cleric;
import constants.skills.Corsair;
import constants.skills.Crossbowman;
import constants.skills.Crusader;
import constants.skills.DarkKnight;
import constants.skills.DawnWarrior;
import constants.skills.DragonKnight;
import constants.skills.Evan;
import constants.skills.FPArchMage;
import constants.skills.FPMage;
import constants.skills.FPWizard;
import constants.skills.Fighter;
import constants.skills.GM;
import constants.skills.Gunslinger;
import constants.skills.Hermit;
import constants.skills.Hero;
import constants.skills.Hunter;
import constants.skills.ILArchMage;
import constants.skills.ILMage;
import constants.skills.ILWizard;
import constants.skills.Legend;
import constants.skills.Magician;
import constants.skills.Marauder;
import constants.skills.Marksman;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Noblesse;
import constants.skills.Outlaw;
import constants.skills.Page;
import constants.skills.Paladin;
import constants.skills.Pirate;
import constants.skills.Priest;
import constants.skills.Ranger;
import constants.skills.Rogue;
import constants.skills.Shadower;
import constants.skills.Sniper;
import constants.skills.Spearman;
import constants.skills.SuperGM;
import constants.skills.ThunderBreaker;
import constants.skills.WhiteKnight;
import constants.skills.WindArcher;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import server.partyquest.MapleCarnivalFactory;
import server.partyquest.MapleCarnivalFactory.MCSkill;

/**
 * @author Matze
 * @author Frz
 * @author Ronan
 */
public class MapleStatEffect {

    private short watk, matk, wdef, mdef, acc, avoid, speed, jump;
    private short hp, mp;
    private double hpR, mpR;
    private short mhpRRate, mmpRRate, mobSkill, mobSkillLevel;
    private byte mhpR, mmpR;
    private short mpCon, hpCon;
    private int duration, target, barrier, mob;
    private boolean overTime, repeatEffect, disease = false;
    private int sourceid, sourceLevel = 0;
    private int moveTo = -1;
    private int cp, nuffSkill;
    //private List<MapleDisease> cureDebuffs;
    private boolean skill;
    private List<Pair<MapleBuffStat, BuffValueHolder>> statups;
    private Map<MonsterStatus, Integer> monsterStatus;
    private int x, y, mobCount, moneyCon, cooldown, morphId = 0, ghost, fatigue, berserk, booster;
    private double prop;
    private int itemCon, itemConNo;
    private int damage, attackCount, fixdamage;
    private Point lt, rb;
    private byte bulletCount, bulletConsume;
    private boolean poison = false;
    private boolean seal = false;
    private boolean darkness = false;
    private boolean weakness = false;
    private boolean curse = false;
    private boolean party = false;
    //private byte mapProtection;
    private CardItemupStats cardStats;
    
    private static class CardItemupStats {
        protected int itemCode, prob;
        private List<Pair<Integer, Integer>> areas;
        protected boolean party;
        
        private CardItemupStats(int code, int prob, List<Pair<Integer, Integer>> areas, boolean inParty) {
            this.itemCode = code;
            this.prob = prob;
            this.areas = areas;
            this.party = inParty;
        }
        
        private boolean isInArea(int mapid) {
            if (this.areas == null) {
                return true;
            }
            
            for (Pair<Integer, Integer> a : this.areas) {
                if (mapid >= a.left && mapid <= a.right) {
                    return true;
                }
            }
            
            return false;
        }
    }
    
      private boolean isEffectActive(int mapid, boolean partyHunting) {
        if (cardStats == null) return true;

        if (!cardStats.isInArea(mapid)) {
            return false;
        }

        if (cardStats.party && !partyHunting) {
            return false;
        }

        return true;
    }
    
    public boolean isActive(MapleCharacter applyto) {
        return isEffectActive(applyto.getMapId(), applyto.getPartyMembersOnSameMap().size() > 1);
    }
    
    public int getCardRate(int mapid, int itemid) {
        if (cardStats != null) {
            if (cardStats.itemCode == Integer.MAX_VALUE) {
                return cardStats.prob;
            } else if (cardStats.itemCode < 1000) {
                if (itemid / 10000 == cardStats.itemCode) {
                    return cardStats.prob;
                }
            } else {
                if (itemid == cardStats.itemCode) {
                    return cardStats.prob;
                }
            }
        }
        
        return 0;
    }
    
    public static MapleStatEffect loadSkillEffectFromData(MapleData source, int skillid, boolean overtime) {
        return loadFromData(source, skillid, true, overtime);
    }

    public static MapleStatEffect loadItemEffectFromData(MapleData source, int itemid) {
        return loadFromData(source, itemid, false, false);
    }

    private static void addBuffStatPairToListIfNotZero(List<Pair<MapleBuffStat, BuffValueHolder>> list, MapleBuffStat buffstat, Integer val) {
        if (val != 0) {
            list.add(new Pair<>(buffstat, new BuffValueHolder(0, 0, val)));
        }
    }

    private static byte mapProtection(int sourceid) {
        if (sourceid == 2022001 || sourceid == 2022186) {
            return 1;   //elnath cold
        } else if (sourceid == 2022040) {
            return 2;   //aqua road underwater
        } else {
            return 0;
        }
    }

    private static MapleStatEffect loadFromData(MapleData source, int sourceid, boolean skill, boolean overTime) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.duration = MapleDataTool.getIntConvert("time", source, -1);
        ret.hp = (short) MapleDataTool.getInt("hp", source, 0);
        ret.hpR = MapleDataTool.getInt("hpR", source, 0) / 100.0;
        ret.mp = (short) MapleDataTool.getInt("mp", source, 0);
        ret.mpR = MapleDataTool.getInt("mpR", source, 0) / 100.0;
        ret.mpCon = (short) MapleDataTool.getInt("mpCon", source, 0);
        ret.hpCon = (short) MapleDataTool.getInt("hpCon", source, 0);
        int iprop = MapleDataTool.getInt("prop", source, 100);
        ret.prop = iprop / 100.0;
        ret.poison = MapleDataTool.getInt("poison", source, 0) > 0;
        ret.seal = MapleDataTool.getInt("seal", source, 0) > 0;
        ret.darkness = MapleDataTool.getInt("darkness", source, 0) > 0;
        ret.weakness = MapleDataTool.getInt("weakness", source, 0) > 0;
        ret.curse = MapleDataTool.getInt("curse", source, 0) > 0;
        ret.party = MapleDataTool.getInt("party", source, 0) > 0;
        ret.cp = MapleDataTool.getInt("cp", source, 0);
       /* List<MapleDisease> cure = new ArrayList<>(5);
        if (MapleDataTool.getInt("poison", source, 0) > 0) {
            cure.add(MapleDisease.POISON);
        }
        if (MapleDataTool.getInt("seal", source, 0) > 0) {
            cure.add(MapleDisease.SEAL);
        }
        if (MapleDataTool.getInt("darkness", source, 0) > 0) {
            cure.add(MapleDisease.DARKNESS);
        }
        if (MapleDataTool.getInt("weakness", source, 0) > 0) {
            cure.add(MapleDisease.WEAKEN);
        }
        if (MapleDataTool.getInt("curse", source, 0) > 0) {
            cure.add(MapleDisease.CURSE);
        }
        ret.cureDebuffs = cure;*/
        ret.nuffSkill = MapleDataTool.getInt("nuffSkill", source, 0);

        ret.mobCount = MapleDataTool.getInt("mobCount", source, 1);
        ret.cooldown = MapleDataTool.getInt("cooltime", source, 0);
        ret.morphId = MapleDataTool.getInt("morph", source, 0);
        ret.ghost = MapleDataTool.getInt("ghost", source, 0);
        ret.fatigue = MapleDataTool.getInt("incFatigue", source, 0);
        ret.repeatEffect = MapleDataTool.getInt("repeatEffect", source, 0) > 0;

        MapleData mdd = source.getChildByPath("0");
        if (mdd != null && mdd.getChildren().size() > 0) {
            ret.mobSkill = (short) MapleDataTool.getInt("mobSkill", mdd, 0);
            ret.mobSkillLevel = (short) MapleDataTool.getInt("level", mdd, 0);
            ret.target = MapleDataTool.getInt("target", mdd, 0);
        } else {
            ret.mobSkill = 0;
            ret.mobSkillLevel = 0;
            ret.target = 0;
        }

        MapleData mdds = source.getChildByPath("mob");
        if (mdds != null) {
            if (mdds.getChildren()!= null && mdds.getChildren().size() > 0) {
                ret.mob = MapleDataTool.getInt("mob", mdds, 0);
            }
        }
        ret.sourceid = sourceid;
        ret.skill = skill;
        if (!ret.skill && ret.duration > -1) {
            ret.overTime = true;
        } else {
            ret.duration *= 1000; // items have their times stored in ms, of course
            ret.overTime = overTime;
        }

        ArrayList<Pair<MapleBuffStat, BuffValueHolder>> statups = new ArrayList<>();
        ret.watk = (short) MapleDataTool.getInt("pad", source, 0);
        ret.wdef = (short) MapleDataTool.getInt("pdd", source, 0);
        ret.matk = (short) MapleDataTool.getInt("mad", source, 0);
        ret.mdef = (short) MapleDataTool.getInt("mdd", source, 0);
        ret.acc = (short) MapleDataTool.getIntConvert("acc", source, 0);
        ret.avoid = (short) MapleDataTool.getInt("eva", source, 0);
        ret.speed = (short) MapleDataTool.getInt("speed", source, 0);
        ret.jump = (short) MapleDataTool.getInt("jump", source, 0);

        /*ret.barrier = MapleDataTool.getInt("barrier", source, 0);
        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AURA, ret.barrier);*/
        
        /*ret.mapProtection = mapProtection(sourceid);
        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MAP_PROTECTION, Integer.valueOf(ret.mapProtection));*/

        if (ret.overTime && ret.getSummonMovementType() == null) {
            if (!skill) {
                if (isPyramidBuff(sourceid)) {
                    ret.berserk = MapleDataTool.getInt("berserk", source, 0);
                    ret.booster = MapleDataTool.getInt("booster", source, 0);

                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BERSERK_FURY, Integer.valueOf(ret.berserk));
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.BOOSTER, Integer.valueOf(ret.booster));

                } else if (isDojoBuff(sourceid) || isHpMpRecovery(sourceid)) {
                    ret.mhpR = (byte) MapleDataTool.getInt("mhpR", source, 0);
                    ret.mhpRRate = (short) (MapleDataTool.getInt("mhpRRate", source, 0) * 100);
                    ret.mmpR = (byte) MapleDataTool.getInt("mmpR", source, 0);
                    ret.mmpRRate = (short) (MapleDataTool.getInt("mmpRRate", source, 0) * 100);

                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.INC_EFFECT_HP_POTION, Integer.valueOf(ret.mhpR));
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.INC_EFFECT_MP_POTION, Integer.valueOf(ret.mmpR));

                /*} else if (isRateCoupon(sourceid)) {
                    switch (MapleDataTool.getInt("expR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP3, 1);
                            break;

                        case 4:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_EXP4, 1);
                            break;
                    }

                    switch (MapleDataTool.getInt("drpR", source, 0)) {
                        case 1:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP1, 1);
                            break;

                        case 2:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP2, 1);
                            break;

                        case 3:
                            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.COUPON_DRP3, 1);
                            break;
                    }*/
                } else if (isMonsterCard(sourceid)) {
                    int prob = 0, itemupCode = Integer.MAX_VALUE;
                    List<Pair<Integer, Integer>> areas = null;
                    boolean inParty = false;

                    MapleData con = source.getChildByPath("con");
                    if (con != null) {
                        areas = new ArrayList<>(3);

                        for (MapleData conData : con.getChildren()) {
                            int type = MapleDataTool.getInt("type", conData, -1);

                            if (type == 0) {
                                int startMap = MapleDataTool.getInt("sMap", conData, 0);
                                int endMap = MapleDataTool.getInt("eMap", conData, 0);

                                areas.add(new Pair<>(startMap, endMap));
                            } else if (type == 2) {
                                inParty = true;
                            }
                        }

                        if (areas.isEmpty()) {
                            areas = null;
                        }
                    }

                    if (MapleDataTool.getInt("mesoupbyitem", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MESO_UP_BY_ITEM, 4);
                        prob = MapleDataTool.getInt("prob", source, 1);
                    }

                    int itemupType = MapleDataTool.getInt("itemupbyitem", source, 0);
                    if (itemupType != 0) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ITEM_UP_BY_ITEM, 4);
                        prob = MapleDataTool.getInt("prob", source, 1);

                        switch (itemupType) {
                            case 2 -> itemupCode = MapleDataTool.getInt("itemCode", source, 1);
                            case 3 -> itemupCode = MapleDataTool.getInt("itemRange", source, 1);    // 3 digits
                        }
                    }

                    if (MapleDataTool.getInt("respectPimmune", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.RESPECT_PIMMUNE, 4);
                    }

                    if (MapleDataTool.getInt("respectMimmune", source, 0) != 0) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.RESPECT_MIMMUNE, 4);
                    }

                    if (MapleDataTool.getString("defenseAtt", source, null) != null) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.DEFENSE_ATT, 4);
                    }

                    if (MapleDataTool.getString("defenseState", source, null) != null) {
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.DEFENSE_STATE, 4);
                    }

                    int thaw = MapleDataTool.getInt("thaw", source, 0);
                    if (thaw != 0) { // // TODO: what is thaw?
                        addBuffStatPairToListIfNotZero(statups, MapleBuffStat.THAW, thaw > 0 ? 1 : 2);
                    }

                    ret.cardStats = new CardItemupStats(itemupCode, prob, areas, inParty);
                } /*else if (isExpIncrease(sourceid)) { TODO: do we need this? what is this
                    addBuffStatPairToListIfNotZero(statups, MapleBuffStat.EXP_INCREASE, MapleDataTool.getInt("expinc", source, 0));
                }*/
            }

            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WATK, Integer.valueOf(ret.watk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.WDEF, Integer.valueOf(ret.wdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MATK, Integer.valueOf(ret.matk));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.MDEF, Integer.valueOf(ret.mdef));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.ACC, Integer.valueOf(ret.acc));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.AVOID, Integer.valueOf(ret.avoid));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.SPEED, Integer.valueOf(ret.speed));
            addBuffStatPairToListIfNotZero(statups, MapleBuffStat.JUMP, Integer.valueOf(ret.jump));
        }

        MapleData ltd = source.getChildByPath("lt");
        if (ltd != null) {
            ret.lt = (Point) ltd.getData();
            ret.rb = (Point) source.getChildByPath("rb").getData();

            if (ServerConstants.USE_MAXRANGE_ECHO_OF_HERO && (sourceid == Beginner.ECHO_OF_HERO || sourceid == Noblesse.ECHO_OF_HERO || sourceid == Legend.ECHO_OF_HERO || sourceid == Evan.ECHO_OF_HERO)) {
                ret.lt = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
                ret.rb = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            }
        }

        int x = MapleDataTool.getInt("x", source, 0);

        ret.x = x;
        ret.y = MapleDataTool.getInt("y", source, 0);

        ret.damage = MapleDataTool.getIntConvert("damage", source, 100);
        ret.fixdamage = MapleDataTool.getIntConvert("fixdamage", source, -1);
        ret.attackCount = MapleDataTool.getIntConvert("attackCount", source, 1);
        ret.bulletCount = (byte) MapleDataTool.getIntConvert("bulletCount", source, 1);
        ret.bulletConsume = (byte) MapleDataTool.getIntConvert("bulletConsume", source, 0);
        ret.moneyCon = MapleDataTool.getIntConvert("moneyCon", source, 0);
        ret.itemCon = MapleDataTool.getInt("itemCon", source, 0);
        ret.itemConNo = MapleDataTool.getInt("itemConNo", source, 0);
        ret.moveTo = MapleDataTool.getInt("moveTo", source, -1);
        Map<MonsterStatus, Integer> monsterStatus = new ArrayMap<>();
        if (skill) {
            switch (sourceid) {
                // BEGINNER
                case Beginner.RECOVERY:
                case Noblesse.RECOVERY:
                case Legend.RECOVERY:
                case Evan.RECOVERY:
                    statups.add(new Pair<>(MapleBuffStat.RECOVERY, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Beginner.ECHO_OF_HERO:
                case Noblesse.ECHO_OF_HERO:
                case Legend.ECHO_OF_HERO:
                case Evan.ECHO_OF_HERO:
                    statups.add(new Pair<>(MapleBuffStat.ECHO_OF_HERO, new BuffValueHolder(sourceid, 0, ret.x)));
                    break;
                case Beginner.MONSTER_RIDER:
                case Noblesse.MONSTER_RIDER:
                case Legend.MONSTER_RIDER:
                case Corsair.BATTLE_SHIP:
                case Beginner.SPACESHIP:
                case Noblesse.SPACESHIP:
                case Beginner.YETI_MOUNT1:
                case Beginner.YETI_MOUNT2:
                case Noblesse.YETI_MOUNT1:
                case Noblesse.YETI_MOUNT2:
                case Legend.YETI_MOUNT1:
                case Legend.YETI_MOUNT2:
                case Beginner.WITCH_BROOMSTICK:
                case Noblesse.WITCH_BROOMSTICK:
                case Legend.WITCH_BROOMSTICK:
                case Beginner.BALROG_MOUNT:
                case Noblesse.BALROG_MOUNT:
                case Legend.BALROG_MOUNT:
                    statups.add(new Pair<>(MapleBuffStat.MONSTER_RIDING, new BuffValueHolder(sourceid, 0, sourceid)));
                    break;
                case Beginner.POWER_EXPLOSION:
                case Noblesse.POWER_EXPLOSION:
                case Legend.METEO_SHOWER:
                case Evan.POWER_EXPLOSION:
                    statups.add(new Pair<>(MapleBuffStat.BERSERK_FURY, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Beginner.INVINCIBLE_BARRIER:
                case Noblesse.INVINCIBLE_BARRIER:
                case Legend.INVICIBLE_BARRIER:
                case Evan.INVINCIBLE_BARRIER:
                    statups.add(new Pair<>(MapleBuffStat.DIVINE_BODY, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Fighter.POWER_GUARD:
                case Page.POWER_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.POWERGUARD, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Spearman.HYPER_BODY:
                case GM.HYPER_BODY:
                case SuperGM.HYPER_BODY:
                    statups.add(new Pair<>(MapleBuffStat.HYPERBODYHP, new BuffValueHolder(sourceid, 0, x)));
                    statups.add(new Pair<>(MapleBuffStat.HYPERBODYMP, new BuffValueHolder(sourceid, 0, ret.y)));
                    break;
                case Crusader.COMBO:
                case DawnWarrior.COMBO:
                    statups.add(new Pair<>(MapleBuffStat.COMBO, new BuffValueHolder(sourceid, 0, 1)));
                    break;
                case WhiteKnight.BW_FIRE_CHARGE:
                case WhiteKnight.BW_ICE_CHARGE:
                case WhiteKnight.BW_LIT_CHARGE:
                case WhiteKnight.SWORD_FIRE_CHARGE:
                case WhiteKnight.SWORD_ICE_CHARGE:
                case WhiteKnight.SWORD_LIT_CHARGE:
                case Paladin.BW_HOLY_CHARGE:
                case Paladin.SWORD_HOLY_CHARGE:
                case DawnWarrior.SOUL_CHARGE:
                case ThunderBreaker.LIGHTNING_CHARGE:
                    statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case DragonKnight.DRAGON_BLOOD:
                    statups.add(new Pair<>(MapleBuffStat.DRAGONBLOOD, new BuffValueHolder(sourceid, 0, ret.x)));
                    break;
                case DragonKnight.DRAGON_ROAR:
                    ret.hpR = -x / 100.0;
                    break;
                case Hero.STANCE:
                case Paladin.STANCE:
                case DarkKnight.STANCE:
                case Aran.FREEZE_STANDING:
                    statups.add(new Pair<>(MapleBuffStat.STANCE, new BuffValueHolder(sourceid, 0, iprop)));
                    break;
                case DawnWarrior.FINAL_ATTACK:
                    statups.add(new Pair<>(MapleBuffStat.SOUL_MASTER_FINAL, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case WindArcher.FINAL_ATTACK:
                    statups.add(new Pair<>(MapleBuffStat.WIND_BREAKER_FINAL, new BuffValueHolder(sourceid, 0, x)));
                    break;
                // MAGICIAN
                case Magician.MAGIC_GUARD:
                case BlazeWizard.MAGIC_GUARD:
                case Evan.MAGIC_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_GUARD, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Cleric.INVINCIBLE:
                    statups.add(new Pair<>(MapleBuffStat.INVINCIBLE, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Priest.HOLY_SYMBOL:
                case SuperGM.HOLY_SYMBOL:
                    statups.add(new Pair<>(MapleBuffStat.HOLY_SYMBOL, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case FPArchMage.INFINITY:
                case ILArchMage.INFINITY:
                case Bishop.INFINITY:
                    statups.add(new Pair<>(MapleBuffStat.INFINITY, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case FPArchMage.MANA_REFLECTION:
                case ILArchMage.MANA_REFLECTION:
                case Bishop.MANA_REFLECTION:
                    statups.add(new Pair<>(MapleBuffStat.MANA_REFLECTION, new BuffValueHolder(sourceid, 0, 1)));
                    break;
                case Bishop.HOLY_SHIELD:
                    statups.add(new Pair<>(MapleBuffStat.HOLY_SHIELD, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case BlazeWizard.ELEMENTAL_RESET:
                case Evan.ELEMENTAL_RESET:
                    statups.add(new Pair<>(MapleBuffStat.ELEMENTAL_RESET, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Evan.MAGIC_SHIELD:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_SHIELD, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Evan.MAGIC_RESISTANCE:
                    statups.add(new Pair<>(MapleBuffStat.MAGIC_RESIST, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Evan.SLOW:
                    statups.add(new Pair<>(MapleBuffStat.EVAN_SLOW, new BuffValueHolder(sourceid, 0, x)));
                    // BOWMAN
                case Priest.MYSTIC_DOOR:
                case Hunter.SOUL_ARROW:
                case Crossbowman.SOUL_ARROW:
                case WindArcher.SOUL_ARROW:
                    statups.add(new Pair<>(MapleBuffStat.SOULARROW, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Ranger.PUPPET:
                case Sniper.PUPPET:
                case WindArcher.PUPPET:
                case Outlaw.OCTOPUS:
                case Corsair.WRATH_OF_THE_OCTOPI:
                    statups.add(new Pair<>(MapleBuffStat.PUPPET, new BuffValueHolder(sourceid, 0, 1)));
                    break;
                case Bowmaster.CONCENTRATE:
                    statups.add(new Pair<>(MapleBuffStat.CONCENTRATE, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Bowmaster.HAMSTRING:
                    statups.add(new Pair<>(MapleBuffStat.HAMSTRING, new BuffValueHolder(sourceid, 0, x)));
                    monsterStatus.put(MonsterStatus.SPEED, x);
                    break;
                case Marksman.BLIND:
                    statups.add(new Pair<>(MapleBuffStat.BLIND, new BuffValueHolder(sourceid, 0, x)));
                    monsterStatus.put(MonsterStatus.ACC, x);
                    break;
                case Bowmaster.SHARP_EYES:
                case Marksman.SHARP_EYES:
                    statups.add(new Pair<>(MapleBuffStat.SHARP_EYES, new BuffValueHolder(sourceid, ret.x, ret.y)));
                    break;
                case WindArcher.WIND_WALK:
                    statups.add(new Pair<>(MapleBuffStat.WIND_WALK, new BuffValueHolder(sourceid, 0, 1)));
                    break;
                // THIEF
                case Rogue.DARK_SIGHT:
                case NightWalker.DARK_SIGHT:
                    statups.add(new Pair<>(MapleBuffStat.DARKSIGHT, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Hermit.MESO_UP:
                    statups.add(new Pair<>(MapleBuffStat.MESOUP, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Hermit.SHADOW_PARTNER:
                case NightWalker.SHADOW_PARTNER:
                    statups.add(new Pair<>(MapleBuffStat.SHADOWPARTNER, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case ChiefBandit.MESO_GUARD:
                    statups.add(new Pair<>(MapleBuffStat.MESOGUARD, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case ChiefBandit.PICKPOCKET:
                    statups.add(new Pair<>(MapleBuffStat.PICKPOCKET, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case NightLord.SHADOW_STARS:
                    statups.add(new Pair<>(MapleBuffStat.SHADOW_CLAW, new BuffValueHolder(sourceid, 0, 0)));
                    break;
                // PIRATE
                case Pirate.DASH:
                case ThunderBreaker.DASH:
                case Beginner.SPACE_DASH:
                case Noblesse.SPACE_DASH:
                    statups.add(new Pair<>(MapleBuffStat.DASH_SPEED, new BuffValueHolder(sourceid, 0, ret.x)));
                    statups.add(new Pair<>(MapleBuffStat.DASH_JUMP, new BuffValueHolder(sourceid, 0, ret.y)));
                    break;
                case Buccaneer.SPEED_INFUSION:
                case ThunderBreaker.SPEED_INFUSION:
                    statups.add(new Pair<>(MapleBuffStat.SPEED_INFUSION, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Outlaw.HOMING_BEACON:
                case Corsair.BULLSEYE:
                    statups.add(new Pair<>(MapleBuffStat.HOMING_BEACON, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case ThunderBreaker.SPARK:
                    statups.add(new Pair<>(MapleBuffStat.SPARK, new BuffValueHolder(sourceid, 0, x)));
                    break;
                // MULTIPLE
                case Aran.POLEARM_BOOSTER:
                case Fighter.AXE_BOOSTER:
                case Fighter.SWORD_BOOSTER:
                case Page.BW_BOOSTER:
                case Page.SWORD_BOOSTER:
                case Spearman.POLEARM_BOOSTER:
                case Spearman.SPEAR_BOOSTER:
                case Hunter.BOW_BOOSTER:
                case Crossbowman.CROSSBOW_BOOSTER:
                case Assassin.CLAW_BOOSTER:
                case Bandit.DAGGER_BOOSTER:
                case FPMage.SPELL_BOOSTER:
                case ILMage.SPELL_BOOSTER:
                case Brawler.KNUCKLER_BOOSTER:
                case Gunslinger.GUN_BOOSTER:
                case DawnWarrior.SWORD_BOOSTER:
                case BlazeWizard.SPELL_BOOSTER:
                case WindArcher.BOW_BOOSTER:
                case NightWalker.CLAW_BOOSTER:
                case ThunderBreaker.KNUCKLER_BOOSTER:
                case Evan.MAGIC_BOOSTER:
                    statups.add(new Pair<>(MapleBuffStat.BOOSTER, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Hero.MAPLE_WARRIOR:
                case Paladin.MAPLE_WARRIOR:
                case DarkKnight.MAPLE_WARRIOR:
                case FPArchMage.MAPLE_WARRIOR:
                case ILArchMage.MAPLE_WARRIOR:
                case Bishop.MAPLE_WARRIOR:
                case Bowmaster.MAPLE_WARRIOR:
                case Marksman.MAPLE_WARRIOR:
                case NightLord.MAPLE_WARRIOR:
                case Shadower.MAPLE_WARRIOR:
                case Corsair.MAPLE_WARRIOR:
                case Buccaneer.MAPLE_WARRIOR:
                case Aran.MAPLE_WARRIOR:
                case Evan.MAPLE_WARRIOR:
                    statups.add(new Pair<>(MapleBuffStat.MAPLE_WARRIOR, new BuffValueHolder(sourceid, 0, ret.x)));
                    break;
                // SUMMON
                case Ranger.SILVER_HAWK:
                case Sniper.GOLDEN_EAGLE:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffValueHolder(sourceid, 0, 1)));
                    monsterStatus.put(MonsterStatus.STUN, 1);
                    break;
                case FPArchMage.ELQUINES:
                case Marksman.FROST_PREY:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffValueHolder(sourceid, 0, 1)));
                    monsterStatus.put(MonsterStatus.FREEZE, 1);
                    break;
                case Priest.SUMMON_DRAGON:
                case Bowmaster.PHOENIX:
                case ILArchMage.IFRIT:
                case Bishop.BAHAMUT:
                case DarkKnight.BEHOLDER:
                case Outlaw.GAVIOTA:
                case DawnWarrior.SOUL:
                case BlazeWizard.FLAME:
                case WindArcher.STORM:
                case NightWalker.DARKNESS:
                case ThunderBreaker.LIGHTNING:
                case BlazeWizard.IFRIT:
                    statups.add(new Pair<>(MapleBuffStat.SUMMON, new BuffValueHolder(sourceid, 0, 1)));
                    break;
                // ----------------------------- MONSTER STATUS ---------------------------------- //
                case Rogue.DISORDER:
                    monsterStatus.put(MonsterStatus.WATK, ret.x);
                    monsterStatus.put(MonsterStatus.WDEF, ret.y);
                    break;
                case Corsair.HYPNOTIZE:
                    monsterStatus.put(MonsterStatus.DAZZLE, 1);
                    break;
                case NightLord.NINJA_AMBUSH:
                case Shadower.NINJA_AMBUSH:
                    monsterStatus.put(MonsterStatus.NINJA_AMBUSH, ret.damage);
                    break;
                case Page.THREATEN:
                    monsterStatus.put(MonsterStatus.WATK, ret.x);
                    monsterStatus.put(MonsterStatus.WDEF, ret.y);
                    break;
                case Crusader.AXE_PANIC:
                case Crusader.SWORD_PANIC:
                    monsterStatus.put(MonsterStatus.DARKNESS, ret.x);
                    break;
                case Crusader.AXE_COMA:
                case Crusader.SWORD_COMA:
                case Crusader.SHOUT:
                case WhiteKnight.CHARGE_BLOW:
                case Hunter.ARROW_BOMB:
                case ChiefBandit.ASSAULTER:
                case Shadower.BOOMERANG_STEP:
                case Brawler.BACK_SPIN_BLOW:
                case Brawler.DOUBLE_UPPERCUT:
                case Buccaneer.DEMOLITION:
                case Buccaneer.SNATCH:
                case Buccaneer.BARRAGE:
                case Gunslinger.BLANK_SHOT:
                case DawnWarrior.COMA:
                case Aran.ROLLING_SPIN:
                case Evan.FIRE_BREATH:
                case Evan.BLAZE:
                    monsterStatus.put(MonsterStatus.STUN, 1);
                    break;
                case NightLord.TAUNT:
                case Shadower.TAUNT:
                    monsterStatus.put(MonsterStatus.SHOWDOWN, ret.x);
                    monsterStatus.put(MonsterStatus.MDEF, ret.x);
                    monsterStatus.put(MonsterStatus.WDEF, ret.x);
                    break;
                case ILWizard.COLD_BEAM:
                case ILMage.ICE_STRIKE:
                case ILArchMage.BLIZZARD:
                case ILMage.ELEMENT_COMPOSITION:
                case Sniper.BLIZZARD:
                case Outlaw.ICE_SPLITTER:
                case FPArchMage.PARALYZE:
                case Aran.COMBO_TEMPEST:
                case Evan.ICE_BREATH:
                    monsterStatus.put(MonsterStatus.FREEZE, 1);
                    break;
                case FPWizard.SLOW:
                case ILWizard.SLOW:
                case BlazeWizard.SLOW:
                    monsterStatus.put(MonsterStatus.SPEED, ret.x);
                    break;
                case FPWizard.POISON_BREATH:
                case FPMage.ELEMENT_COMPOSITION:
                    monsterStatus.put(MonsterStatus.POISON, 1);
                    break;
                case Priest.DOOM:
                    monsterStatus.put(MonsterStatus.DOOM, 1);
                    break;
                case ILMage.SEAL:
                case FPMage.SEAL:
                    monsterStatus.put(MonsterStatus.SEAL, 1);
                    break;
                case Hermit.SHADOW_WEB: // shadow web
                case NightWalker.SHADOW_WEB:
                    monsterStatus.put(MonsterStatus.SHADOW_WEB, 1);
                    break;
                case FPArchMage.FIRE_DEMON:
                case ILArchMage.ICE_DEMON:
                    monsterStatus.put(MonsterStatus.POISON, 1);
                    monsterStatus.put(MonsterStatus.FREEZE, 1);
                    break;
                case Evan.PHANTOM_IMPRINT:
                    monsterStatus.put(MonsterStatus.WEAKEN, x);
                    //ARAN
                case Aran.FINAL_CROSS:
                    monsterStatus.put(MonsterStatus.RISE_BY_TOSS, ret.x);
                    break;
                case Aran.COMBO_ABILITY:
                    statups.add(new Pair<>(MapleBuffStat.ARAN_COMBO, new BuffValueHolder(sourceid, 0, 100)));
                    break;
                case Aran.COMBO_BARRIER:
                    statups.add(new Pair<>(MapleBuffStat.COMBO_BARRIER, new BuffValueHolder(sourceid, 0, ret.x)));
                    break;
                case Aran.COMBO_DRAIN:
                    statups.add(new Pair<>(MapleBuffStat.COMBO_DRAIN, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Aran.SMART_KNOCKBACK:
                    statups.add(new Pair<>(MapleBuffStat.SMART_KNOCKBACK, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Aran.BODY_PRESSURE:
                    statups.add(new Pair<>(MapleBuffStat.BODY_PRESSURE, new BuffValueHolder(sourceid, 0, x)));
                    break;
                case Aran.SNOW_CHARGE:
                    statups.add(new Pair<>(MapleBuffStat.WK_CHARGE, new BuffValueHolder(sourceid, 0, ret.duration)));
                    break;
                default:
                    break;
            }
        }
        if (ret.isMorph()) {
            statups.add(new Pair<>(MapleBuffStat.MORPH, new BuffValueHolder(sourceid, 0, ret.getMorph())));
        }
        if (ret.ghost > 0 && !skill) {
            statups.add(new Pair<>(MapleBuffStat.GHOST_MORPH, new BuffValueHolder(sourceid, 0, ret.ghost)));
        }
        ret.monsterStatus = monsterStatus;
        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    /**
     * @param applyto
     * @param obj
     * @param attack damage done by the skill
     */
    public void applyPassive(MapleCharacter applyto, MapleMapObject obj, int attack) {
        if (makeChanceResult()) {
            switch (sourceid) { // MP eater
                case FPWizard.MP_EATER, ILWizard.MP_EATER, Cleric.MP_EATER -> {
                    if (obj == null || obj.getType() != MapleMapObjectType.MONSTER) {
                        return;
                    }
                    MapleMonster mob = (MapleMonster) obj; // x is absorb percentage
                    if (!mob.isBoss()) {
                        int absorbMp = Math.min((int) (mob.getMaxMp() * (getX() / 100.0)), mob.getMp());
                        if (absorbMp > 0) {
                            mob.setMp(mob.getMp() - absorbMp);
                            applyto.addMP(absorbMp);
                            applyto.announce(MaplePacketCreator.showOwnBuffEffect(sourceid, 1));
                            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1), false);
                        }
                    }
                }
            }
        }
    }

    public boolean applyEchoOfHero(MapleCharacter applyfrom) {
        Map<Integer, MapleCharacter> mapPlayers = applyfrom.getMap().getMapPlayers();
        mapPlayers.remove(applyfrom.getId());

        boolean hwResult = applyTo(applyfrom);
        for (MapleCharacter chr : mapPlayers.values()) {    // Echo of Hero not buffing players in the map detected thanks to Masterrulax
            applyTo(applyfrom, chr, false, null, false);
        }

        return hwResult;
    }

    public boolean applyTo(MapleCharacter chr) {
        return applyTo(chr, chr, true, null, false);
    }

    public boolean applyTo(MapleCharacter chr, Point pos) {
        return applyTo(chr, chr, true, pos, false);
    }

    public boolean applyTo(MapleCharacter chr, boolean itemBypass) {
        return applyTo(chr, chr, true, null, itemBypass);
    }

    //added itemBypass to use buffs without consuming items (started as a quick hack fix for !buffme lol.)
    private boolean applyTo(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary, Point pos, boolean itemBypass) {
        if (skill && (sourceid == GM.HIDE || sourceid == SuperGM.HIDE)) {
            applyto.toggleHide(false);
            return true;
        }
        int hpchange = calcHPChange(applyfrom, primary);
        int mpchange = calcMPChange(applyfrom, primary);

        List<Pair<MapleStat, Integer>> hpmpupdate = new ArrayList<>(2);
        if (!primary && isResurrection()) {
            hpchange = applyto.getMaxHp();
            applyto.setStance(0);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.removePlayerFromMap(applyto.getId()), false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.spawnPlayerMapObject(applyfrom.getClient(), applyto), false);
        }
        if (canDispel()) {
            List<MapleBuffStat> toDispel = new ArrayList<>();
            if (poison)
                toDispel.add(MapleBuffStat.POISON);
            if (seal)
                toDispel.add(MapleBuffStat.SEAL);
            if (darkness)
                toDispel.add(MapleBuffStat.DARKNESS);
            if (weakness)
                toDispel.add(MapleBuffStat.WEAKEN);
            if (curse)
                toDispel.add(MapleBuffStat.CURSE);

            applyto.cancelPlayerBuffs(toDispel);
        }

        if (isDispel() && makeChanceResult()) {
            applyto.dispelDebuffs();
        } else if (isHerosWill(sourceid)) {
            applyto.cancelBuffStats(MapleBuffStat.SEDUCE);
        }
        if (isComboReset()) {
            applyto.setCombo((short) 0);
        }
        if (hpchange != 0) {
            int newHp = applyto.getHp() + hpchange;
            if (skill) {
                if (hpchange < 0 && (-hpchange) > applyto.getHp()) {
                    return false;
                }
                if (newHp < 1) {
                    newHp = 1;
                }
            }
            applyto.updateHp(newHp);
            hpmpupdate.add(new Pair<>(MapleStat.HP, Integer.valueOf(applyto.getHp())));
        }
        if (mpchange != 0) {
            int newMp = applyto.getMp() + mpchange;
            if (skill) {
                if (mpchange < 0 && (-mpchange) > applyto.getMp()) {
                    return false;
                }
                if (newMp < 1) {
                    newMp = 1;
                }
            }
            applyto.updateMp(newMp);
            hpmpupdate.add(new Pair<>(MapleStat.MP, Integer.valueOf(applyto.getMp())));
        }
        applyto.getClient().announce(MaplePacketCreator.updatePlayerStats(hpmpupdate, true, applyto));
        if (moveTo != -1) {
            if (moveTo != applyto.getMapId()) {
                MapleMap target;
                if (moveTo == 999999999) {
                    target = applyto.getMap().getReturnMap();
                } else {
                    target = applyto.getClient().getWorldServer().getChannel(applyto.getClient().getChannel()).getMapFactory().getMap(moveTo);
                    int targetid = target.getId() / 10000000;
                    if (targetid != 60 && applyto.getMapId() / 10000000 != 61 && targetid != applyto.getMapId() / 10000000 && targetid != 21 && targetid != 20 && targetid != 12 && (applyto.getMapId() / 10000000 != 10 && applyto.getMapId() / 10000000 != 12)) {
                        return false;
                    }
                }
                applyto.changeMap(target);
            } else {
                return false;
            }
        }
        if (isShadowClaw()) {
            int projectile = 0;
            MapleInventory use = applyto.getInventory(MapleInventoryType.USE);
            for (int i = 1; i <= use.getSlotLimit(); i++) { // impose order...
                Item item = use.getItem((short) i);
                if (item != null) {
                    if (ItemConstants.isThrowingStar(item.getItemId()) && item.getQuantity() >= 200) {
                        projectile = item.getItemId();
                        break;
                    }
                }
            }
            if (projectile == 0) {
                return false;
            } else {
                if (!applyto.isGM()) {
                    MapleInventoryManipulator.removeById(applyto.getClient(), MapleInventoryType.USE, projectile, 200, false, true);
                }
            }

        }
        SummonMovementType summonMovementType = getSummonMovementType();
        if (overTime || isCygnusFA() || summonMovementType != null) {
            applyBuffEffect(applyfrom, applyto, primary);
        }

        if (primary && (overTime || isHeal())) {
            applyBuff(applyfrom);
        }

        if (primary && isMonsterBuff()) {
            applyMonsterBuff(applyfrom);
        }

        if (this.getFatigue() != 0) {
            applyto.getMount().setTiredness(applyto.getMount().getTiredness() + this.getFatigue());
        }

        if (summonMovementType != null && pos != null) {
            final MapleSummon tosummon = new MapleSummon(applyfrom, sourceid, pos, summonMovementType);
            applyfrom.getMap().spawnSummon(tosummon);
            applyfrom.addSummon(sourceid, tosummon);
            tosummon.addHP(x);
            if (isBeholder()) {
                tosummon.addHP(1);
            }
        }
        if (isMagicDoor() && !FieldLimit.DOOR.check(applyto.getMap().getFieldLimit())) { // Magic Door
            if (applyto.getPosition().distanceSq(pos) > 100) {
                // generally, this is a PE or the slope is too strange for even
                // the CLIENT to know where to spawn the door
                // this will generally prevent map crashes involving
                // Mystic Door placement
                return false;
            }

            MapleDoor door = new MapleDoor(this, applyto.getMap(), applyto, pos); // door object

            applyto.setDoor(door); // door for this player
            applyto.getMPC().updateDoor(door);
            door.getTown().spawnDoor(door.getTownDoor());
            door.getTarget().spawnDoor(door.getTargetDoor());

            // the technically proper way to do it, but silentPartyUpdate works equally as well
//            if (applyto.getParty() != null) {
//                applyto.getClient().getWorldServer().updateParty(applyto.getPartyId(), PartyOperation.MYSTIC_DOOR, applyto.getMPC());
//            }
            applyto.silentPartyUpdate();
        } else if (isMist()) {
            Rectangle bounds = calculateBoundingBox(sourceid == NightWalker.POISON_BOMB ? pos : applyfrom.getPosition(), applyfrom.isFacingLeft());
            MapleMist mist = new MapleMist(bounds, applyfrom, this);
            applyfrom.getMap().spawnMist(mist, getDuration(), mist.isPoisonMist(), false, mist.isRecoveryMist());
        } else if (isTimeLeap()) {
            applyto.removeAllCooldownsExcept(Buccaneer.TIME_LEAP, true);
        }

        // don't consume item until we know for sure there's nothing left...
        // though, I don't even know if we check for item consumption (lol)
        // main reason this is moved is to accomodate people who cannot
        // cast mystic door on that given foothold
        if (primary && !itemBypass) {
            if (itemConNo != 0) {
                MapleInventoryManipulator.removeById(applyto.getClient(),
                        ItemConstants.getInventoryType(itemCon), itemCon, itemConNo, false, true);
            }
        }

        return true;
    }

    private void applyBuff(MapleCharacter applyfrom) {
        if (isPartyBuff() && (applyfrom.getParty() != null || isGmBuff())) {
            Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
            List<MapleMapObject> affecteds = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.PLAYER));
            List<MapleCharacter> affectedp = new ArrayList<>(affecteds.size());
            for (MapleMapObject affectedmo : affecteds) {
                MapleCharacter affected = (MapleCharacter) affectedmo;
                if (affected != applyfrom && (isGmBuff() || applyfrom.getParty().equals(affected.getParty()))) {
                    if ((isResurrection() && !affected.isAlive()) || (!isResurrection() && affected.isAlive())) {
                        affectedp.add(affected);
                    }
                }
            }
            for (MapleCharacter affected : affectedp) {
                applyTo(applyfrom, affected, false, null, false);
                affected.announce(MaplePacketCreator.showOwnBuffEffect(sourceid, 2));
                affected.getMap().broadcastMessage(affected, MaplePacketCreator.showBuffeffect(affected.getId(), sourceid, 2), false);
            }
        }
    }

    private void applyMonsterBuff(MapleCharacter applyfrom) {
        Rectangle bounds = calculateBoundingBox(applyfrom.getPosition(), applyfrom.isFacingLeft());
        List<MapleMapObject> affected = applyfrom.getMap().getMapObjectsInRect(bounds, Arrays.asList(MapleMapObjectType.MONSTER));
        PlayerSkill skill_ = SkillFactory.getSkill(sourceid);
        int i = 0;
        for (MapleMapObject mo : affected) {
            MapleMonster monster = (MapleMonster) mo;
            if (isDispel()) {
                monster.debuffMob(skill_.getId());
            } else if (makeChanceResult()) {
                monster.applyStatus(applyfrom, new MonsterStatusEffect(getMonsterStati(), skill_, null, false, getDuration()), isPoison());
                if (isCrash()) {
                    monster.debuffMob(skill_.getId());
                }
            }
            i++;
            if (i >= mobCount) {
                break;
            }
        }
    }

    public static MapleStatEffect loadDebuffEffectFromMobSkill(MobSkill source) {
        MapleStatEffect ret = new MapleStatEffect();
        ret.skill = true;
        ret.sourceid = source.getSkillId();
        ret.sourceLevel = source.getSkillLevel();
        ret.disease = true;
        ret.overTime = true;

        ret.x = source.getX();
        ret.y = source.getY();

        ret.duration = (int) source.getDuration();
        ret.cooldown = (int) source.getCoolTime();

        ret.rb = source.getRb();
        ret.lt = source.getLt();
        ArrayList<Pair<MapleBuffStat, BuffValueHolder>> statups = new ArrayList<>();
        switch (ret.sourceid) {
            case 120:
                statups.add(new Pair<>(MapleBuffStat.SEAL, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 121:
                statups.add(new Pair<>(MapleBuffStat.DARKNESS, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 122:
                statups.add(new Pair<>(MapleBuffStat.WEAKEN, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 123:
                statups.add(new Pair<>(MapleBuffStat.STUN, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 124:
                statups.add(new Pair<>(MapleBuffStat.CURSE, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 125:
                statups.add(new Pair<>(MapleBuffStat.POISON, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 126:
                statups.add(new Pair<>(MapleBuffStat.SLOW, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 129:
                statups.add(new Pair<>(MapleBuffStat.BAN_MAP, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
            case 128:
                statups.add(new Pair<>(MapleBuffStat.SEDUCE, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 132:
                statups.add(new Pair<>(MapleBuffStat.CONFUSE, new BuffValueHolder(source.getSkillId(), source.getSkillLevel(), ret.x)));
                break;
            case 133:
                //statups.add(new Pair<>(MapleBuffStat.ZOMBIFY, buffVal)); // Ignore for now, need to figure out the packet first.
                break;
        }

        statups.trimToSize();
        ret.statups = statups;
        return ret;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        Point mylt;
        Point myrb;
        if (facingLeft) {
            mylt = new Point(lt.x + posFrom.x, lt.y + posFrom.y);
            myrb = new Point(rb.x + posFrom.x, rb.y + posFrom.y);
        } else {
            myrb = new Point(-lt.x + posFrom.x, rb.y + posFrom.y);
            mylt = new Point(-rb.x + posFrom.x, lt.y + posFrom.y);
        }
        Rectangle bounds = new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
        return bounds;
    }

    public int getBuffLocalDuration() {
        return duration;
    }

    public void silentApplyBuff(MapleCharacter chr, long localStartTime) {
        int localDuration = duration;
        localDuration = alchemistModifyVal(chr, localDuration, false);
        CancelEffectAction cancelAction = new CancelEffectAction(chr, this, localStartTime);
        ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((localStartTime + localDuration) - System.currentTimeMillis()));
        chr.registerEffect(this, localStartTime, schedule, statups);
        SummonMovementType summonMovementType = getSummonMovementType();
        if (summonMovementType != null) {
            final MapleSummon tosummon = new MapleSummon(chr, sourceid, chr.getPosition(), summonMovementType);
            if (!tosummon.isStationary()) {
                chr.addSummon(sourceid, tosummon);
                tosummon.addHP(x);
            }
        }
        if (sourceid == Corsair.BATTLE_SHIP) {
            chr.announceBattleshipHp();
        }
    }

    public final void applyComboBuff(final MapleCharacter applyto, int combo) {
        final List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.ARAN_COMBO, new BuffValueHolder(0, 0, combo)));
        applyto.announce(MaplePacketCreator.giveBuff(sourceid, 99999, stat));

        final long starttime = Server.getInstance().getCurrentTime();
//	final CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
//	final ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, ((starttime + 99999) - Server.getInstance().getCurrentTime()));
        applyto.registerEffect(this, starttime, null, statups);
    }

    public final void applyBeaconBuff(final MapleCharacter applyto, int objectid) { // thanks Thora & Hyun for reporting an issue with homing beacon autoflagging mobs when changing maps
        final List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.HOMING_BEACON, new BuffValueHolder(0, 0, objectid)));
        applyto.announce(MaplePacketCreator.giveBuff(1, sourceid, stat));

        final long starttime = Server.getInstance().getCurrentTime();
        applyto.registerEffect(this, starttime, null, statups);
    }

    public void updateBuffEffect(MapleCharacter target, List<Pair<MapleBuffStat, BuffValueHolder>> activeStats, long starttime) {
        int localDuration = getBuffLocalDuration();
        localDuration = alchemistModifyVal(target, localDuration, false);

        long leftDuration = (starttime + localDuration) - Server.getInstance().getCurrentTime();
        if (leftDuration > 0) {
            target.announce(MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), (int) leftDuration, activeStats));
        }
    }

    private void applyBuffEffect(MapleCharacter applyfrom, MapleCharacter applyto, boolean primary) {
        if (!isMonsterRiding() && !isCouponBuff() && !isHyperBody() && !isCombo()) {
            applyto.cancelEffect(this, true, -1);
        }

        List<Pair<MapleBuffStat, BuffValueHolder>> localstatups = statups;
        int localDuration = getBuffLocalDuration();
        int localsourceid = sourceid;
        int seconds = localDuration / 1000;
        MapleMount givemount = null;
        if (isMonsterRiding()) {
            int ridingMountId = 0;
            Item mount = applyfrom.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -18);
            if (mount != null) {
                ridingMountId = mount.getItemId();
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                ridingMountId = 1932000;
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                ridingMountId = 1932000 + applyto.getSkillLevel(sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                ridingMountId = 1932003;
            } else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                ridingMountId = 1932004;
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                ridingMountId = 1932005;
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                ridingMountId = 1932010;
            } else {
                if (applyto.getMount() == null) {
                    applyto.mount(ridingMountId, sourceid);
                }

                applyto.getClient().getWorldServer().registerMountHunger(applyto);
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                givemount = new MapleMount(applyto, 1932000, sourceid);
            } else if (sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP) {
                givemount = new MapleMount(applyto, 1932000 + applyto.getSkillLevel(sourceid), sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT1) {
                givemount = new MapleMount(applyto, 1932003, sourceid);
            } else if (sourceid == Beginner.YETI_MOUNT2 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Legend.YETI_MOUNT2) {
                givemount = new MapleMount(applyto, 1932004, sourceid);
            } else if (sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Legend.WITCH_BROOMSTICK) {
                givemount = new MapleMount(applyto, 1932005, sourceid);
            } else if (sourceid == Beginner.BALROG_MOUNT || sourceid == Noblesse.BALROG_MOUNT || sourceid == Legend.BALROG_MOUNT) {
                givemount = new MapleMount(applyto, 1932010, sourceid);
            } else {
                givemount = applyto.getMount();
            }
            localDuration = sourceid;
            localsourceid = ridingMountId;
            localstatups = Collections.singletonList(new Pair<>(MapleBuffStat.MONSTER_RIDING, new BuffValueHolder(sourceid, 0, ridingMountId)));
        } else if (isSkillMorph()) {
            for (int i = 0; i < localstatups.size(); i++) {
                if (localstatups.get(i).getLeft().equals(MapleBuffStat.MORPH)) {
                    localstatups.set(i, new Pair<>(MapleBuffStat.MORPH, new BuffValueHolder(sourceid, 0, getMorph(applyto))));
                    break;
                }
            }
        }
        if (primary) {
            localDuration = alchemistModifyVal(applyfrom, localDuration, false);
            applyto.getMap().broadcastMessage(applyto, MaplePacketCreator.showBuffeffect(applyto.getId(), sourceid, 1, (byte) 3), false);
        }
        if (localstatups.size() > 0) {
            byte[] buff = null;
            byte[] mbuff = null;
            if (getSummonMovementType() == null && this.isActive(applyto)) {
                buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups);
            }
            if (isDisease()) {
                buff = MaplePacketCreator.giveBuff((sourceLevel << 16 | sourceid), localDuration, localstatups);
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), localstatups);
            } else if (getSummonMovementType() == null) {
                buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, localstatups);
            }
            if (isDash()) {
                buff = MaplePacketCreator.givePirateBuff(statups, sourceid, seconds);
                mbuff = MaplePacketCreator.giveForeignPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isInfusion()) {
                buff = MaplePacketCreator.givePirateBuff(localstatups, sourceid, seconds);
                mbuff = MaplePacketCreator.giveForeignPirateBuff(applyto.getId(), sourceid, seconds, localstatups);
            } else if (isDs()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.DARKSIGHT,  new BuffValueHolder(0, 0, 0)));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat);
            } else if (isWw()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> dsstat = Collections.singletonList(new Pair<>(MapleBuffStat.WIND_WALK,  new BuffValueHolder(0, 0, 0)));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), dsstat);
            } else if (isCombo()) {
                Integer comboCount = applyto.getBuffedValue(MapleBuffStat.COMBO);
                if (comboCount == null) comboCount = 0;

                List<Pair<MapleBuffStat, BuffValueHolder>> cbstat = Collections.singletonList(new Pair<>(MapleBuffStat.COMBO,new BuffValueHolder(0, 0, comboCount)));
                buff = MaplePacketCreator.giveBuff((skill ? sourceid : -sourceid), localDuration, cbstat);
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), cbstat);
            } else if (isMonsterRiding()) {
                if (sourceid == Corsair.BATTLE_SHIP) {//hp
                    if (applyto.getBattleshipHp() <= 0) {
                        applyto.resetBattleshipHp();
                    }

                    localstatups = statups;
                }
                buff = MaplePacketCreator.giveBuff(localsourceid, localDuration, localstatups);
                mbuff = MaplePacketCreator.showMonsterRiding(applyto.getId(), givemount);
                localDuration = duration;
            } else if (isShadowPartner()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SHADOWPARTNER, new BuffValueHolder(0, 0, 0)));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isSoulArrow()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.SOULARROW, new BuffValueHolder(0, 0, 0)));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isEnrage()) {
                applyto.handleOrbconsume();
            } else if (isMorph()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.MORPH, new BuffValueHolder(sourceid, 0, getMorph(applyto))));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            } else if (isAriantShield()) {
                List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(MapleBuffStat.DOJANG_SHIELD,  new BuffValueHolder(sourceid, 0, 0)));
                mbuff = MaplePacketCreator.giveForeignBuff(applyto.getId(), stat);
            }

            if (buff != null) {
                if (!hasNoIcon()) { //Thanks flav for such a simple release! :)
                    applyto.announce(buff);
                } else {
                    System.out.println("<Error> NO buff icon for id " + sourceid);
                }
            }

            long starttime = Server.getInstance().getCurrentTime();
            CancelEffectAction cancelAction = new CancelEffectAction(applyto, this, starttime);
            ScheduledFuture<?> schedule = TimerManager.getInstance().schedule(cancelAction, localDuration);
            applyto.registerEffect(this, starttime, schedule, localstatups);

            if (mbuff != null) {
                applyto.getMap().broadcastMessage(applyto, mbuff, false);
            }
            if (sourceid == Corsair.BATTLE_SHIP) {
                applyto.announceBattleshipHp();
            }
        }
    }

    private int calcHPChange(MapleCharacter applyfrom, boolean primary) {
        int hpchange = 0;
        if (hp != 0) {
            if (!skill) {
                if (primary) {
                    hpchange += alchemistModifyVal(applyfrom, hp, true);
                } else {
                    hpchange += hp;
                }
            } else {
                hpchange += makeHealHP(hp / 100.0, applyfrom.getTotalMagic(), 3, 5);
            }
        }
        if (hpR != 0) {
            hpchange += (int) (applyfrom.getCurrentMaxHp() * hpR);
            applyfrom.checkBerserk(true); // or perhaps false jeeeez
        }
        if (primary) {
            if (hpCon != 0) {
                hpchange -= hpCon;
            }
        }
        if (isChakra()) {
            hpchange += makeHealHP(getY() / 100.0, applyfrom.getTotalLuk(), 2.3, 3.5);
        } else if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            hpchange += (applyfrom.getMaxHp() - applyfrom.getHp());
        }
        return hpchange;
    }

    private int makeHealHP(double rate, double stat, double lowerfactor, double upperfactor) {
        return (int) ((Math.random() * ((int) (stat * upperfactor * rate) - (int) (stat * lowerfactor * rate) + 1)) + (int) (stat * lowerfactor * rate));
    }

    private int calcMPChange(MapleCharacter applyfrom, boolean primary) {
        int mpchange = 0;
        if (mp != 0) {
            if (primary) {
                mpchange += alchemistModifyVal(applyfrom, mp, true);
            } else {
                mpchange += mp;
            }
        }
        if (mpR != 0) {
            mpchange += (int) (applyfrom.getCurrentMaxMp() * mpR);
        }
        if (primary) {
            if (mpCon != 0) {
                double mod = 1.0;
                boolean isAFpMage = applyfrom.getJob().isA(MapleJob.FP_MAGE);
                boolean isCygnus = applyfrom.getJob().isA(MapleJob.BLAZEWIZARD2);
                boolean isEvan = applyfrom.getJob().isA(MapleJob.EVAN7);
                if (isAFpMage || isCygnus || isEvan || applyfrom.getJob().isA(MapleJob.IL_MAGE)) {
                    PlayerSkill amp = isAFpMage ? SkillFactory.getSkill(FPMage.ELEMENT_AMPLIFICATION) : (isCygnus ? SkillFactory.getSkill(BlazeWizard.ELEMENT_AMPLIFICATION) : (isEvan ? SkillFactory.getSkill(Evan.MAGIC_AMPLIFICATION) : SkillFactory.getSkill(ILMage.ELEMENT_AMPLIFICATION)));
                    int ampLevel = applyfrom.getSkillLevel(amp);
                    if (ampLevel > 0) {
                        mod = amp.getEffect(ampLevel).getX() / 100.0;
                    }
                }
                mpchange -= mpCon * mod;
                if (applyfrom.getBuffedValue(MapleBuffStat.INFINITY) != null) {
                    mpchange = 0;
                } else if (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE) != null) {
                    mpchange -= (int) (mpchange * (applyfrom.getBuffedValue(MapleBuffStat.CONCENTRATE).doubleValue() / 100));
                }
            }
        }
        if (sourceid == SuperGM.HEAL_PLUS_DISPEL) {
            mpchange += applyfrom.getCurrentMaxMp();
        }

        return mpchange;
    }

    private int alchemistModifyVal(MapleCharacter chr, int val, boolean withX) {
        if (!skill && (chr.getJob().isA(MapleJob.HERMIT) || chr.getJob().isA(MapleJob.NIGHTWALKER3))) {
            MapleStatEffect alchemistEffect = getAlchemistEffect(chr);
            if (alchemistEffect != null) {
                return (int) (val * ((withX ? alchemistEffect.getX() : alchemistEffect.getY()) / 100.0));
            }
        }
        return val;
    }

    private MapleStatEffect getAlchemistEffect(MapleCharacter chr) {
        int id = Hermit.ALCHEMIST;
        if (chr.isCygnus()) {
            id = NightWalker.ALCHEMIST;
        }
        int alchemistLevel = chr.getSkillLevel(SkillFactory.getSkill(id));
        return alchemistLevel == 0 ? null : SkillFactory.getSkill(id).getEffect(alchemistLevel);
    }

    private boolean isGmBuff() {
        return switch (sourceid) {
            case Beginner.ECHO_OF_HERO, Noblesse.ECHO_OF_HERO, Legend.ECHO_OF_HERO,
                    Evan.ECHO_OF_HERO, SuperGM.HEAL_PLUS_DISPEL, SuperGM.HASTE,
                    SuperGM.HOLY_SYMBOL, SuperGM.BLESS, SuperGM.RESURRECTION, SuperGM.HYPER_BODY -> true;
            default -> false;
        };
    }

    private boolean isMonsterBuff() {
        if (!skill) {
            return false;
        }
        return switch (sourceid) {
            case Page.THREATEN, FPWizard.SLOW, ILWizard.SLOW, FPMage.SEAL,
                    ILMage.SEAL, Priest.DOOM, Hermit.SHADOW_WEB, NightLord.NINJA_AMBUSH,
                    Shadower.NINJA_AMBUSH, BlazeWizard.SLOW, BlazeWizard.SEAL,
                    NightWalker.SHADOW_WEB, Crusader.ARMOR_CRASH, DragonKnight.POWER_CRASH,
                    WhiteKnight.MAGIC_CRASH, Priest.DISPEL, SuperGM.HEAL_PLUS_DISPEL -> true;
            default -> false;
        };
    }

    private boolean isPartyBuff() {
        if (lt == null || rb == null) {
            return false;
        }
        if ((sourceid >= 1211003 && sourceid <= 1211008) || sourceid == Paladin.SWORD_HOLY_CHARGE || sourceid == Paladin.BW_HOLY_CHARGE || sourceid == DawnWarrior.SOUL_CHARGE) {// wk charges have lt and rb set but are neither player nor monster buffs
            return false;
        }
        return true;
    }

    private boolean isHeal() {
        return sourceid == Cleric.HEAL || sourceid == SuperGM.HEAL_PLUS_DISPEL;
    }

    private boolean isResurrection() {
        return sourceid == Bishop.RESURRECTION || sourceid == GM.RESURRECTION || sourceid == SuperGM.RESURRECTION;
    }

    private boolean isTimeLeap() {
        return sourceid == Buccaneer.TIME_LEAP;
    }

    public boolean isDragonBlood() {
        return skill && sourceid == DragonKnight.DRAGON_BLOOD;
    }

    public boolean isBerserk() {
        return skill && sourceid == DarkKnight.BERSERK;
    }

    public boolean isRecovery() {
        return sourceid == Beginner.RECOVERY || sourceid == Noblesse.RECOVERY || sourceid == Legend.RECOVERY || sourceid == Evan.RECOVERY;
    }

    public boolean isMapChair() {
        return sourceid == Beginner.MAP_CHAIR || sourceid == Noblesse.MAP_CHAIR || sourceid == Legend.MAP_CHAIR;
    }

    public static boolean isMapChair(int sourceid) {
        return sourceid == Beginner.MAP_CHAIR || sourceid == Noblesse.MAP_CHAIR || sourceid == Legend.MAP_CHAIR;
    }

    public boolean isDojoBuff() {
        return sourceid >= 2022359 && sourceid <= 2022421;
    }

    public static boolean isDojoBuff(int sourceid) {
        return sourceid >= 2022359 && sourceid <= 2022421;
    }

    public static boolean isHpMpRecovery(int sourceid) {
        return sourceid == 2022198 || sourceid == 2022337;
    }

    public static boolean isPyramidBuff(int sourceid) {
        return sourceid >= 2022585 && sourceid <= 2022617;
    }

    public static boolean isRateCoupon(int sourceid) {
        int itemType = sourceid / 1000;
        return itemType == 5211 || itemType == 5360;
    }

    public static boolean isExpIncrease(int sourceid) {
        return sourceid >= 2022450 && sourceid <= 2022452;
    }
    
    public static boolean isAriantShield(int sourceid) {
        return sourceid == 2022269;
    }
    
    public static boolean isMonsterCard(int sourceid) {
        int itemType = sourceid / 10000;
        return itemType == 238;
    }

    private boolean isDs() {
        return skill && (sourceid == Rogue.DARK_SIGHT || sourceid == NightWalker.DARK_SIGHT);
    }

    private boolean isWw() {
        return skill && (sourceid == WindArcher.WIND_WALK);
    }

    private boolean isCombo() {
        return skill && (sourceid == Crusader.COMBO || sourceid == DawnWarrior.COMBO);
    }

    private boolean isEnrage() {
        return skill && sourceid == Hero.ENRAGE;
    }

    public boolean isBeholder() {
        return skill && sourceid == DarkKnight.BEHOLDER;
    }

    private boolean isShadowPartner() {
        return skill && (sourceid == Hermit.SHADOW_PARTNER || sourceid == NightWalker.SHADOW_PARTNER);
    }

    private boolean isChakra() {
        return skill && sourceid == ChiefBandit.CHAKRA;
    }

    private boolean isCouponBuff() {
        return isRateCoupon(sourceid);
    }
    
    private boolean isAriantShield() {
        int itemid = sourceid;
        return isAriantShield(itemid);
    }

    public boolean isMonsterRiding() {
        return skill && (sourceid % 10000000 == 1004 || sourceid == Corsair.BATTLE_SHIP || sourceid == Beginner.SPACESHIP || sourceid == Noblesse.SPACESHIP
                || sourceid == Beginner.YETI_MOUNT1 || sourceid == Beginner.YETI_MOUNT2 || sourceid == Beginner.WITCH_BROOMSTICK || sourceid == Beginner.BALROG_MOUNT
                || sourceid == Noblesse.YETI_MOUNT1 || sourceid == Noblesse.YETI_MOUNT2 || sourceid == Noblesse.WITCH_BROOMSTICK || sourceid == Noblesse.BALROG_MOUNT
                || sourceid == Legend.YETI_MOUNT1 || sourceid == Legend.YETI_MOUNT2 || sourceid == Legend.WITCH_BROOMSTICK || sourceid == Legend.BALROG_MOUNT);
    }

    public boolean isMagicDoor() {
        return skill && sourceid == Priest.MYSTIC_DOOR;
    }

    public boolean isPoison() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == FPWizard.POISON_BREATH || sourceid == FPMage.ELEMENT_COMPOSITION || sourceid == NightWalker.POISON_BOMB || sourceid == BlazeWizard.FLAME_GEAR);
    }

    public boolean isMorph() {
        return morphId > 0;
    }

    public boolean isMorphWithoutAttack() {
        return morphId > 0 && morphId < 100; // Every morph item I have found has been under 100, pirate skill transforms start at 1000.
    }

    private boolean isMist() {
        return skill && (sourceid == FPMage.POISON_MIST || sourceid == Shadower.SMOKE_SCREEN || sourceid == BlazeWizard.FLAME_GEAR || sourceid == NightWalker.POISON_BOMB || sourceid == Evan.RECOVERY_AURA);
    }

    private boolean isSoulArrow() {
        return skill && (sourceid == Hunter.SOUL_ARROW || sourceid == Crossbowman.SOUL_ARROW || sourceid == WindArcher.SOUL_ARROW);
    }

    private boolean isShadowClaw() {
        return skill && sourceid == NightLord.SHADOW_STARS;
    }

    private boolean isCrash() {
        return skill && (sourceid == DragonKnight.POWER_CRASH || sourceid == Crusader.ARMOR_CRASH || sourceid == WhiteKnight.MAGIC_CRASH);
    }

    private boolean isDispel() {
        return skill && (sourceid == Priest.DISPEL || sourceid == SuperGM.HEAL_PLUS_DISPEL);
    }

    private boolean isCureAllAbnormalStatus() {
        if (skill) {
            return isHerosWill(sourceid);
        } else if (sourceid == 2022544) {
            return true;
        }

        return false;
    }

    public static boolean isHerosWill(int skillid) {
        return switch (skillid) {
            case Hero.HEROS_WILL, Paladin.HEROS_WILL, DarkKnight.HEROS_WILL,
                    FPArchMage.HEROS_WILL, ILArchMage.HEROS_WILL, Bishop.HEROS_WILL,
                    Bowmaster.HEROS_WILL, Marksman.HEROS_WILL, NightLord.HEROS_WILL,
                    Shadower.HEROS_WILL, Buccaneer.PIRATES_RAGE, Aran.HEROS_WILL -> true;
            default -> false;
        };
    }

    private boolean isDash() {
        return skill && (sourceid == Pirate.DASH || sourceid == ThunderBreaker.DASH || sourceid == Beginner.SPACE_DASH || sourceid == Noblesse.SPACE_DASH);
    }

    private boolean isSkillMorph() {
        return skill && (sourceid == Buccaneer.SUPER_TRANSFORMATION || sourceid == Marauder.TRANSFORMATION || sourceid == WindArcher.EAGLE_EYE || sourceid == ThunderBreaker.TRANSFORMATION);
    }

    private boolean isInfusion() {
        return skill && (sourceid == Buccaneer.SPEED_INFUSION || sourceid == Corsair.SPEED_INFUSION || sourceid == ThunderBreaker.SPEED_INFUSION);
    }

    private boolean isCygnusFA() {
        return skill && (sourceid == DawnWarrior.FINAL_ATTACK || sourceid == WindArcher.FINAL_ATTACK);
    }

    private boolean isHyperBody() {
        return skill && (sourceid == Spearman.HYPER_BODY || sourceid == GM.HYPER_BODY || sourceid == SuperGM.HYPER_BODY);
    }

    private boolean isComboReset() {
        return sourceid == Aran.COMBO_BARRIER || sourceid == Aran.COMBO_DRAIN;
    }

    private int getFatigue() {
        return fatigue;
    }

    private int getMorph() {
        return morphId;
    }

    private int getMorph(MapleCharacter chr) {
        if (morphId == 1000 || morphId == 1001 || morphId == 1003) { // morph skill
            return chr.getGender() == 0 ? morphId : morphId + 100;
        } 
        return morphId;
    }

    public boolean canDispel() {
        return poison || seal || darkness || weakness || curse;
    }

    private SummonMovementType getSummonMovementType() {
        if (!skill) {
            return null;
        }
        return switch (sourceid) {
            case Ranger.PUPPET, Sniper.PUPPET,
                    WindArcher.PUPPET, Outlaw.OCTOPUS,
                    Corsair.WRATH_OF_THE_OCTOPI -> SummonMovementType.STATIONARY;
            case Ranger.SILVER_HAWK, Sniper.GOLDEN_EAGLE,
                    Priest.SUMMON_DRAGON, Marksman.FROST_PREY,
                    Bowmaster.PHOENIX, Outlaw.GAVIOTA -> SummonMovementType.CIRCLE_FOLLOW;
            case DarkKnight.BEHOLDER, FPArchMage.ELQUINES,
                    ILArchMage.IFRIT, Bishop.BAHAMUT,
                    DawnWarrior.SOUL, BlazeWizard.FLAME,
                    BlazeWizard.IFRIT, WindArcher.STORM,
                    NightWalker.DARKNESS, ThunderBreaker.LIGHTNING -> SummonMovementType.FOLLOW;
            default -> null;
        };
    }

    public boolean hasNoIcon() {
        return (sourceid == 3111002 || sourceid == 3211002 || + // puppet, puppet
                sourceid == 3211005 || + // golden eagle
                sourceid == 2121005 || sourceid == 2221005 || + // elquines, ifrit
                sourceid == 2321003 || sourceid == 3121006 || + // bahamut, phoenix
                sourceid == 3221005 || sourceid == 3111005 || + // frostprey, silver hawk
                sourceid == 2311006 || sourceid == 5220002 || + // summon dragon, wrath of the octopi
                sourceid == 5211001 || sourceid == 5211002); // octopus, gaviota
    }

    public boolean isSkill() {
        return skill;
    }

    public int getSourceId() {
        return sourceid;
    }

    public int getBuffSourceId() {
        return skill ? sourceid : -sourceid;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }

    public boolean isDisease() {
        return disease;
    }

    private static class CancelEffectAction implements Runnable {

        private MapleStatEffect effect;
        private WeakReference<MapleCharacter> target;
        private long startTime;

        public CancelEffectAction(MapleCharacter target, MapleStatEffect effect, long startTime) {
            this.effect = effect;
            this.target = new WeakReference<>(target);
            this.startTime = startTime;
        }

        @Override
        public void run() {
            MapleCharacter realTarget = target.get();
            if (realTarget != null) {
                realTarget.cancelEffect(effect, false, startTime);
            }
        }
    }

    public short getHp() {
        return hp;
    }

    public short getMp() {
        return mp;
    }

    public double getHpRate() {
        return hpR;
    }

    public double getMpRate() {
        return mpR;
    }

    public byte getHpR() {
        return mhpR;
    }

    public byte getMpR() {
        return mmpR;
    }

    public short getHpRRate() {
        return mhpRRate;
    }

    public short getMpRRate() {
        return mmpRRate;
    }

    public short getHpCon() {
        return hpCon;
    }

    public short getMpCon() {
        return mpCon;
    }

    public short getMatk() {
        return matk;
    }

    public short getWatk() {
        return watk;
    }

    public int getDuration() {
        return duration;
    }

    public List<Pair<MapleBuffStat, BuffValueHolder>> getStatups() {
        return statups;
    }

    public boolean sameSource(MapleStatEffect effect) {
        return this.sourceid == effect.sourceid && this.skill == effect.skill;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDamage() {
        return damage;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public int getMobCount() {
        return mobCount;
    }

    public int getFixDamage() {
        return fixdamage;
    }

    public byte getBulletCount() {
        return bulletCount;
    }

    public byte getBulletConsume() {
        return bulletConsume;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }

    public int getMoneyCon() {
        return moneyCon;
    }

    public int getCooldown() {
        return cooldown;
    }

    public Map<MonsterStatus, Integer> getMonsterStati() {
        return monsterStatus;
    }

    public boolean isMaxHpIncrease() {
        return !skill && ItemConstants.isIncMaxHpItem(sourceid);
    }

    public boolean isMaxMpIncrease() {
        return !skill && ItemConstants.isIncMaxMpItem(sourceid);
    }
}
