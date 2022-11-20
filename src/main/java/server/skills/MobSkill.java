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
package server.skills;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import client.MapleBuffStat;
import client.MapleCharacter;
import client.status.MonsterStatus;
import network.packet.MobPool;
import server.MapleStatEffect;
import server.life.Element;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.maps.AffectedArea;
import tools.ArrayMap;
import tools.MaplePacketCreator;
import tools.Randomizer;
import tools.data.output.MaplePacketLittleEndianWriter;

/**
 *
 * @author Danny (Leifde)
 */
public class MobSkill implements Skill {
    private int skillId, skillLevel, mpCon;
    private List<Integer> toSummon = new ArrayList<Integer>();
    private int spawnEffect, hp, x, y;
    private long duration, cooltime;
    private float prop;
    private Point lt, rb;
    private int limit;
	private MapleStatEffect effect;
	private int count;
	private int delay;
	
    public MobSkill(int skillId, int level) {
        this.skillId = skillId;
        this.skillLevel = level;
    }
	
	public MapleStatEffect getEffect() {
		if(effect == null) {
			effect = MapleStatEffect.loadDebuffEffectFromMobSkill(this);
		}
		return effect;
	}

    public void setMpCon(int mpCon) {
        this.mpCon = mpCon;
    }

    public void addSummons(List<Integer> toSummon) {
        for (Integer summon : toSummon) {
            this.toSummon.add(summon);
        }
    }

