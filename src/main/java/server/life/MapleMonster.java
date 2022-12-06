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

package server.life;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import client.MapleJob;
import enums.FieldEffectType;
import network.packet.CField;
import network.packet.MobPool;
import network.packet.NpcPool;
import server.maps.MapleMapObject;
import server.skills.MobSkill;
import server.skills.PlayerSkill;
import client.listeners.DamageEvent;
import client.listeners.DamageListener;
import client.listeners.MobKilledEvent;
import client.listeners.MobKilledListener;
import client.status.MonsterStatus;
import client.status.MonsterStatusEffect;
import constants.GameConstants;
import constants.ServerConstants;
import constants.skills.Crusader;
import constants.skills.DragonKnight;
import constants.skills.Hermit;
import constants.skills.NightLord;
import constants.skills.NightWalker;
import constants.skills.Shadower;
import constants.skills.SuperGM;
import constants.skills.WhiteKnight;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import net.server.audit.locks.MonitoredReentrantLock;
import net.server.channel.Channel;
import net.server.world.MapleParty;
import net.server.world.MaplePartyCharacter;
import scripting.event.EventInstanceManager;
import server.TimerManager;
import server.achievements.WorldTour;
import server.life.MapleLifeFactory.BanishInfo;
import server.maps.MapleMap;
import server.maps.MapleMapObjectType;
import server.maps.NostalgicMap;
import server.skills.SkillFactory;
import tools.*;
import net.server.audit.LockCollector;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;

public class MapleMonster extends AbstractLoadedMapleLife {

    private ChangeableStats ostats = null;  //unused, v83 WZs offers no support for changeable stats.
    private MapleMonsterStats stats;
    private AtomicLong maxHpPlusHeal = new AtomicLong(1);
    private int hp, mp;
    private WeakReference<MapleCharacter> controller = new WeakReference<>(null);
    private boolean controllerHasAggro, controllerKnowsAboutAggro;
    //private Collection<MonsterListener> listeners = new LinkedList<>();
    private Collection<AttackerEntry> attackers = new LinkedList<>();
    private MapleCharacter highestDamageChar;
    private EnumMap<MonsterStatus, MonsterStatusEffect> stati = new EnumMap<>(MonsterStatus.class);
    private ArrayList<MonsterStatus> alreadyBuffed = new ArrayList<>();
    private MapleMap map;
    private int VenomMultiplier = 0;
    private boolean fake = false;
    private boolean dropsDisabled = false;
    private List<Pair<Integer, Integer>> usedSkills = new ArrayList<>();
    private Map<Pair<Integer, Integer>, Integer> skillsUsed = new HashMap<>();
    private Set<Integer> usedAttacks = new HashSet<>();
    private Set<Integer> calledMobOids = null;
    private int calledMobCount = 0;
    private WeakReference<MapleMonster> callerMob = new WeakReference<>(null);
    private List<Integer> stolenItems = new ArrayList<>();
    private int team;
    private int parentMob = 0;
    private int parentMobOid = 0;
    private int spawnEffect = 0;
    private final HashMap<Integer, AtomicLong> takenDamage = new HashMap<>();
    private ScheduledFuture<?> monsterItemDrop = null;
    private Runnable removeAfterAction = null;
    // Listeners
    private Set<MobKilledListener> mobDeadListeners = new HashSet<>();
    private Set<DamageListener> damage_listeners = new HashSet<>();

