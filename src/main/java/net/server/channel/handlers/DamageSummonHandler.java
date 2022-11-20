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

import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import net.AbstractMaplePacketHandler;
import network.packet.SummonedPool;
import server.life.MapleMonster;
import server.maps.MapleSummon;
import tools.data.input.SeekableLittleEndianAccessor;

public final class DamageSummonHandler extends AbstractMaplePacketHandler {
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int oid = slea.readInt();
        MapleSummon summon = null;
        MapleCharacter player = c.getPlayer();
        for (MapleSummon sum : player.getSummonsValues()) {
            if (sum.getObjectId() == oid) {
                summon = sum;
            }
        }
        int action = slea.readByte();
        int damage = slea.readInt();
        int monsterId = slea.readInt();
        boolean isLeft = slea.readByte() > 0;
        if (summon != null) {
            MapleMonster monster = player.getMap().getMonsterById(monsterId);
            if (monster != null) {
                summon.addHP(-damage);
                if (summon.getHP() <= 0) {
                    player.cancelEffectFromBuffStat(MapleBuffStat.PUPPET);
                }
                player.getMap().broadcastMessage(player, SummonedPool.Packet.onDamaged(player.getId(),
                        summon.getObjectId(), damage, action, monster.getId(), isLeft), summon.getPosition());
            }
        }
    }
}