    public void setSpawnEffect(int spawnEffect) {
        this.spawnEffect = spawnEffect;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setCoolTime(long cooltime) {
        this.cooltime = cooltime;
    }

    public void setProp(float prop) {
        this.prop = prop;
    }

    public void setLtRb(Point lt, Point rb) {
        this.lt = lt;
        this.rb = rb;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

	public void applyEffect(MapleCharacter player, MapleMonster monster, int delay) {
		this.delay = delay;
		if (skillId >= 120 && skillId <= 136 && skillId != 130) {
			userStatChange(player, monster);
		} else if (skillId == 130) {
			affectAreaSkill(player, monster);
		} else if (skillId == 200) {
			summonSkill(player, monster);
		} else {
			mobStatChange(player, monster);
		}
	}

    private void affectAreaSkill(MapleCharacter player, MapleMonster monster) {
		monster.getMap().spawnMist(new AffectedArea(calculateBoundingBox(monster.getPosition(), true), monster, this), x * 10, false, false, false);
		monster.usedSkill(skillId, skillLevel, cooltime, duration);
        monster.setMp(monster.getMp() - getMpCon());
    }

	private void summonSkill(MapleCharacter player, MapleMonster monster) { //CMob::DoSkill_Summon Missing rectangular bounds check, and foothold calcs
		if (monster.getMap().getMonsters().size() >= 50) {
			return;
		}
		for (int mobId : getSummons()) {
			MapleMonster toSpawn = MapleLifeFactory.getMonster(mobId);
			monster.getMap().spawnMonsterWithEffect(toSpawn,  getSpawnEffect(), monster.getPosition());
		}
		monster.usedSkill(skillId, skillLevel, cooltime, duration);
		monster.setMp(monster.getMp() - getMpCon());
	}

	private void userStatChange(MapleCharacter player, MapleMonster monster) { //CMob::DoSkill_UserStatChange
		if (monster.getMap().getAllPlayers().size() < 0) {
			return;
		}
		int skillID = skillId | (skillLevel << 16);

		for (MapleCharacter chr : getRandomPlayersInRange(monster, player)) {
			if (!chr.isAlive()) {
				continue;
			}
/*			if (!makeChanceResult()) {
				System.out.println("makechanceresult failed");
				continue;
			}*/
			MapleBuffStat disease = null;
			switch (skillId) {
				case 120 -> disease = MapleBuffStat.SEAL;
				case 121 -> disease = MapleBuffStat.DARKNESS;
				case 122 -> disease = MapleBuffStat.WEAKEN;
				case 123 -> disease = MapleBuffStat.STUN;
				case 124 -> disease = MapleBuffStat.CURSE;
				case 125 -> disease = MapleBuffStat.POISON;
				case 126 -> disease = MapleBuffStat.SLOW;
				case 127 -> player.dispel();
				case 128 -> disease = MapleBuffStat.SEDUCE;
				case 129 -> {
					disease = MapleBuffStat.BAN_MAP;
					player.changeMapBanish(monster.getBanish().getMap(), monster.getBanish().getPortal(), monster.getBanish().getMsg());
				}
				case 132 -> disease = MapleBuffStat.CONFUSE;
				case 133 -> // zombify
						disease = MapleBuffStat.ZOMBIFY;
				case 134 -> disease = MapleBuffStat.STOP_POTION;
				case 135 -> disease = MapleBuffStat.STOP_MOTION;
				case 136 -> disease = MapleBuffStat.FEAR;
			}
/*			if (chr.getBuffEffect(disease) != null) {
				disease = null;
				continue;
			}*/
			if (skillId < 127) {
				if (chr.getBuffEffect(MapleBuffStat.HOLY_SHIELD) != null) {
					disease = null;
				}
			}
			if (disease != null) { // TODO when passed here applyTo does not actually work
				System.out.println("let's apply that disease: " + disease.name());
				getEffect().applyTo(chr);
			}
		}
		monster.usedSkill(skillId, skillLevel, cooltime, duration);
		monster.setMp(monster.getMp() - getMpCon());
	}

	private void mobStatChange(MapleCharacter player, MapleMonster monster) { //CMob::DoSkill_StatChange
		Map<MonsterStatus, Integer> stats = new ArrayMap<>();
		int skillID = skillId | (skillLevel << 16);
		switch (skillId) {
			case 100, 110, 150 -> stats.put(MonsterStatus.WEAPON_ATTACK_UP, x);
			case 101, 111, 151 -> stats.put(MonsterStatus.MAGIC_ATTACK_UP, x);
			case 102, 112, 152 -> stats.put(MonsterStatus.WEAPON_DEFENSE_UP, x);
			case 103, 113, 153 -> stats.put(MonsterStatus.MAGIC_DEFENSE_UP, x);
			case 114 -> {
				List<MapleMapObject> objects = getObjectsInRange(monster, MapleMapObjectType.MONSTER);
				int hps = (getX()) + (int) (Math.random() % getY());
				for (MapleMapObject mons : objects) {
					MapleMonster mon = (MapleMonster) mons;
					mon.heal(hps, 0);
					mon.getMap().broadcastMessage(player, MobPool.Packet.onAffected(mon.getObjectId(), skillID, 0), true);
				}
			}
			case 140 -> stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
			case 141 -> stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
			case 142 -> stats.put(MonsterStatus.HARD_SKIN, x);
			case 143 -> { // Weapon Reflect
				stats.put(MonsterStatus.WEAPON_REFLECT, x);
				stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
			}
			case 144 -> { // Magic Reflect
				stats.put(MonsterStatus.MAGIC_REFLECT, x);
				stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
			}
			case 145 -> { // Weapon / Magic reflect
				stats.put(MonsterStatus.WEAPON_REFLECT, x);
				stats.put(MonsterStatus.WEAPON_IMMUNITY, x);
				stats.put(MonsterStatus.MAGIC_REFLECT, x);
				stats.put(MonsterStatus.MAGIC_IMMUNITY, x);
			}
			case 154 -> // accuracy up
					stats.put(MonsterStatus.ACC, x);
			case 155 -> // avoid up
					stats.put(MonsterStatus.AVOID, x);
			case 156 -> // speed up
					stats.put(MonsterStatus.SPEED, x);
			case 157 -> stats.put(MonsterStatus.SEAL_SKILL, x);
		}
		if (stats.size() > 0) {
			monster.applyMonsterBuff(stats, getX(), getSkillId(), getDuration(), this);
		}
		monster.usedSkill(skillId, skillLevel, cooltime, duration);
		monster.setMp(monster.getMp() - getMpCon());
	}


	private List<MapleCharacter> getRandomPlayersInRange(MapleMonster monster, MapleCharacter player) {
    	List<MapleCharacter> ret = new ArrayList<>(count);
    	List<MapleCharacter> players = getPlayersInRange(monster, player);
		if(players.size() == 0)
			return ret;
		
    	for (int i = 0; i < count; i++) {
    		MapleCharacter chr = players.get(Randomizer.nextInt(players.size()));
    		if (!ret.contains(chr)) {
    			ret.add(chr);
    		}
    	}
    	return ret;
    }
    
    private List<MapleCharacter> getPlayersInRange(MapleMonster monster, MapleCharacter player) {
        List<MapleCharacter> players = new ArrayList<MapleCharacter>(count);
        if (lt == null || rb == null) {
			players.add(player);
        	return players;
        }
        return monster.getMap().getPlayersInRange(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), players);
    }
    
    public int getSkillId() {
        return skillId;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public int getMpCon() {
        return mpCon;
    }

    public List<Integer> getSummons() {
        return Collections.unmodifiableList(toSummon);
    }

    public int getSpawnEffect() {
        return spawnEffect;
    }

    public int getHP() {
        return hp;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public long getDuration() {
        return duration;
    }

    public long getCoolTime() {
        return cooltime;
    }

    public Point getLt() {
        return lt;
    }

    public Point getRb() {
        return rb;
    }

    public int getLimit() {
        return limit;
    }

    public boolean makeChanceResult() {
        return prop == 1.0 || Math.random() < prop;
    }
    
    public float getProp() {
    	return prop;
    }

    private Rectangle calculateBoundingBox(Point posFrom, boolean facingLeft) {
        int multiplier = facingLeft ? 1 : -1;
        Point mylt = new Point(lt.x * multiplier + posFrom.x, lt.y + posFrom.y);
        Point myrb = new Point(rb.x * multiplier + posFrom.x, rb.y + posFrom.y);
        return new Rectangle(mylt.x, mylt.y, myrb.x - mylt.x, myrb.y - mylt.y);
    }

    private List<MapleMapObject> getObjectsInRange(MapleMonster monster, MapleMapObjectType objectType) {
        List<MapleMapObjectType> objectTypes = new ArrayList<MapleMapObjectType>();
        objectTypes.add(objectType);
        return monster.getMap().getMapObjectsInBox(calculateBoundingBox(monster.getPosition(), monster.isFacingLeft()), objectTypes);
    }

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public void setId(int id) {
		this.skillId = id;
	}

	@Override
	public int getId() {
		return skillId;
	}

	@Override
	public void setLevel(int level) {
		this.skillLevel = level;
	}

	@Override
	public int getLevel() {
		return skillLevel;
	}

	@Override
	public Element getElement() {
		return null;
	}

	@Override
	public int getAnimationTime() {
		return 0;
	}

	@Override
	public void writeSkillInfo(MaplePacketLittleEndianWriter mplew) {
		mplew.writeShort(skillId);
		mplew.writeShort(skillLevel);
	}

	@Override
	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	public int getDelay() {
		return delay;
	}
}
