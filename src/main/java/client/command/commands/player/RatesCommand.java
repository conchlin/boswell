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

import client.MapleCharacter;
import client.command.Command;
import client.MapleClient;

public class RatesCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        
        String showMsg_ = "#eServer Rates#n" + "\r\n\r\n";
        showMsg_ += "Exp Rate: #e#b" + player.getExpRate() + "x#k#n\r\n";
        showMsg_ += "Mesos Rate: #e#b" + player.getMesoRate() + "x#k#n" + "\r\n";
        showMsg_ += "Quest Rate: #e#b" + c.getWorldServer().getQuestRate() + "x#k#n" + "\r\n\r\n";
        showMsg_ += "#e#bPlease reference our website's library for drop rates#k#n" + "\r\n";

        player.showHint(showMsg_, 300);
    }
}
