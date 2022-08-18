package client.command.commands.player;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;

import static constants.ServerConstants.ENABLE_PQ_TOUR_FEATURE;
import static server.daily.PQTour.getTourPQ;

public class PqTourCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient client, String[] params) {

        if (ENABLE_PQ_TOUR_FEATURE) {
            String message = "The PQTour this week is: " + getTourPQ();
            if (client.tryacquireClient()) {
                try {
                    MapleCharacter chr = client.getPlayer();
                    chr.getClient().announceHint(message, 100);
                    chr.dropMessage(6, message);
                } finally {
                    client.releaseClient();
                }
            }
        }
    }
}
