/*
    This file is part of the Noblestory MapleStory Server, commands OdinMS-based
    Copyleft (L) 2019 Saffron

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

package client.command.commands.staff;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;

/**
 *
 * @author Saffron
 * @since 6/12/2019
 */
public class ClearanceCommand extends Command {
    {
        setDescription("");
    }
    
    @Override
     public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        String name;
        int clearance;
        if (params.length < 1) {
            player.yellowMessage("Syntax: !clearance [<playername>] <level>");
            return;
        }
        if (params.length > 2) {
            name = params[1];
            clearance = Integer.parseInt(params[2]);
        } else {
            name = c.getPlayer().getName();
            clearance = Integer.parseInt(params[1]);
        }
        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(name);
        if (victim != null) {
            victim.getClient().setClearance((byte) clearance);
            player.message(name + " has been given " + clearance + " level clearance.");
        } else {
            player.message("Player '" + name + "' could not be found.");
        }
     }
}
