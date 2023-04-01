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

import client.command.Command;
import client.MapleClient;
import java.util.Arrays;
import java.util.List;
import server.MapleItemInformationProvider;
import server.gachapon.MapleGach;

public class GachaCommand extends Command {
    {
        setDescription("<location name>");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleGach.Gachapon gacha = null;
        
        String search = c.getPlayer().getLastCommandMessage();
        String gachaName = "";
        List<String> names = Arrays.asList("Global","Henesys", "Ellinia", "Perion", "Kerning City", "Sleepywood", "Mushroom Shrine", "Showa Spa M", "Showa Spa F", "New Leaf City", "Nautilus Harbor");
        int [] ids = {-1, 9100100, 9100101, 9100102, 9100103, 9100104, 9100105, 9100106, 9100107, 9100109, 9100117};
        
        for (int i = 0; i < names.size(); i++){
            if (search.equalsIgnoreCase(names.get(i))){
                gachaName = names.get(i);
                gacha = MapleGach.Gachapon.getByNpcId(ids[i]);
            }
        }
        
        if (gacha == null) {
            c.getPlayer().showHint("Please use @gachapon <location>", 150);
            c.getPlayer().yellowMessage("Gachapon Lccations:");
            c.getPlayer().message(names.toString().replace("[", "").replace("]", ""));
            return;
        }
        
        String talkStr = "Please note that all Gachapons include the global gach list too. \r\n The #b" + gachaName + "#k Gachapon contains the following items.\r\n\r\n";
        for (int i = 0; i < 3; i++){ // let's actual pull all three tiers smh
            for (int id : gacha.getItems(i)){
                //talkStr += "-" + MapleItemInformationProvider.getInstance().getName(id) + "\r\n";
                talkStr += "#v" + id + "# - #z" + id + "# \r\n";
            }
        }
      
        //c.getAbstractPlayerInteraction().npcTalk(9010000, talkStr);
    }
}
