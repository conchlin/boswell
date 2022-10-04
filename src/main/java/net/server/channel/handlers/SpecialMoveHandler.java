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

import java.awt.Point;
import java.util.concurrent.ScheduledFuture;

import net.AbstractMaplePacketHandler;
import network.packet.MobPool;
import network.packet.UserLocal;
import network.packet.UserRemote;
import network.packet.WvsContext;
import server.MapleStatEffect;
import server.TimerManager;
import server.life.MapleMonster;
import server.skills.PlayerSkill;
import server.skills.SkillFactory;
import tools.MaplePacketCreator;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleCharacter;
import client.MapleCharacter.CancelCooldownAction;
import client.MapleClient;
import constants.skills.Brawler;
import constants.skills.Corsair;
import constants.skills.DarkKnight;
import constants.skills.Hero;
import constants.skills.Paladin;
import constants.skills.Priest;
import constants.skills.SuperGM;

public final class SpecialMoveHandler extends AbstractMaplePacketHandler {
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
    	MapleCharacter chr = c.getPlayer();
        slea.readInt();
        int skillid = slea.readInt();
   
        Point pos = null;
        int __skillLevel = slea.readByte();
        PlayerSkill skill = SkillFactory.getSkill(skillid);
        int skillLevel = chr.getSkillLevel(skill);
        if (skillid % 10000000 == 1010 || skillid % 10000000 == 1011) {
            if (chr.getDojoEnergy() < 10000) { // PE hacking or maybe just lagging
                return;
            }
            skillLevel = 1;
            chr.setDojoEnergy(0);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
            c.announce(MaplePacketCreator.serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        }
        if (skillLevel == 0 || skillLevel != __skillLevel) return;
        
        MapleStatEffect effect = skill.getEffect(skillLevel);
        if (effect.getCooldown() > 0) {
            if (chr.skillIsCooling(skillid)) {
                return;
            } else if (skillid != Corsair.BATTLE_SHIP) {
                int cooldownTime = effect.getCooldown();
                
                c.announce(UserLocal.Packet.skillCooldown(skillid, cooldownTime));
                ScheduledFuture<?> timer = TimerManager.getInstance().schedule(
                        new CancelCooldownAction(c.getPlayer(), skillid), effect.getCooldown() * 1000);
                chr.addCooldown(skillid, System.currentTimeMillis(), effect.getCooldown() * 1000, timer);
            }
        }
        if (skillid == Hero.MONSTER_MAGNET || skillid == Paladin.MONSTER_MAGNET || skillid == DarkKnight.MONSTER_MAGNET) { // Monster Magnet
            int num = slea.readInt();
            for (int i = 0; i < num; i++) {
                int mobOid = slea.readInt();
                byte success = slea.readByte();
                chr.getMap().broadcastMessage(chr, MobPool.Packet.catchMonster(mobOid, success), false);
                MapleMonster monster = chr.getMap().getMonsterByOid(mobOid);
                if (monster != null && success > 0) {
                    if (!monster.isBoss()) {
                        monster.switchController(c.getPlayer(), monster.isControllerHasAggro());
                    }
                }
            }
            byte direction = slea.readByte();   // thanks MedicOP for pointing some 3rd-party related issues with Magnet
            chr.getMap().broadcastMessage(chr, UserRemote.Packet.showBuffEffect(chr.getId(), skillid, chr.getSkillLevel(skillid), 1, direction), false);
            c.announce(WvsContext.Packet.enableActions());
            return;
        } else if (skillid == Brawler.MP_RECOVERY) {// MP Recovery
            PlayerSkill s = SkillFactory.getSkill(skillid);
            MapleStatEffect ef = s.getEffect(chr.getSkillLevel(s));
            
            int lose = chr.safeAddHP(-1 * (chr.getCurrentMaxHp() / ef.getX()));
            int gain = -lose * (ef.getY() / 100);
            chr.addMP(gain);
        } else if (skillid == SuperGM.HEAL_PLUS_DISPEL) {
            slea.skip(11);
            chr.getMap().broadcastMessage(chr, UserRemote.Packet.showBuffEffect(chr.getId(), skillid, chr.getSkillLevel(skillid)), false);
        } else if (skillid % 10000000 == 1004) {
            slea.readShort();
        }
        if (slea.available() > 3) {
            pos = new Point(slea.readShort(), slea.readShort());
        }
        if (chr.isAlive()) {
            if (skill.getId() == Priest.MYSTIC_DOOR && !chr.canDoor()) {
                chr.message("Please wait 5 seconds before casting Mystic Door again");
                c.announce(WvsContext.Packet.enableActions());
            } else {
                skill.getEffect(skillLevel).applyTo(c.getPlayer(), pos);
            }

        } else {
            c.announce(WvsContext.Packet.enableActions());
        }
    }
}