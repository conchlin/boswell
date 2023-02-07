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

import client.MapleClient;
import client.autoban.AutobanFactory;
import net.AbstractMaplePacketHandler;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.maps.MapleMap;
import tools.FilePrinter;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Jay Estrella
 */
public final class MobAttackMobHandler extends AbstractMaplePacketHandler {
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        int from = slea.readInt();
        slea.readInt();
        int to = slea.readInt();
        slea.readByte();
        int dmg = slea.readInt();
        
        MapleMap map = c.getPlayer().getMap();
        MapleMonster attacker = map.getMonsterByOid(from);
        MapleMonster damaged = map.getMonsterByOid(to);

        if (attacker != null && damaged != null) {
            if (dmg < 1 || dmg >= damaged.getMaxHp()) {
                AutobanFactory.DAMAGE_HACK.alert(
                        c.getPlayer(), "Possible packet editing hypnotize damage exploit.");
                FilePrinter.printError(
                        FilePrinter.EXPLOITS + c.getPlayer().getName() + ".txt", c.getPlayer().getName()
                                + " had hypnotized " + MapleMonsterInformationProvider.getInstance().
                                getMobNameFromId(attacker.getId()) + " to attack "
                                + MapleMonsterInformationProvider.getInstance().getMobNameFromId(damaged.getId())
                                + " with damage " + dmg);
            }

            map.damageMonster(c.getPlayer(), damaged, dmg);
            //map.broadcastMessage(c.getPlayer(), MaplePacketCreator.damageMonster(to, dmg), false);
        }
    }
}
