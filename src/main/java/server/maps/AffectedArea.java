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
package server.maps;

import client.MapleCharacter;
import client.MapleClient;
import network.packet.AffectedAreaPool;
import server.skills.PlayerSkill;
import server.skills.Skill;

import java.awt.Point;
import java.awt.Rectangle;

import constants.skills.BlazeWizard;
import constants.skills.Evan;
import constants.skills.FPMage;
import constants.skills.NightWalker;
import constants.skills.Shadower;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.skills.MobSkill;
import server.skills.SkillFactory;
import tools.MaplePacketCreator;

/**
 *
 * @author LaiLaiNoob
 */
public class AffectedArea extends AbstractMapleMapObject {
    private Rectangle mistPosition;
    private MapleCharacter owner = null;
    private MapleMonster mob = null;
    private MapleStatEffect source;
    private MobSkill skill;
    private boolean isMobMist, isPoisonMist, isRecoveryMist;
    private int skillDelay;

    public AffectedArea(Rectangle mistPosition, MapleMonster mob, MobSkill skill) {
        this.mistPosition = mistPosition;
        this.mob = mob;
        this.skill = skill;
        isMobMist = true;
        isPoisonMist = true;
        isRecoveryMist = false;
        skillDelay = 0;
    }

    public AffectedArea(Rectangle mistPosition, MapleCharacter owner, MapleStatEffect source) {
        this.mistPosition = mistPosition;
        this.owner = owner;
        this.source = source;
        this.skillDelay = 8;
        this.isMobMist = false;
        this.isRecoveryMist = false;
        this.isPoisonMist = false;
        switch (source.getSourceId()) {
            case Evan.RECOVERY_AURA -> isRecoveryMist = true;
            case Shadower.SMOKE_SCREEN -> isPoisonMist = false;
            case FPMage.POISON_MIST, BlazeWizard.FLAME_GEAR, NightWalker.POISON_BOMB -> isPoisonMist = true;
        }
    }

    @Override
    public MapleMapObjectType getType() {
        return MapleMapObjectType.MIST;
    }

    @Override
    public Point getPosition() {
        return mistPosition.getLocation();
    }

    public PlayerSkill getSourceSkill() {
        return SkillFactory.getSkill(source.getSourceId());
    }

    public boolean isMobMist() {
        return isMobMist;
    }

    public boolean isPoisonMist() {
        return isPoisonMist;
    }

    public boolean isRecoveryMist() {
    	return isRecoveryMist;
    }
    
    public int getSkillDelay() {
        return skillDelay;
    }

    public MapleMonster getMobOwner() {
        return mob;
    }

    public MapleCharacter getOwner() {
        return owner;
    }

    public Rectangle getBox() {
        return mistPosition;
    }

    @Override
    public void setPosition(Point position) {
        throw new UnsupportedOperationException();
    }

    public final byte[] makeDestroyData() {
        return AffectedAreaPool.Packet.onAffectedAreaRemoved(getObjectId());
    }

    public final byte[] makeSpawnData() {
        if (owner != null) {
            return AffectedAreaPool.Packet.onAffectedAreaCreated(getObjectId(), owner.getId(), getSourceSkill().getId(), owner.getSkillLevel(SkillFactory.getSkill(source.getSourceId())), this);
        }
        return AffectedAreaPool.Packet.onAffectedAreaCreated(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    public final byte[] makeFakeSpawnData(int level) {
        if (owner != null) {
            return AffectedAreaPool.Packet.onAffectedAreaCreated(getObjectId(), owner.getId(), getSourceSkill().getId(), level, this);
        }
        return AffectedAreaPool.Packet.onAffectedAreaCreated(getObjectId(), mob.getId(), skill.getSkillId(), skill.getSkillLevel(), this);
    }

    @Override
    public void sendSpawnData(MapleClient client) {
        client.announce(makeSpawnData());
    }

    @Override
    public void sendDestroyData(MapleClient client) {
        client.announce(makeDestroyData());
    }

    public boolean makeChanceResult() {
        return source.makeChanceResult();
    }
}
