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

public class QuestCompleteCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);

        if (params.length < 2) {
            player.yellowMessage("Syntax: !completequest <playername> <questid>");
            return;
        }

        if (params.length > 2) {
            player.yellowMessage("Syntax: !completequest <playername> <questid>");
            return;
        }

        int questId = Integer.parseInt(params[1]);

        if (victim != null) {
            if (victim.getQuestStatus(questId) == 1) {
                victim.getAbstractPlayerInteraction().forceCompleteQuest(questId);
                player.dropMessage(5, victim + ": QUEST " + questId + " completed.");
                victim.dropMessage(5, player + ": QUEST " + questId + " completed.");
            } else {
                player.dropMessage(5, victim + ": QUESTID " + questId + " not started or already completed.");
                victim.dropMessage(5, player + ": QUESTID " + questId + " not started or already completed.");
            }
        } else {
            player.message("Player '" + params[0] + "' could not be found.");
        }
    }
}