    public ReentrantLock monsterLock = new ReentrantLock();
    private MonitoredReentrantLock externalLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_EXT);
    private MonitoredReentrantLock statiLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_STATI);
    private MonitoredReentrantLock animationLock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.MOB_ANI);

    public MapleMonster(int id, MapleMonsterStats stats) {
        super(id);
        initWithStats(stats);
    }

    public MapleMonster(MapleMonster monster) {
        super(monster);
        initWithStats(monster.stats);
    }

    public void lockMonster() {
        externalLock.lock();
    }

    public void unlockMonster() {
        externalLock.unlock();
    }

    private void initWithStats(MapleMonsterStats baseStats) {
        setStance(5);
        this.stats = baseStats.copy();
        hp = stats.getHp();
        mp = stats.getMp();

        maxHpPlusHeal.set(hp);
    }

    public void setSpawnEffect(int effect) {
        spawnEffect = effect;
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public void disableDrops() {
        this.dropsDisabled = true;
    }

    public void enableDrops() {
        this.dropsDisabled = false;
    }

    public boolean dropsDisabled() {
        return dropsDisabled;
    }

    public void setMap(MapleMap map) {
        this.map = map;
    }

    public int getParentMobOid() {
        return parentMobOid;
    }

    public void setParentMobOid(int parentMobId) {
        this.parentMobOid = parentMobId;
    }

    public int countAvailableMobSummons(int limit, int skillLimit) {    // limit prop for summons has another conotation, found thanks to MedicOP
        Set<Integer> calledOids = this.calledMobOids;
        if (calledOids != null) {
            limit -= calledOids.size();
        }

        return Math.min(limit, skillLimit - this.calledMobCount);
    }

    public void addSummonedMob(MapleMonster mob) {
        Set<Integer> calledOids = this.calledMobOids;
        if (calledOids == null) {
            calledOids = Collections.synchronizedSet(new HashSet<Integer>());
            this.calledMobOids = calledOids;
        }

        calledOids.add(mob.getObjectId());
        mob.setSummonerMob(this);
        this.calledMobCount += 1;
    }

    private void removeSummonedMob(int mobOid) {
        Set<Integer> calledOids = this.calledMobOids;
        if (calledOids != null) {
            calledOids.remove(mobOid);
        }
    }

    private void setSummonerMob(MapleMonster mob) {
        this.callerMob = new WeakReference<>(mob);
    }

    private void dispatchClearSummons() {
        MapleMonster caller = this.callerMob.get();
        if (caller != null) {
            caller.removeSummonedMob(this.getObjectId());
        }

        this.calledMobOids = null;
    }

    public void pushRemoveAfterAction(Runnable run) {
        this.removeAfterAction = run;
    }

    public Runnable popRemoveAfterAction() {
        Runnable r = this.removeAfterAction;
        this.removeAfterAction = null;

        return r;
    }

    public int getHp() {
        return hp;
    }

    public synchronized void addHp(int hp) {
        if (hp <= 0) {
            return;
        }
        hp += hp;
    }

    public synchronized void setStartingHp(int hp) {
        stats.setHp(hp);    // refactored mob stats after non-static HP pool suggestion thanks to twigs
        hp += hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return stats.getHp();
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        if (mp < 0) {
            mp = 0;
        }
        this.mp = mp;
    }

    public int getMaxMp() {
        return stats.getMp();
    }

    public int getExp() {
        return stats.getExp();
    }

    public int getLevel() {
        return stats.getLevel();
    }

    public int getCP() {
        return stats.getCP();
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getVenomMulti() {
        return this.VenomMultiplier;
    }

    public void setVenomMulti(int multiplier) {
        this.VenomMultiplier = multiplier;
    }

    public MapleMonsterStats getStats() {
        return stats;
    }

    public boolean isBoss() {
        return stats.isBoss();
    }

    public int getAnimationTime(String name) {
        return stats.getAnimationTime(name);
    }

    private List<Integer> getRevives() {
        return stats.getRevives();
    }

    private byte getTagColor() {
        return stats.getTagColor();
    }

    private byte getTagBgColor() {
        return stats.getTagBgColor();
    }

    private boolean applyAnimationIfRoaming(int attackPos, MobSkill skill) {   // roam: not casting attack or skill animations
        if (!animationLock.tryLock()) {
            return false;
        }

        try {
            long animationTime;

            if (skill == null) {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobAttackAnimationTime(this.getId(), attackPos);
            } else {
                animationTime = MapleMonsterInformationProvider.getInstance().getMobSkillAnimationTime(skill);
            }

            if (animationTime > 0) {
                return map.getChannelServer().registerMobOnAnimationEffect(map.getId(), this.hashCode(), animationTime);
            } else {
                return true;
            }
        } finally {
            animationLock.unlock();
        }
    }

    public synchronized void disposeMapObject() {     // mob is no longer associated with the map it was in
        hp = -1;
    }

    /**
     * Applies damage and returns true if the monster died
     *
     * @param from             Player who did the damage
     * @param damage           Amount of damage
     * @param updateAttackTime
     * @return True if monster's hp is <= 0, otherwise false
     */
    public synchronized boolean damage(MapleCharacter from, int damage, boolean updateAttackTime) {
        if (!isAlive()) {
            return false;
        }
        AttackerEntry attacker;
        updateDamageListeners(from, damage);
        if (from.getParty() != null) {
            attacker = new PartyAttackerEntry(
                    from.getParty().getId(), from.getClient().getChannelServer());
        } else {
            attacker = new SingleAttackerEntry(from, from.getClient().getChannelServer());
        }
        boolean replaced = false;
        for (AttackerEntry aentry : attackers) {
            if (aentry.equals(attacker)) {
                attacker = aentry;
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            attackers.add(attacker);
        }
        int rDamage = Math.max(0, Math.min(damage, this.hp));
        attacker.addDamage(from, rDamage, updateAttackTime);
        this.hp -= rDamage;
        int remhppercentage = (int) Math.ceil((this.hp * 100.0) / getMaxHp());
        if (remhppercentage < 1) {
            remhppercentage = 1;
        }
        //if (isBoss()) {
        //    MonsterScriptManager.getInstance().onDamage(from.getClient(), getId(), rDamage);
        //}
        long okTime = System.currentTimeMillis() - 4000;
        if (hasBossHPBar() && !GameConstants.isDojo(from.getMapId())) {
            from.getMap().broadcastMessage(makeBossHPBarPacket(), getPosition());
        }
        if (!isBoss() || isBoss() && GameConstants.isDojo(from.getMapId())) {
            for (AttackerEntry mattacker : attackers) {
                for (AttackingMapleCharacter cattacker : mattacker.getAttackers()) {
                    if (cattacker.getAttacker().getMap() == from.getMap()) {
                        if (cattacker.getLastAttackTime() >= okTime) {
                            cattacker.getAttacker().getClient().announce(
                                    MobPool.Packet.onHpIndicator(getObjectId(), remhppercentage));
                        }
                    }
                }
            }
        }
        return hp <= 0;
    }

    public void heal(int hp, int mp) {
        int mp2Heal = getMp() + mp;
        int maxMp = getMaxMp();
        if (mp2Heal >= maxMp) {
            mp2Heal = maxMp;
        }
        setMp(mp2Heal);

        if (hp > 0) {
            getMap().broadcastMessage(MobPool.Packet.healMonster(getMonster(), hp));
        }
    }

    public boolean isAttackedBy(MapleCharacter chr) {
        return takenDamage.containsKey(chr.getId());
    }

    public void giveExpToCharacter(MapleCharacter attacker, int exp, boolean isKiller, int numExpSharers) {
        final int partyModifier = 0;
        //numExpSharers > 1 ? (110 + (5 * (numExpSharers - 2))) : 0;

        int partyExp = 0;

        if (attacker.getHp() > 0) {
            int personalExp = exp;

            if (exp > 0) {
                if (partyModifier > 0) {
                    partyExp = (int) (personalExp
                            * ServerConstants.PARTY_EXPERIENCE_MOD
                            * partyModifier
                            / 1000f);
                }
                Integer holySymbol = attacker.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                boolean GMHolySymbol
                        = attacker.getBuffSource(MapleBuffStat.HOLY_SYMBOL)
                        == SuperGM.HOLY_SYMBOL;
                if (holySymbol != null) {
                    if (numExpSharers == 1 && !GMHolySymbol) {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 500.0);
                    } else {
                        personalExp *= 1.0 + (holySymbol.doubleValue() / 100.0);
                    }
                }
                if (stati.containsKey(MonsterStatus.SHOWDOWN)) {
                    personalExp *= (stati.get(MonsterStatus.SHOWDOWN).getStati()
                            .get(MonsterStatus.SHOWDOWN).doubleValue()
                            / 100.0 + 1.0);
                }
            }
            if (exp < 0) {//O.O ><
                personalExp = Integer.MAX_VALUE;
            }

            // Nerf leechers' EXP by 60%
            if (System.currentTimeMillis() - getMap().getAfkTime(attacker) > 60000) {
                personalExp *= .4;
            }

            attacker.gainExp(personalExp, partyExp, true, false, isKiller);
            attacker.updateQuestMobCount(getId());
            attacker.increaseEquipExp(personalExp); //better place
        }
    }

    public MapleCharacter killBy(MapleCharacter killer, int animation) {
        //distributeExperience(killer != null ? killer.getId() : 0);
        // Calculate EXP distribution
        double nostalgicBonus = NostalgicMap.getNostalgicRate(this.getId());
        long totalBaseExpL = (long) ((this.getExp() * nostalgicBonus) * killer.getClient().getPlayer().getExpRate());
        //long totalBaseExpL = (long) (this.getExp() * killer.getClient().getPlayer().getExpRate()); original
        int totalBaseExp = (int) (Math.min(Integer.MAX_VALUE, totalBaseExpL));
        AttackerEntry highest = null;
        int highdamage = 0;
        for (AttackerEntry attackEntry : attackers) {
            if (attackEntry.getDamage() > highdamage) {
                highest = attackEntry;
                highdamage = attackEntry.getDamage();
            }
        }
        for (AttackerEntry attackEntry : attackers) {
            attackEntry.killedMob(killer.getMap(),
                    (int) Math.ceil(totalBaseExp * ((double) attackEntry.getDamage() / getMaxHp())),
                    attackEntry == highest);
        }
        if (this.getController() != null) { // this can/should only happen when a hidden gm attacks the monster
            getController().getClient().announce(MaplePacketCreator.stopControllingMonster(this.getObjectId()));
            getController().stopControllingMonster(this);
        }
        if (hasBossHPBar()) {
            killer.getMap().broadcastMessage(makeBossHPBarPacket(), getPosition());
        }
        if (this.getRevives() != null) {
            final MapleMap reviveMap = this.getMap();
            final List<Integer> toSpawn = this.getRevives();
            // Dojo stuff that probably should be removed
            if (toSpawn.contains(9300216) && reviveMap.getId() > 925000000 && reviveMap.getId() < 926000000) {
                reviveMap.broadcastMessage(CField.Packet.onFieldEffect(FieldEffectType.Effect.getMode(), "dojang/end/clear"));
                reviveMap.broadcastMessage(CField.Packet.onFieldEffect(FieldEffectType.Sound.getMode(), "Dojang/clear"));
            }

            // Revives timed mobs and npcs (?) -> not sure why 9001108 is specifically targeted here
            Pair<Integer, String> timeMob = reviveMap.getTimeMob();
            if (timeMob != null) {
                if (toSpawn.contains(timeMob.getLeft())) {
                    reviveMap.broadcastMessage(MaplePacketCreator.serverNotice(6, timeMob.getRight()));
                }

                if (timeMob.getLeft() == 9300338 && (reviveMap.getId() >= 922240100 && reviveMap.getId() <= 922240119)) {
                    if (!reviveMap.containsNPC(9001108)) {
                        MapleNPC npc = MapleLifeFactory.getNPC(9001108);
                        npc.setPosition(new Point(172, 9));
                        npc.setCy(9);
                        npc.setRx0(172 + 50);
                        npc.setRx1(172 - 50);
                        npc.setFh(27);
                        reviveMap.addMapObject(npc);
                        reviveMap.broadcastMessage(NpcPool.Packet.onEnterField(npc));
                    } else {
                        reviveMap.toggleHiddenNPC(9001108);
                    }
                }
            }
            if (killer.getParty() != null) {
                for (MaplePartyCharacter mpc : killer.getParty().getMembers()) {
                    if (mpc.getMapId() == killer.getMapId() && mpc.getChannel() == killer.getClient().getChannel()) { // map and channel check
                        mpc.getPlayer().finishWorldTour(WorldTour.AchievementType.MONSTERKILL, getId()); // reward is hp
                    }
                }
            } else killer.finishWorldTour(WorldTour.AchievementType.MONSTERKILL, getId());

            // Spawns these on death
            for (Integer mid : toSpawn) {
                final MapleMonster newMob = MapleLifeFactory.getMonster(mid);
                newMob.setPosition(this.getPosition());
                newMob.setFh(this.getFh());
                //newMob.setEventInstance(eventInstance);
                newMob.setParentMob(getObjectId());
                if (this.dropsDisabled()) {
                    newMob.disableDrops();
                }

                //new_mob.setMobDeadListeners(mob.getMobDeadListeners());
                reviveMap.spawnRevives(newMob);
            }
        }
        // Update mob death listeners
        updateMobDeadListeners(killer);

        MapleCharacter ret = highestDamageChar;
        highestDamageChar = null; // may not keep hard references to chars outside of PlayerStorage or MapleMap
        killer.getMap().broadcastMessage(MaplePacketCreator.killMonster(getObjectId(), animation), getPosition());
        //if (isBoss()) {
        //    MonsterScriptManager.getInstance().onDeath(killer.getClient(), getId());
        //}
        return ret;
    }

    public void updateMobDeadListeners(MapleCharacter from) {
        // Update listeners
        MobKilledEvent event = new MobKilledEvent(this, this, from);
        for (MobKilledListener listener : this.mobDeadListeners)
            listener.update(event);

        if (null != null)
            for (MobKilledListener listener : from.getMobKilledListeners())
                listener.update(event);
    }

    public void updateDamageListeners(MapleCharacter from, int damage) {
        from.updateDamageListeners(new DamageEvent(this, this, from, damage));
        for (DamageListener listener : this.damage_listeners)
            listener.update(new DamageEvent(this, this, from, damage));
    }

    public void addMobDeadListener(MobKilledListener listener) {
        this.mobDeadListeners.add(listener);
    }

    public void addDamageListener(DamageListener listener) {
        damage_listeners.add(listener);
    }

    public Collection<DamageListener> getDamageListeners() {
        return Collections.unmodifiableCollection(damage_listeners);
    }

    public Collection<MobKilledListener> getMobDeadListeners() {
        return Collections.unmodifiableCollection(mobDeadListeners);
    }

    public void setMobDeadListeners(Set<MobKilledListener> listeners) {
        mobDeadListeners = listeners;
    }

    public int getHighestDamagerId() {
        int curId = 0;
        long curDmg = 0;

        for (Entry<Integer, AtomicLong> damage : takenDamage.entrySet()) {
            curId = damage.getValue().get() >= curDmg ? damage.getKey() : curId;
            curDmg = damage.getKey() == curId ? damage.getValue().get() : curDmg;
        }

        return curId;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public byte[] makeBossHPBarPacket() {
        //return MaplePacketCreator.showBossHP(getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
        return CField.Packet.onFieldEffect(FieldEffectType.BossHp.getMode(), getId(), getHp(), getMaxHp(), getTagColor(), getTagBgColor());
    }

    public boolean hasBossHPBar() {
        return isBoss() && getTagColor() > 0;
    }

    public void announceMonsterStatus(MapleClient client) {
        statiLock.lock();
        try {
            if (stati.size() > 0) {
                for (final MonsterStatusEffect mse : this.stati.values()) {
                    //client.announce(MaplePacketCreator.applyMonsterStatus(getObjectId(), mse, null));
                }
            }
        } finally {
            statiLock.unlock();
        }
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        int[] removeMob = {5090000, 5090001, 6090000, 6090001, 6090002, 6090003, 6090004, 7090000, 8090000}; // bot-catching mobs
        for (int i : removeMob) {
            if (getId() == i) {
                return;
            }
        }
        if (hp <= 0) { // mustn't monsterLock this function
            return;
        }
        if (fake) {
            client.announce(MaplePacketCreator.spawnFakeMonster(this, MobSpawnType.SUSPENDED.getType()));
        } else {
            client.announce(MaplePacketCreator.spawnMonster(this, MobSpawnType.NORMAL.getType()));
        }

        announceMonsterStatus(client);

        if (hasBossHPBar()) {
            client.announceBossHpBar(this, this.hashCode(), makeBossHPBarPacket());
        }
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(MaplePacketCreator.killMonster(getObjectId(), false));
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MONSTER;
    }

    public boolean isMobile() {
        return stats.isMobile();
    }

    public ElementalEffectiveness getElementalEffectiveness(Element e) {
        statiLock.lock();
        try {
            if (stati.get(MonsterStatus.DOOM) != null) {
                return ElementalEffectiveness.NORMAL; // like blue snails
            }
        } finally {
            statiLock.unlock();
        }

        return getMonsterEffectiveness(e);
    }

    private ElementalEffectiveness getMonsterEffectiveness(Element e) {
        return stats.getEffectiveness(e);
    }

    private MapleCharacter getActiveController() {
        MapleCharacter chr = getController();

        if (chr != null && chr.isLoggedinWorld() && chr.getMap() == this.getMap()) {
            return chr;
        } else {
            return null;
        }
    }

    private void broadcastMonsterStatusMessage(byte[] packet) {
        map.broadcastMessage(packet, getPosition());

        MapleCharacter chrController = getActiveController();
        if (chrController != null && !chrController.isMapObjectVisible(MapleMonster.this)) {
            chrController.announce(packet);
        }
    }
    
/*    private int broadcastStatusEffect(final MonsterStatusEffect status) {
        int animationTime = status.getSkill().getAnimationTime();
        byte[] packet = MaplePacketCreator.applyMonsterStatus(getObjectId(), status, null);
        broadcastMonsterStatusMessage(packet);
        
        return animationTime;
    }*/

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status,
                               boolean poison) {
        return applyStatus(from, status, poison, false);
    }

    public boolean applyStatus(MapleCharacter from, final MonsterStatusEffect status,
                               boolean poison, boolean venom) {
        switch (stats.getEffectiveness(status.getPlayerSkill().getElement())) {
            case IMMUNE:
            case STRONG:
            case NEUTRAL:
                return false;
            case NORMAL:
            case WEAK:
                break;
            default: {
                System.out.println("Unknown elemental effectiveness: "
                        + stats.getEffectiveness(status.getPlayerSkill().getElement()));
                return false;
            }
        }

        if (poison && getHp() <= 1) {
            return false;
        }

        final Map<MonsterStatus, Integer> statis = status.getStati();
        if (stats.isBoss()) {
            if (!(statis.containsKey(MonsterStatus.SPEED)
                    && statis.containsKey(MonsterStatus.NINJA_AMBUSH)
                    && statis.containsKey(MonsterStatus.WATK))) {
                return false;
            }
        }

        for (MonsterStatus stat : statis.keySet()) {
            final MonsterStatusEffect oldEffect = stati.get(stat);
            if (oldEffect != null) {
                oldEffect.removeActiveStatus(stat);
                if (oldEffect.getStati().isEmpty()) {
                    oldEffect.cancelTask();
                    oldEffect.cancelDamageSchedule();
                }
            }
        }

        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = () -> {
            if (isAlive()) {
                byte[] packet = MobPool.Packet.onStatReset(getObjectId(), status.getStati());
                map.broadcastMessage(packet, getPosition());
                if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                    getController().getClient().announce(packet);
                }
            }
            for (MonsterStatus stat : status.getStati().keySet()) {
                stati.remove(stat);
            }
            setVenomMulti(0);
            status.cancelDamageSchedule();
        };
        if (poison) {
            int poisonLevel = from.getSkillLevel(status.getPlayerSkill().getId());
            int poisonDamage = Math.min(Short.MAX_VALUE, (int) (getMaxHp() / (70.0 - poisonLevel) + 0.999));
            status.setValue(MonsterStatus.POISON, poisonDamage);
            status.setDamageSchedule(timerManager.register(
                    new DamageTask(poisonDamage, from, status, cancelTask, 0),
                    1000, 1000));
        } else if (venom) {
            if (from.getJob() == MapleJob.NIGHTLORD
                    || from.getJob() == MapleJob.SHADOWER
                    || from.getJob().isA(MapleJob.NIGHTWALKER3)) {
                int poisonLevel, matk;
                int venomskill = (from.getJob() == MapleJob.NIGHTLORD ? NightLord.VENOMOUS_STAR
                        : (from.getJob() == MapleJob.SHADOWER ? Shadower.VENOMOUS_STAB : NightWalker.VENOM));
                poisonLevel = from.getSkillLevel(SkillFactory.getSkill(venomskill));
                if (poisonLevel <= 0) {
                    return false;
                }
                matk = SkillFactory.getSkill(venomskill).getEffect(poisonLevel).getMatk();
                int luk = from.getLuk();
                int str = from.getStr();
                int dex = from.getDex();
                //int maxDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.2 * luk * matk));
                //int minDmg = (int) Math.ceil(Math.min(Short.MAX_VALUE, 0.1 * luk * matk));
                int maxDmg = (int) Math.ceil((18.5 * (str + luk) + dex * 2) / 100 * matk);
                int minDmg = (int) Math.ceil((8.0 * (str + luk) + dex * 2) / 100 * matk);

                int gap = maxDmg - minDmg;
                if (gap == 0) {
                    gap = 1;
                }
                int poisonDamage = 0;
                for (int i = 0; i < getVenomMulti(); i++) {
                    poisonDamage += (Randomizer.nextInt(gap) + minDmg);
                }
                status.setValue(MonsterStatus.VENOMOUS_WEAPON, poisonDamage);
                status.setDamageSchedule(timerManager.register(
                        new DamageTask(poisonDamage, from, status, cancelTask, 0),
                        1000, 1000));
            } else {
                return false;
            }
        } else if (status.getPlayerSkill().getId() == Hermit.SHADOW_WEB
                || status.getPlayerSkill().getId() == NightWalker.SHADOW_WEB) { //Shadow Web
            status.setDamageSchedule(
                    timerManager.schedule(new DamageTask((int) (getMaxHp() / 50.0 + 0.999),
                            from, status, cancelTask, 1), 3500));
        } else if (status.getPlayerSkill().getId() == NightLord.NINJA_AMBUSH || status.getPlayerSkill().getId() == Shadower.NINJA_AMBUSH) { // Ninja Ambush
            final PlayerSkill skill = SkillFactory.getSkill(status.getPlayerSkill().getId());
            final int level = from.getSkillLevel(skill);
            final int damage = (int) (level + 30) * skill.getEffect(level).getDamage() * (from.getStr() + from.getLuk()) / 2000;
            /*if (getHp() - damage <= 1)  { make hp 1 betch
             damage = getHp() - (getHp() - 1);
             }*/

            status.setValue(MonsterStatus.NINJA_AMBUSH, damage);
            status.setDamageSchedule(timerManager.register(
                    new DamageTask(damage, from, status, cancelTask, 2), 1000, 1000));
        }
        for (MonsterStatus stat : status.getStati().keySet()) {
            stati.put(stat, status);
            alreadyBuffed.add(stat);
        }
        int animationTime = status.getPlayerSkill().getAnimationTime();
        byte[] packet = MobPool.Packet.onStatSet(getObjectId(), status);
        map.broadcastMessage(packet, getPosition());
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().announce(packet);
        }
        status.setCancelTask(timerManager.scheduleAtTimestamp(cancelTask, status.getDuration())); // + animationTime
        return true;
    }

    public final void dispelSkill(final MobSkill skillId) {
        List<MonsterStatus> toCancel = new ArrayList<>();
        for (Entry<MonsterStatus, MonsterStatusEffect> effects : stati.entrySet()) {
            MonsterStatusEffect mse = effects.getValue();
            if (mse.getMobSkill() != null && mse.getMobSkill().getSkillId() == skillId.getSkillId()) { //not checking for level.
                toCancel.add(effects.getKey());
            }
        }
        for (MonsterStatus stat : toCancel) {
            debuffMobStat(stat);
        }
    }

    public void applyMonsterBuff(final Map<MonsterStatus, Integer> stats, final int x,
                                 int skillId, long duration, MobSkill skill) {
        TimerManager timerManager = TimerManager.getInstance();
        final Runnable cancelTask = () -> {
            if (isAlive()) {
                byte[] packet = MobPool.Packet.onStatReset(getObjectId(), stats);
                map.broadcastMessage(packet, getPosition());
                if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                    getController().getClient().announce(packet);
                }
                for (final MonsterStatus stat : stats.keySet()) {
                    stati.remove(stat);
                }
            }
        };
        final MonsterStatusEffect effect = new MonsterStatusEffect(stats, null, skill, true, duration);
        byte[] packet = MobPool.Packet.onStatSet(getObjectId(), effect);
        map.broadcastMessage(packet, getPosition());
        for (MonsterStatus stat : stats.keySet()) {
            stati.put(stat, effect);
            alreadyBuffed.add(stat);
        }
        if (getController() != null && !getController().isMapObjectVisible(this)) {
            getController().getClient().announce(packet);
        }
        effect.setCancelTask(timerManager.schedule(cancelTask, duration));
    }

    public void refreshMobPosition() {
        resetMobPosition(getPosition());
    }

    public void resetMobPosition(Point newPoint) {
        //aggroRemoveController();

        setPosition(newPoint);
        //map.broadcastMessage(MaplePacketCreator.moveMonster(
        //        this.getObjectId(), false, -1, 0, 0, 0, this.getPosition(), this.getIdleMovement(), getIdleMovementDataLength()));
        map.moveMonster(this, this.getPosition());

        //aggroUpdateController();
    }

    private void debuffMobStat(MonsterStatus stat) {
        MonsterStatusEffect oldEffect;
        statiLock.lock();
        try {
            oldEffect = stati.remove(stat);
        } finally {
            statiLock.unlock();
        }

        if (oldEffect != null) {
            byte[] packet = MobPool.Packet.onStatReset(getObjectId(), oldEffect.getStati());
            broadcastMonsterStatusMessage(packet);
        }
    }

    public void debuffMob(int skillId) {
        List<MonsterStatus> stats = new ArrayList<>();
        if (skillId == DragonKnight.POWER_CRASH || skillId == WhiteKnight.MAGIC_CRASH || Crusader.ARMOR_CRASH == skillId) {
            stats.add(MonsterStatus.MAGIC_IMMUNITY);
            stats.add(MonsterStatus.WEAPON_IMMUNITY);
        } else {
            stats.add(MonsterStatus.WEAPON_ATTACK_UP);
            stats.add(MonsterStatus.WEAPON_DEFENSE_UP);
            stats.add(MonsterStatus.MAGIC_ATTACK_UP);
            stats.add(MonsterStatus.MAGIC_DEFENSE_UP);
            stats.add(MonsterStatus.SHOWDOWN);
            stats.add(MonsterStatus.HARD_SKIN);
        }
        for (MonsterStatus stat : stats) {
            if (isBuffed(stat)) {
                final MonsterStatusEffect oldEffect = stati.get(stat);
                byte[] packet = MobPool.Packet.onStatReset(getObjectId(), oldEffect.getStati());
                map.broadcastMessage(packet, getPosition());
                if (getController() != null && !getController().isMapObjectVisible(MapleMonster.this)) {
                    getController().getClient().announce(packet);
                }
                stati.remove(stat);
            }
        }
    }

    public boolean isBuffed(MonsterStatus status) {
        statiLock.lock();
        try {
            return stati.containsKey(status);
        } finally {
            statiLock.unlock();
        }
    }

    public void setFake(boolean fake) {
        this.fake = fake;
    }

    public boolean isFake() {
        return fake;
    }

    public MapleMap getMap() {
        return map;
    }

    public MapleMonster getMonster() {
        return this;
    }

    public List<Pair<Integer, Integer>> getSkills() {
        return stats.getSkills();
    }

    public boolean hasSkill(int skillId, int level) {
        return stats.hasSkill(skillId, level);
    }

    public int getSkillPos(int skillId, int level) {
        int pos = 0;
        for (Pair<Integer, Integer> ms : this.getSkills()) {
            if (ms.getLeft() == skillId && ms.getRight() == level) {
                return pos;
            }

            pos++;
        }

        return -1;
    }

    public boolean canUseSkill(MobSkill toUse, boolean apply) {
        if (toUse == null) {
            return false;
        }
        if (isBuffed(MonsterStatus.SEAL_SKILL)) {
            return false;
        }
        if (toUse.getHP() < 100) {
            float percentHp = hp * 100f / getMaxHp();
            float skillHp = toUse.getHP() * 1.0f;
            if (percentHp > skillHp) {
                return false;
            }
        }
        Pair<Integer, Integer> goingToUse = new Pair<>(toUse.getSkillId(), toUse.getSkillLevel());
        if (toUse.getLimit() > 0) {
            if (this.skillsUsed.containsKey(goingToUse)) {
                int times = this.skillsUsed.get(goingToUse);
                if (times >= toUse.getLimit()) {
                    return false;
                }
            }
        }
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == toUse.getSkillId()
                    && skill.getRight() == toUse.getSkillLevel()) {
                return false;
            }
        }
        if (toUse.getSkillId() == 200) {
            Collection<MapleMapObject> mmo = getMap().getMapObjects();
            int i = 0;
            for (MapleMapObject mo : mmo) {
                if (mo.getType() == MapleMapObjectType.MONSTER) {
                    i++;
                }
            }
            if (i > 100) {
                return false;
            }
        }
        return true;
    }

    public void usedSkill(final int skillId, final int level, long cooltime, long duration) {
        this.usedSkills.add(new Pair<>(skillId, level));
        if (this.skillsUsed.containsKey(new Pair<>(skillId, level))) {
            int times = this.skillsUsed.get(new Pair<>(skillId, level)) + 1;
            this.skillsUsed.remove(new Pair<>(skillId, level));
            this.skillsUsed.put(new Pair<>(skillId, level), times);
        } else {
            this.skillsUsed.put(new Pair<>(skillId, level), 1);
        }
        final MapleMonster mons = this;
        TimerManager tMan = TimerManager.getInstance();
        tMan.schedule(() -> {
            mons.clearSkill(skillId, level);
        }, cooltime + duration);
    }

    public void clearSkill(int skillId, int level) {
        int index = -1;
        for (Pair<Integer, Integer> skill : usedSkills) {
            if (skill.getLeft() == skillId && skill.getRight() == level) {
                index = usedSkills.indexOf(skill);
                break;
            }
        }
        if (index != -1) {
            usedSkills.remove(index);
        }
    }

    public int canUseAttack(int attackPos, boolean isSkill) {
        Pair<Integer, Integer> attackInfo = MapleMonsterInformationProvider.getInstance().getMobAttackInfo(this.getId(), attackPos);
        if (attackInfo == null) return -1;
        int mpCon = attackInfo.getLeft();
        if (mp < mpCon) return -1;

        usedAttack(attackPos, mpCon, attackInfo.getRight());
        return 1;
    }

    private void usedAttack(final int attackPos, int mpCon, int cooltime) {
        mp -= mpCon;
        usedAttacks.add(attackPos);

        final MapleMonster mons = this;
        MapleMap mmap = mons.getMap();
        Runnable r = () -> mons.clearAttack(attackPos);

        mmap.getChannelServer().registerMobClearSkillAction(mmap.getId(), r, cooltime);
    }

    private void clearAttack(int attackPos) {
        usedAttacks.remove(attackPos);
    }

    public int getNoSkills() {
        return this.stats.getNoSkills();
    }

    public boolean isFirstAttack() {
        return this.stats.isFirstAttack();
    }

    public int getBuffToGive() {
        return this.stats.getBuffToGive();
    }

    private final class DamageTask implements Runnable {

        private final int dealDamage;
        private final MapleCharacter chr;
        private final MonsterStatusEffect status;
        private final Runnable cancelTask;
        private final int type;
        private final MapleMap map;

        private DamageTask(int dealDamage, MapleCharacter chr,
                           MonsterStatusEffect status, Runnable cancelTask, int type) {
            this.dealDamage = dealDamage;
            this.chr = chr;
            this.status = status;
            this.cancelTask = cancelTask;
            this.type = type;
            this.map = chr.getMap();
        }

        @Override
        public void run() {
            int damage = dealDamage;
            if (damage >= hp) {
                damage = hp - 1;
                if (type == 1 || type == 2) {
                    cancelTask.run();
                    status.getCancelTask().cancel(false);
                }
            }
            if (hp > 1 && damage > 0) {
                damage(chr, damage, false);
            }
        }
    }

    public String getName() {
        return stats.getName();
    }

    public void addStolen(int itemId) {
        stolenItems.add(itemId);
    }

    public List<Integer> getStolen() {
        return stolenItems;
    }

    public void setTempEffectiveness(Element e, ElementalEffectiveness ee, long milli) {
        final Element fE = e;
        final ElementalEffectiveness fEE = stats.getEffectiveness(e);
        if (!fEE.equals(ElementalEffectiveness.WEAK)) {
            stats.setEffectiveness(e, ee);

            MapleMap mmap = this.getMap();
            Runnable r = () -> {
                stats.removeEffectiveness(fE);
                stats.setEffectiveness(fE, fEE);
            };

            mmap.getChannelServer().registerMobClearSkillAction(mmap.getId(), r, milli);
        }
    }

    public Collection<MonsterStatus> alreadyBuffedStats() {
        statiLock.lock();
        try {
            return Collections.unmodifiableCollection(alreadyBuffed);
        } finally {
            statiLock.unlock();
        }
    }

    public BanishInfo getBanish() {
        return stats.getBanishInfo();
    }

    public void setBoss(boolean boss) {
        this.stats.setBoss(boss);
    }

    public int getDropPeriodTime() {
        return stats.getDropPeriod();
    }

    public int getPADamage() {
        return stats.getPADamage();
    }

    public Map<MonsterStatus, MonsterStatusEffect> getStati() {
        statiLock.lock();
        try {
            return Collections.unmodifiableMap(stati);
        } finally {
            statiLock.unlock();
        }
    }

    public MonsterStatusEffect getStati(MonsterStatus ms) {
        statiLock.lock();
        try {
            return stati.get(ms);
        } finally {
            statiLock.unlock();
        }
    }

    // ---- one can always have fun trying these pieces of codes below in-game rofl ----

    public final ChangeableStats getChangedStats() {
        return ostats;
    }

    public final int getMobMaxHp() {
        if (ostats != null) {
            return ostats.hp;
        }
        return stats.getHp();
    }

    public final void setOverrideStats(final OverrideMonsterStats ostats) {
        this.ostats = new ChangeableStats(stats, ostats);
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
    }

    public final void changeLevel(final int newLevel) {
        changeLevel(newLevel, true);
    }

    public final void changeLevel(final int newLevel, boolean pqMob) {
        if (!stats.isChangeable()) {
            return;
        }
        this.ostats = new ChangeableStats(stats, newLevel, pqMob);
        this.hp = ostats.getHp();
        this.mp = ostats.getMp();
    }

    private float getDifficultyRate(final int difficulty) {
        return switch (difficulty) {
            case 6 -> (7.7f);
            case 5 -> (5.6f);
            case 4 -> (3.2f);
            case 3 -> (2.1f);
            case 2 -> (1.4f);
            default -> (1.0f);
        };

    }

    private void changeLevelByDifficulty(final int difficulty, boolean pqMob) {
        changeLevel((int) (this.getLevel() * getDifficultyRate(difficulty)), pqMob);
    }

    public final void changeDifficulty(final int difficulty, boolean pqMob) {
        changeLevelByDifficulty(difficulty, pqMob);
    }

    // ---- aggro code ---
    public MapleCharacter getController() {
        return controller.get();
    }

    public void setController(MapleCharacter controller) {
        this.controller = new WeakReference<>(controller);
    }

    public void switchController(MapleCharacter newController, boolean immediateAggro) {
        MapleCharacter controllers = getController();
        if (controllers == newController) {
            return;
        }
        if (controllers != null) {
            controllers.stopControllingMonster(this);
            controllers.getClient().announce(
                    MaplePacketCreator.stopControllingMonster(getObjectId()));
        }
        newController.controlMonster(this, immediateAggro);
        setController(newController);
        if (immediateAggro) {
            setControllerHasAggro(true);
        }
        setControllerKnowsAboutAggro(false);
    }

    public boolean isControllerHasAggro() {
        return fake ? false : controllerHasAggro;
    }

    public void setControllerHasAggro(boolean controllerHasAggro) {
        if (fake) {
            return;
        }
        this.controllerHasAggro = controllerHasAggro;
    }

    public boolean isControllerKnowsAboutAggro() {
        return fake ? false : controllerKnowsAboutAggro;
    }

    public void setControllerKnowsAboutAggro(boolean controllerKnowsAboutAggro) {
        if (fake) {
            return;
        }
        this.controllerKnowsAboutAggro = controllerKnowsAboutAggro;
    }

    public final int getRemoveAfter() {
        return stats.removeAfter();
    }

    public void dispose() {
        if (monsterItemDrop != null) {
            monsterItemDrop.cancel(false);
        }
        this.getMap().dismissRemoveAfter(this);
        disposeLocks();
    }

    private void disposeLocks() {
        LockCollector.getInstance().registerDisposeAction(() -> emptyLocks());
    }

    private void emptyLocks() {
        externalLock = externalLock.dispose();
        //monsterLock = monsterLock.dispose();
        statiLock = statiLock.dispose();
        animationLock = animationLock.dispose();
    }

    public void dropFromFriendlyMonster(long delay) {
        final MapleMonster m = this;
        monsterItemDrop = TimerManager.getInstance().register(new Runnable() {
            @Override
            public void run() {
                if (!m.isAlive()) {
                    if (monsterItemDrop != null) {
                        monsterItemDrop.cancel(false);
                    }

                    return;
                }

                MapleMap map = m.getMap();
                List<MapleCharacter> chrList = map.getAllPlayers();
                if (!chrList.isEmpty()) {
                    MapleCharacter chr = (MapleCharacter) chrList.get(0);

                    EventInstanceManager eim = map.getEventInstance();
                    if (eim != null) {
                        eim.friendlyItemDrop(m);
                    }

                    map.dropFromFriendlyMonster(chr, m);
                }
            }
        }, delay, delay);
    }

    public int getParentMob() {
        return parentMob;
    }

    public void setParentMob(int parentMob) {
        this.parentMob = parentMob;
    }

    private class AttackingMapleCharacter {

        private MapleCharacter attacker;
        private long lastAttackTime;

        public AttackingMapleCharacter(MapleCharacter attacker, long lastAttackTime) {
            super();
            this.attacker = attacker;
            this.lastAttackTime = lastAttackTime;
        }

        public long getLastAttackTime() {
            return lastAttackTime;
        }

        public MapleCharacter getAttacker() {
            return attacker;
        }
    }

    private interface AttackerEntry {

        List<AttackingMapleCharacter> getAttackers();

        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime);

        public int getDamage();

        public boolean contains(MapleCharacter chr);

        public void killedMob(MapleMap map, int baseExp, boolean mostDamage);
    }

    private class SingleAttackerEntry implements AttackerEntry {

        private int damage;
        private int chrid;
        private long lastAttackTime;
        private Channel cserv;

        public SingleAttackerEntry(MapleCharacter from, Channel cserv) {
            this.chrid = from.getId();
            this.cserv = cserv;
        }

        @Override
        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
            if (chrid == from.getId()) {
                this.damage += damage;
            } else {
                throw new IllegalArgumentException("Not the attacker of this entry");
            }
            if (updateAttackTime) {
                lastAttackTime = System.currentTimeMillis();
            }
        }

        @Override
        public List<AttackingMapleCharacter> getAttackers() {
            MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(chrid);
            if (chr != null) {
                return Collections.singletonList(new AttackingMapleCharacter(chr, lastAttackTime));
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return chrid == chr.getId();
        }

        @Override
        public int getDamage() {
            return damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
            MapleCharacter chr = map.getCharacterById(chrid);
            if (chr != null) {
                giveExpToCharacter(chr, baseExp, mostDamage, 1);
            }
        }

        @Override
        public int hashCode() {
            return chrid;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final SingleAttackerEntry other = (SingleAttackerEntry) obj;
            return chrid == other.chrid;
        }
    }

    private static class OnePartyAttacker {

        public MapleParty lastKnownParty;
        public int damage;
        public long lastAttackTime;

        public OnePartyAttacker(MapleParty lastKnownParty, int damage) {
            this.lastKnownParty = lastKnownParty;
            this.damage = damage;
            this.lastAttackTime = System.currentTimeMillis();
        }
    }

    private class PartyAttackerEntry implements AttackerEntry {

        private int totDamage;
        private Map<Integer, OnePartyAttacker> attackers;
        private Channel cserv;
        private int partyid;

        public PartyAttackerEntry(int partyid, Channel cserv) {
            this.partyid = partyid;
            this.cserv = cserv;
            attackers = new HashMap<>(6);
        }

        @Override
        public List<AttackingMapleCharacter> getAttackers() {
            List<AttackingMapleCharacter> ret = new ArrayList<>(attackers.size());
            for (Entry<Integer, OnePartyAttacker> entry : attackers.entrySet()) {
                MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(entry.getKey());
                if (chr != null) {
                    ret.add(new AttackingMapleCharacter(chr, entry.getValue().lastAttackTime));
                }
            }
            return ret;
        }

        private Map<MapleCharacter, OnePartyAttacker> resolveAttackers() {
            Map<MapleCharacter, OnePartyAttacker> ret = new HashMap<>(attackers.size());
            for (Entry<Integer, OnePartyAttacker> aentry : attackers.entrySet()) {
                MapleCharacter chr = cserv.getPlayerStorage().getCharacterById(aentry.getKey());
                if (chr != null) {
                    ret.put(chr, aentry.getValue());
                }
            }
            return ret;
        }

        @Override
        public boolean contains(MapleCharacter chr) {
            return attackers.containsKey(chr.getId());
        }

        @Override
        public int getDamage() {
            return totDamage;
        }

        @Override
        public void addDamage(MapleCharacter from, int damage, boolean updateAttackTime) {
            OnePartyAttacker oldPartyAttacker = attackers.get(from.getId());
            if (oldPartyAttacker != null) {
                oldPartyAttacker.damage += damage;
                oldPartyAttacker.lastKnownParty = from.getParty();
                if (updateAttackTime) {
                    oldPartyAttacker.lastAttackTime = System.currentTimeMillis();
                }
            } else {
                // TODO actually this causes wrong behaviour when the party changes between attacks
                // only the last setup will get exp - but otherwise we'd have to store the full party
                // constellation for every attack/everytime it changes, might be wanted/needed in the
                // future but not now
                OnePartyAttacker onePartyAttacker = new OnePartyAttacker(from.getParty(), damage);
                attackers.put(from.getId(), onePartyAttacker);
                if (!updateAttackTime) {
                    onePartyAttacker.lastAttackTime = 0;
                }
            }
            totDamage += damage;
        }

        @Override
        public void killedMob(MapleMap map, int baseExp, boolean mostDamage) {
            Map<MapleCharacter, OnePartyAttacker> attackers_ = resolveAttackers();
            MapleCharacter highest = null;
            int highestDamage = 0;
            int partyMembersInMap = 0;
            int afkPartyMembersInMap = 0;
            Map<MapleCharacter, Integer> expMap = new ArrayMap<>(6);
            for (Entry<MapleCharacter, OnePartyAttacker> attacker : attackers_.entrySet()) {
                MapleParty party = attacker.getValue().lastKnownParty;
                double averagePartyLevel = 0;
                List<MapleCharacter> expApplicable = new ArrayList<>();
                for (MaplePartyCharacter partychar : party.getMembers()) {
                    if (attacker.getKey().getLevel() - partychar.getLevel() <= 5
                            || getLevel() - partychar.getLevel() <= 5) {
                        MapleCharacter pchr
                                = cserv.getPlayerStorage().getCharacterByName(partychar.getName());
                        if (pchr != null) {
                            if (pchr.isAlive() && pchr.getMap() == map) {
                                expApplicable.add(pchr);
                                if (map.getPartyBonusRate() > 0) {
                                    partyMembersInMap += 1;
                                    if (System.currentTimeMillis() - getMap().getAfkTime(pchr) > 60000) { // prevent user from boosting their own EXP in party play maps
                                        afkPartyMembersInMap += 1;
                                        //pchr.titleMessage("Your party play bonus EXP effect has been deactivated due to inactivity.");
                                    }
                                }
                                averagePartyLevel += pchr.getLevel();
                            }
                        }
                    }
                }
                double expBonus = 1.0;
                if (expApplicable.size() > 1) {
                    expBonus = 1.10 + 0.05 * expApplicable.size();
                    averagePartyLevel /= expApplicable.size();
                }
                int partyPlayBonus = 0;
                if (partyMembersInMap > 0) { // begin calc for party play rate
                    Integer holySymbol = highest.getBuffedValue(MapleBuffStat.HOLY_SYMBOL);
                    if (holySymbol == null) {
                        partyPlayBonus = (int) Math.round(baseExp * (((partyMembersInMap - 1) - afkPartyMembersInMap) * .10));
                    }
                }
                int iDamage = attacker.getValue().damage;
                if (iDamage > highestDamage) {
                    highest = attacker.getKey();
                    highestDamage = iDamage;
                }
                double innerBaseExp = (baseExp + partyPlayBonus) * ((double) iDamage / totDamage);
                double expFraction = (innerBaseExp * expBonus) / (expApplicable.size() + 1);
                for (MapleCharacter expReceiver : expApplicable) {
                    Integer oexp = expMap.get(expReceiver);
                    int iexp;
                    if (oexp == null) {
                        iexp = 0;
                    } else {
                        iexp = oexp;
                    }
                    double expWeight = (expReceiver == attacker.getKey() ? 2.0 : 1.0);
                    iexp += (int) Math.round(expFraction * expWeight);
                    expMap.put(expReceiver, Integer.valueOf(iexp));
                }
            }
            for (Entry<MapleCharacter, Integer> expReceiver : expMap.entrySet()) {
                giveExpToCharacter(expReceiver.getKey(), expReceiver.getValue(),
                        mostDamage ? expReceiver.getKey() == highest : false,
                        expMap.size());
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + partyid;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final PartyAttackerEntry other = (PartyAttackerEntry) obj;
            return partyid == other.partyid;
        }
    }
}