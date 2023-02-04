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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import client.*;
import client.MapleCharacter.CancelCooldownAction;
import network.packet.UserLocal;
import network.packet.UserRemote;
import network.packet.context.WvsContext;
import server.MapleStatEffect;
import server.TimerManager;
import server.skills.PlayerSkill;
import server.skills.SkillFactory;
import tools.MaplePacketCreator;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;
import constants.GameConstants;
import constants.ServerConstants;
import constants.skills.Crusader;
import constants.skills.DawnWarrior;
import constants.skills.DragonKnight;
import constants.skills.Hero;
import constants.skills.NightWalker;
import constants.skills.Rogue;

public final class CloseRangeDamageHandler extends AbstractDealDamageHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();

        if(chr == null) return;
        AttackInfo attack = parseDamage(slea, chr, false, false);
        if (chr.getBuffEffect(MapleBuffStat.MORPH) != null) {
            if(chr.getBuffEffect(MapleBuffStat.MORPH).isMorphWithoutAttack()) {
                // How are they attacking when the client won't let them?
                chr.getClient().disconnect(false, false);
                return; 
            }
        }

        if (chr.getDojoEnergy() < 10000 && (attack.skill == 1009 || attack.skill == 10001009 || attack.skill == 20001009)) // PE hacking or maybe just lagging
            return;
        if (chr.getMap().isDojoMap() && attack.numAttacked > 0) {
            chr.setDojoEnergy(chr.getDojoEnergy() + ServerConstants.DOJO_ENERGY_ATK);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
        }
        
        chr.getMap().broadcastMessage(chr, UserRemote.Packet.onMeleeAttack(chr, attack.skill, attack.skilllevel,
                attack.stance, attack.numAttackedAndDamage, attack.allDamage, attack.speed,
                attack.direction, attack.display), false, true);
        int numFinisherOrbs = 0;
        Integer comboBuff = chr.getBuffedValue(MapleBuffStat.COMBO);
        if (GameConstants.isFinisherSkill(attack.skill)) {
            if (comboBuff != null) {
                numFinisherOrbs = comboBuff.intValue() - 1;
            }
            chr.handleOrbconsume();
        } else if (attack.numAttacked > 0) {
            if (attack.skill != 1111008 && comboBuff != null) {
                int orbcount = chr.getBuffedValue(MapleBuffStat.COMBO);
                int oid = chr.isCygnus() ? DawnWarrior.COMBO : Crusader.COMBO;
                int advcomboid = chr.isCygnus() ? DawnWarrior.ADVANCED_COMBO : Hero.ADVANCED_COMBO;
                PlayerSkill combo = SkillFactory.getSkill(oid);
                PlayerSkill advcombo = SkillFactory.getSkill(advcomboid);
                MapleStatEffect ceffect;
                int advComboSkillLevel = chr.getSkillLevel(advcombo);
                if (advComboSkillLevel > 0) {
                    ceffect = advcombo.getEffect(advComboSkillLevel);
                } else {
                    int comboLv = chr.getSkillLevel(combo);
                    if(comboLv <= 0 || chr.isGM()) comboLv = SkillFactory.getSkill(oid).getMaxLevel();
                    
                    if(comboLv > 0) ceffect = combo.getEffect(comboLv);
                    else ceffect = null;
                }
                if(ceffect != null) {
                    if (orbcount < ceffect.getX() + 1) {
                        int neworbcount = orbcount + 1;
                        if (advComboSkillLevel > 0 && ceffect.makeChanceResult()) {
                            if (neworbcount <= ceffect.getX()) {
                                neworbcount++;
                            }
                        }

                        int olv = chr.getSkillLevel(oid);
                        if(olv <= 0) olv = SkillFactory.getSkill(oid).getMaxLevel();
                        
                        int duration = combo.getEffect(olv).getDuration();
                        List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(
                                MapleBuffStat.COMBO, new BuffValueHolder(0, 0, neworbcount)));
                        chr.setBuffedValue(MapleBuffStat.COMBO, neworbcount);                 
                        duration -= (int) (currentServerTime() - chr.getBuffedStarttime(MapleBuffStat.COMBO));
                        c.announce(WvsContext.Packet.giveBuff(oid, duration, stat));
                        chr.getMap().broadcastMessage(chr, UserRemote.Packet.giveForeignBuff(chr.getId(), stat), false);
                    }
                }
            } else if (chr.getSkillLevel(chr.isCygnus() ? SkillFactory.getSkill(15100004)
                    : SkillFactory.getSkill(5110001)) > 0 && (chr.getJob().isA(MapleJob.MARAUDER) || chr.getJob().isA(MapleJob.THUNDERBREAKER2))) {
                for (int i = 0; i < attack.numAttacked; i++) {
                    chr.handleEnergyChargeGain();
                }
            }
        }
        if (attack.numAttacked > 0 && attack.skill == DragonKnight.SACRIFICE) {
            int totDamageToOneMonster = 0; // sacrifice attacks only 1 mob with 1 attack
            final Iterator<List<Integer>> dmgIt = attack.allDamage.values().iterator();
            if (dmgIt.hasNext()) {
                totDamageToOneMonster = dmgIt.next().get(0).intValue();
            }
            
            chr.safeAddHP(-1 * totDamageToOneMonster * attack.getAttackEffect(chr, null).getX() / 100);
        }
        if (attack.numAttacked > 0 && attack.skill == 1211002) {
            boolean advcharge_prob = false;
            int advcharge_level = chr.getSkillLevel(SkillFactory.getSkill(1220010));
            if (advcharge_level > 0) {
                advcharge_prob = SkillFactory.getSkill(1220010).getEffect(advcharge_level).makeChanceResult();
            }
            if (!advcharge_prob) {
                chr.cancelEffectFromBuffStat(MapleBuffStat.WK_CHARGE);
            }
        }
        int attackCount = 1;
        if (attack.skill != 0) {
            attackCount = attack.getAttackEffect(chr, null).getAttackCount();
        }
        if (numFinisherOrbs == 0 && GameConstants.isFinisherSkill(attack.skill)) {
            return;
        }
        if (attack.skill % 10000000 == 1009) { // bamboo
            if (chr.getDojoEnergy() < 10000) { // PE hacking or maybe just lagging
                return;
            }
            
            chr.setDojoEnergy(0);
            c.announce(MaplePacketCreator.getEnergy("energy", chr.getDojoEnergy()));
            c.announce(MaplePacketCreator.serverNotice(5, "As you used the secret skill, your energy bar has been reset."));
        } else if (attack.skill > 0) {
            PlayerSkill skill = SkillFactory.getSkill(attack.skill);
            MapleStatEffect effect_ = skill.getEffect(chr.getSkillLevel(skill));
            if (effect_.getCooldown() > 0) {
                if (chr.skillIsCooling(attack.skill)) {
                    return;
                } else {
                    c.announce(UserLocal.Packet.skillCooldown(attack.skill, effect_.getCooldown()));
                    chr.addCooldown(attack.skill, System.currentTimeMillis(), effect_.getCooldown() * 1000,
                            TimerManager.getInstance().schedule(new CancelCooldownAction(chr, attack.skill),
                                    effect_.getCooldown() * 1000));
                }
            }
        }
        if ((chr.getSkillLevel(SkillFactory.getSkill(NightWalker.VANISH)) > 0
                || chr.getSkillLevel(SkillFactory.getSkill(Rogue.DARK_SIGHT)) > 0)
                && chr.getBuffedValue(MapleBuffStat.DARKSIGHT) != null) {// && player.getBuffSource(MapleBuffStat.DARKSIGHT) != 9101004
            chr.cancelEffectFromBuffStat(MapleBuffStat.DARKSIGHT);
            chr.cancelBuffStats(MapleBuffStat.DARKSIGHT);
        }
        
        applyAttack(attack, chr, attackCount);
    }
}