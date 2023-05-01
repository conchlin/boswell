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
import database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class UnBanCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !unban <playername>");
            return;
        }
        int aid = MapleCharacter.getAccountIdByName(params[0]);
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement p = con.prepareStatement("UPDATE accounts SET banned = false WHERE id = " + aid)) {
                p.executeUpdate();
            }
            try (PreparedStatement p = con.prepareStatement("DELETE FROM ip_bans WHERE aid = " + aid)) {
                p.executeUpdate();
            }
            try (PreparedStatement p = con.prepareStatement("DELETE FROM mac_bans WHERE aid = " + aid)) {
                p.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            player.message("Failed to unban " + params[0]);
            return;
        }
        player.message("Unbanned " + params[0]);
    }
}
