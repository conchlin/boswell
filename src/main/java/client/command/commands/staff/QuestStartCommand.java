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
package client.command.commands.staff;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;

public class QuestStartCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);

        if (params.length < 2) {
            player.yellowMessage("Syntax: !startquest <playername> <questid>");
            return;
        }

        if (params.length > 2) {
            player.yellowMessage("Syntax: !startquest <playername> <questid>");
            return;
        }

        int questId = Integer.parseInt(params[1]);

        if (victim != null) {
            if (victim.getQuestStatus(questId) == 0) {
                //victim.getAbstractPlayerInteraction().forceStartQuest(questId);
                player.dropMessage(5, victim + ": QUEST " + questId + " started.");
                victim.dropMessage(5, player + ": QUEST " + questId + " started.");
            } else {
                player.dropMessage(5, victim + ": QUESTID " + questId + " already started/completed.");
                victim.dropMessage(5, player + ": QUESTID " + questId + " already started/completed.");
            }
        } else {
            player.message("Player '" + params[0] + "' could not be found.");
        }
    }
}
