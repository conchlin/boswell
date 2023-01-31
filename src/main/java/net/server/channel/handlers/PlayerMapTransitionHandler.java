/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

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

import client.BuffValueHolder;
import client.MapleBuffStat;
import client.MapleCharacter;
import client.MapleClient;
import java.util.Collections;
import java.util.List;
import net.AbstractMaplePacketHandler;
import network.packet.wvscontext.WvsContext;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Ronan
 */
public final class PlayerMapTransitionHandler extends AbstractMaplePacketHandler {

    // this whole file seems useless
    
    @Override
    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        MapleCharacter chr = c.getPlayer();
        chr.setMapTransitionComplete();
        
        int beaconid = chr.getBuffSource(MapleBuffStat.HOMING_BEACON);
        if (beaconid != -1) {
            chr.cancelBuffStats(MapleBuffStat.HOMING_BEACON);
            
            final List<Pair<MapleBuffStat, BuffValueHolder>> stat = Collections.singletonList(new Pair<>(
                    MapleBuffStat.HOMING_BEACON, new BuffValueHolder(0, 0, 0)));
            chr.announce(WvsContext.Packet.giveBuff(1, beaconid, stat));
        }
    }
}