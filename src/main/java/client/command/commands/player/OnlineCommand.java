/*
    This file is part of the HeavenMS MapleStory Server, commands OdinMS-based
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

/*
   @Author: Arthur L - Refactored command content into modules
   @Author: saffron
*/
package client.command.commands.player;

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;
import java.util.LinkedList;
import java.util.List;
import net.server.Server;
import net.server.channel.Channel;

public class OnlineCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        
        List<String> onlineChar = new LinkedList<>();
        MapleCharacter player = c.getPlayer();
        
        for (Channel ch : Server.getInstance().getChannelsFromWorld(player.getWorld())) {
            player.yellowMessage("Players in Channel " + ch.getId() + ":");
            
            for (MapleCharacter chr : ch.getPlayerStorage().getAllCharacters()) {
                if (!chr.isGM()) {
                    onlineChar.add(chr.getName());
                }
            }
            
            player.message(onlineChar.toString().replace("[", "").replace("]", ""));
            onlineChar.clear();
        }
    }
}
