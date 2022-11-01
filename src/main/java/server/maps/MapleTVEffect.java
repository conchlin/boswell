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
import java.util.List;
import net.server.Server;
import network.packet.MapleTVMan;
import server.TimerManager;
import tools.MaplePacketCreator;

/*
 * MapleTVEffect
 * @author MrXotic
 * @author Ronan - made MapleTV mechanics synchronous
 */
public class MapleTVEffect {
	
	private final static boolean ACTIVE[] = new boolean[Server.getInstance().getWorldsSize()];
	
	public static synchronized boolean broadcastMapleTVIfNotActive(MapleCharacter player, MapleCharacter victim, List<String> messages, int tvType){
                int w = player.getWorld();
                if(!ACTIVE[w]) {
                        broadcastTV(true, w, messages, player, tvType, victim);
                        return true;
                }
            
		return false;
	}

	private static synchronized void broadcastTV(boolean activity, final int userWorld, List<String> message, MapleCharacter user, int type, MapleCharacter partner) {
		Server server = Server.getInstance();
		ACTIVE[userWorld] = activity;
		if (activity) {
			server.broadcastMessage(userWorld, MapleTVMan.Packet.enableTV());
			server.broadcastMessage(userWorld, MapleTVMan.Packet.sendTV(user, message, type <= 2 ? type : type - 3, partner));
			int delay = 15000;
			if (type == 4) {
				delay = 30000;
			} else if (type == 5) {
				delay = 60000;
			}
			TimerManager.getInstance().schedule(() -> broadcastTV(false, userWorld, null, null, -1, null), delay);
		} else {
			server.broadcastMessage(userWorld, MapleTVMan.Packet.removeTV());
		}
	}
}
