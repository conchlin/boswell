package client.command.commands.player;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import server.MaplePortal;
import server.maps.MapleMap;

public class BootCommand extends Command {
    {
        setDescription("<player name> (costs 15000 mesos)");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        if (params.length < 1) {
            player.yellowMessage("Syntax: !jail <playername>");
            return;
        }

        if (player.getMapId() >= 910000000 || player.getMapId() <= 910000022) {
            player.yellowMessage("Cannot be used in the Free Market");
            return;
        }

        if (player.isCheater()) {
            player.yellowMessage("Don't boot your own people!");
            return;
        }

        MapleCharacter victim = c.getWorldServer().getPlayerStorage().getCharacterByName(params[0]);
        if (!victim.isCheater()) {
            player.yellowMessage(victim.getName() + " is not a cheater!");
            return;
        }

        if (victim.getMapId() == 300000012) {
            player.yellowMessage(victim.getName() + " is already in jail.");
            return;
        }

        if (victim.getMapId() != player.getMapId()) {
            player.yellowMessage(victim.getName() + " is not in your current map.");
            return;
        }
        int minutesJailed = 10;
        int mapid = 300000012;
        int cost = -15000;
        if (victim != null) {
            player.gainMeso(cost);
            victim.addJailExpirationTime(minutesJailed * 60 * 1000);

            if (victim.getMapId() != mapid) {    // those gone to jail won't be changing map anyway
                MapleMap target = c.getChannelServer().getMapFactory().getMap(mapid);
                MaplePortal targetPortal = target.getPortal(0);
                victim.saveLocationOnWarp();
                victim.changeMap(target, targetPortal);
                player.message(victim.getName() + " was jailed for " + minutesJailed + " minutes.");
            }

        } else {
            player.message("Player '" + params[0] + "' could not be found.");
        }
    }
}
