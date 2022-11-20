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

import client.*;
import client.autoban.AutobanFactory;
import client.status.MonsterStatusEffect;
import java.util.ArrayList;
import java.util.List;

import constants.skills.Outlaw;
import network.packet.SummonedPool;
import server.MapleStatEffect;
import server.life.MapleMonster;
import server.maps.MapleSummon;
import server.skills.PlayerSkill;
import server.skills.SkillFactory;
import tools.data.input.SeekableLittleEndianAccessor;

public final class SummonDamageHandler extends AbstractDealDamageHandler {
    
    public final class SummonAttackEntry {

        private int monsterOid;
        private int damage;
        
        public SummonAttackEntry(int monsterOid, int damage) {
            this.monsterOid = monsterOid;
            this.damage = damage;
        }

        public int getMonsterOid() {
            return monsterOid;
        }

        public int getDamage() {
            return damage;
        }
        
    }

    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleCharacter player = c.getPlayer();
        if (!player.isAlive()) {
            return;
        }
        MapleSummon summon = null;
        for (MapleSummon sum : player.getSummonsValues()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        if (summon == null) {
            return;
        }
        if (summon.getSkill() == Outlaw.GAVIOTA) {
            player.cancelBuffStats(MapleBuffStat.SUMMON);
        }
        PlayerSkill summonSkill = SkillFactory.getSkill(summon.getSkill());
        MapleStatEffect summonEffect = summonSkill.getEffect(summon.getSkillLevel());
        slea.readInt();
        List<SummonAttackEntry> allDamage = new ArrayList<>();
        byte animation = slea.readByte();
        int numAttacked = slea.readByte();
        slea.readShort();//mob x
        slea.readShort();//mob y
        slea.readShort();//summon x
        slea.readShort();// summon y
        for (int x = 0; x < numAttacked; x++) {
            int monsterOid = slea.readInt(); // attacked oid
            slea.readInt();
            slea.readByte();
            slea.readByte(); //nAction
            slea.readByte();
            slea.readByte();
            slea.readShort();
            slea.readShort();
            slea.readShort();
            slea.readShort();
            slea.readShort();
            int damage = slea.readInt();
            if (damage < 0) {
                AutobanFactory.DAMAGE_HACK.alert(player, "Summon Damage hack, " + damage);
                return;
            }
            allDamage.add(new SummonAttackEntry(monsterOid, damage));
        }
        slea.readInt();
        player.getMap().broadcastMessage(
                player, SummonedPool.Packet.onAttack(player.getId(), oid, animation, allDamage), summon.getPosition());

        for (SummonAttackEntry attackEntry : allDamage) {
            int damage = attackEntry.getDamage();
            MapleMonster target = player.getMap().getMonsterByOid(attackEntry.getMonsterOid());
            if (target != null) {
                if (damage > 0 && summonEffect.getMonsterStati().size() > 0) {
                    if (summonEffect.makeChanceResult()) {
                        target.applyStatus(player, new MonsterStatusEffect(summonEffect.getMonsterStati(), summonSkill, null, false, summonEffect.getX() * 1000), summonEffect.isPoison());
                    }
                }
                if (damage > 75000) {
                    AutobanFactory.SUMMON_DAMAGE.alert(
                            player, "DMG: " + damage + " SID: " + summon.getSkill() + " MobID: " + target.getId()
                            + " Map: " + player.getMap().getMapName() + " (" + player.getMapId() + ")");
                }

                player.getMap().damageMonster(player, target, damage);
            }
        }
    }
}
