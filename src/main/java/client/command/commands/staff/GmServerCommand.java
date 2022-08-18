package client.command.commands.staff;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import constants.ServerConstants;

public class GmServerCommand extends Command {
    {
        setDescription("use this command to either open or close the server to player connections");
    }

    @Override
    public void execute(MapleClient client, String[] params) {
        MapleCharacter player = client.getPlayer();
        if (params.length != 1) {
            player.yellowMessage("Syntax: !server <open/close>");
        } else {
            switch (params[0]) {
                case "open" -> {
                    ServerConstants.GM_SERVER = false;
                    player.message("You have opened the server to the players!");
                }
                case "close" -> {
                    ServerConstants.GM_SERVER = true;
                    player.message("You have blocked all non-staff connections.");
                }
            }
        }
    }
}
