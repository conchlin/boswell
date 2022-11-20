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
import java.awt.Point;
import java.util.List;

import network.packet.MobPool;
import server.life.MapleMonster;
import server.skills.MobSkill;
import server.skills.MobSkillFactory;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.movement.LifeMovementFragment;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 * @author Danny (Leifde)
 * @author ExtremeDevilz
 * @author Ronan (HeavenMS)
 */
public final class MoveLifeHandler extends AbstractMovementPacketHandler {
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int objectid = slea.readInt();
        short moveid = slea.readShort();
        MapleCharacter chr = c.getPlayer();
        MapleMap map = chr.getMap();

        if (chr == null) return;
        if (chr.isChangingMaps()) return; // mob movement shuffle (mob OID on different maps) happening on map transitions

	    MapleMapObject mmo = map.getMapObject(objectid);
		
        if (mmo == null || mmo.getType() != MapleMapObjectType.MONSTER) {
            return;
	    }
                
	    MapleMonster monster = (MapleMonster) mmo;
        if (!monster.isAlive()) return;

        List<LifeMovementFragment> res;
        byte skillByte = slea.readByte(); //nMobCtrlState
        byte action = slea.readByte(); //nAction
        int skillInfo = slea.readInt(); //dwData - the mob skill and movement information
        int skillId = skillInfo & 0xFF;
        int skillLevel = (skillInfo >> 8) & 0xFF;
        int delay = (skillInfo >> 16) & 0xFF;//option this is for summon option
                
	    MobSkill toUse = null;
        if (skillByte == 1 && monster.getNoSkills() > 0) {
            int random = Randomizer.nextInt(monster.getNoSkills()); // size
            Pair<Integer, Integer> skillToUse = monster.getSkills().get(random);
            toUse = MobSkillFactory.getMobSkill(skillToUse.getLeft(), skillToUse.getRight());
            if (!monster.canUseSkill(toUse, true)) { // test
                toUse = null;
            }
        }

      /*  if (action >= MobActions.MOVE.getAction()) {
            if (action < MobActions.ATTACK1.getAction() || action > MobActions.ATTACKF.getAction()) {
            	if (action >= MobActions.SKILL1.getAction() && action <= MobActions.SKILLF.getAction()) {
                   // return false;
            	}
            } else {
               // MobAttackInfo pAttackInfo = m_pTemplate.aAttackInfo.get(nAction - MobAct.Attack1.getAction());
            }
        }  */

        if ((skillId >= 100 && skillId <= 200) && monster.hasSkill(skillId, skillLevel)) {
            MobSkill skillData = MobSkillFactory.getMobSkill(skillId, skillLevel);
            if (skillData != null) {
                if (monster.canUseSkill(skillData, false)) { // test i've tried true here
                    skillData.applyEffect(chr, monster, delay);
                    //skillData.applyDelayedEffect(chr, monster, true, delay); // or should we use applyEffect?
                }
            }
        }

        slea.readByte(); //pvcActive->baseclass_0.m_bActive || CVecCtrlMob::IsCheatMobMoveRand(pvcActive)
        slea.readInt(); //nHackedCode
        slea.readInt(); //dwHackedCodeCRC ?
        slea.readInt(); //dwHackedCodeCRC ?
        short vx = slea.readShort();
        short vy = slea.readShort();
        Point p = new Point(vx, vy);
        res = parseMovement(slea);
        if (monster.getController() != chr) {
            if (monster.isAttackedBy(chr)) {// aggro and controller change
                monster.switchController(chr, true);
            }
        } else if (action == -1 && monster.isControllerKnowsAboutAggro() && !monster.isMobile() && !monster.isFirstAttack()) {
            monster.setControllerHasAggro(false);
            monster.setControllerKnowsAboutAggro(false);
        }
        boolean aggro = monster.isControllerHasAggro();
        if (toUse != null) {
            c.announce(MobPool.Packet.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro, toUse.getSkillId(), toUse.getSkillLevel()));
        } else {
            c.announce(MobPool.Packet.moveMonsterResponse(objectid, moveid, monster.getMp(), aggro));
        }
        if (aggro) monster.setControllerKnowsAboutAggro(true);

        if (res != null) {
            chr.getMap().broadcastMessage(chr, MobPool.Packet.onMove(skillByte, action, delay, skillInfo, objectid, p, res), monster.getPosition());
            updatePosition(res, monster, -1);
            chr.getMap().moveMonster(monster, monster.getPosition());
        }
    }
}