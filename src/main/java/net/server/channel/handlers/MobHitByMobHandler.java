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

import enums.BroadcastMessageType;
import net.AbstractMaplePacketHandler;
import network.packet.MobPool;
import network.packet.context.BroadcastMsgPacket;
import network.packet.context.WvsContext;
import server.life.MapleMonster;
import server.maps.MapleMap;
import tools.Randomizer;
import tools.data.input.SeekableLittleEndianAccessor;
import client.MapleClient;

/**
 * @author Xotic & BubblesDev
 */

public final class MobHitByMobHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int attacker = slea.readInt();
        slea.readInt();
        int damaged = slea.readInt();

        MapleMap map = c.getPlayer().getMap();
        MapleMonster monster = map.getMonsterByOid(damaged);

        if (monster == null || c.getPlayer().getMap().getMonsterByOid(attacker) == null) {
            return;
        }

        int damage = Randomizer.nextInt(((monster.getMaxHp() / 13 + monster.getPADamage() * 10)) * 2 + 500) / 10; //Beng's formula.
        //EventInstanceManager eim = map.getEventInstance();

        if (monster.getHp() - damage < 1) {     // friendly dies
            switch (monster.getId()) {
                case 9300102 -> // mount
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "The Watch Hog has been injured by the aliens. Better luck next time..."));
                case 9300061 ->//moon bunny
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "The Moon Bunny went home because he was sick."));
                case 9300093 ->//tylus
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "Tylus has fallen by the overwhelming forces of the ambush."));
                case 9300137 ->//juliet
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "Juliet has fainted in the middle of the combat."));
                case 9300138 ->//romeo
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "Romeo has fainted in the middle of the combat."));
                case 9400322, 9400327, 9400332 ->//snowman
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "The Snowman has melted on the heat of the battle."));
                case 9300162 ->//delli
                        map.broadcastMessage(BroadcastMsgPacket.Packet.onBroadcastMsg(BroadcastMessageType.BlueText.getType(),
                                "Delli vanished after the ambush, sheets still laying on the ground..."));
                default -> {
                }
            }

            map.killFriendlies(monster);
        } else {
            /*if (eim != null) {
                eim.friendlyDamaged(monster);
            }*/
        }

        //monster.applyAndGetHpDamage(damage, false);

        int remainingHp = monster.getHp();
        if (remainingHp <= 0) {
            remainingHp = 0;
            // TODO implement update method in eventinstancemanager (reference rien)
            //eim.update(new MobKilledEvent(this, monster, c.getPlayer()));
            map.removeMapObject(monster);
        }

        map.broadcastMessage(MobPool.Packet.onDamaged(monster, true, damage, remainingHp));
        c.announce(WvsContext.Packet.enableActions());
    }
}