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
*/
package client.command.commands.player;

import client.MapleClient;
import client.command.Command;
import tools.MaplePacketCreator;

public class StaffCommand extends Command {
    {
        setDescription(" Displays current staff list");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        c.announce(MaplePacketCreator.serverNotice(5, "Boswell Staff:"));
        c.announce(MaplePacketCreator.serverNotice(5, "Contempt - Admin"));
        c.announce(MaplePacketCreator.serverNotice(5, "Saffron - Admin/Developer"));
        c.announce(MaplePacketCreator.serverNotice(5, "michu - Developer"));
        c.announce(MaplePacketCreator.serverNotice(5, "w - Developer"));
        c.announce(MaplePacketCreator.serverNotice(5, "Ponzu - Developer"));
        c.announce(MaplePacketCreator.serverNotice(5, "Elon - Game Master"));
        c.announce(MaplePacketCreator.serverNotice(5, "Frida - Game Master"));
        c.announce(MaplePacketCreator.serverNotice(5, "Alex - Game Master"));

    }
}