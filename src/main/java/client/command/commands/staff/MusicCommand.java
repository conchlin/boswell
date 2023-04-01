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

import client.command.Command;
import client.MapleClient;
import client.MapleCharacter;
import constants.GameConstants;
import enums.FieldEffectType;
import network.packet.field.CField;
import network.packet.ScriptMan;

public class MusicCommand extends Command {
    {
        setDescription("");
    }

    private static String getSongList() {
        StringBuilder songList = new StringBuilder("Song:\r\n");
        for (String s : GameConstants.GAME_SONGS) {
            songList.append("  ").append(s).append("\r\n");
        }
        
        return songList.toString();
    }
    
    @Override
    public void execute(MapleClient c, String[] params) {
        
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            String sendMsg = "";
            
            sendMsg += "Syntax: #r!music <song>#k\r\n\r\n";
            sendMsg += getSongList();

            c.announce(ScriptMan.Packet.onSay(1052015, sendMsg, false, false));
            return;
        }
        
        String song = player.getLastCommandMessage();
        for (String s : GameConstants.GAME_SONGS) {
            if (s.equalsIgnoreCase(song)) {    // thanks Masterrulax for finding an issue here
                player.getMap().broadcastMessage(CField.Packet.onFieldEffect(FieldEffectType.Music.getMode(), s));
                player.yellowMessage("Now playing song " + s + ".");
                return;
            }
        }
        
        String sendMsg = "";
        sendMsg += "Song not found, please enter a song below.\r\n\r\n";
        sendMsg += getSongList();

        c.announce(ScriptMan.Packet.onSay(1052015, sendMsg, false, false));
    }
}
