package client.command.commands.player;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;

public class RollCommand extends Command {
    {
        setDescription("<number> defaults to 100");
    }

    private int randomIntGenerator(int min, int max) {
        return (int) Math.floor(Math.random() * (max - min + 1) + min);
    }

    @Override
    public void execute(MapleClient client, String[] params) {

        int min = 1;
        int max = 100;

        //Set custom max from param
        if (params.length > 0) {
            max = Integer.parseInt(params[0]);
        }

        if (client.tryacquireClient()) {
            try {
                MapleCharacter chr = client.getPlayer();
                /*TODO
                 *  - Limit the rolls to prevent spam
                 * */

                int playerRoll = randomIntGenerator(min, max);
                chr.getMap().dropMessage(6, chr.getName() + " rolls " + playerRoll + " (1-" + max + ")");
                chr.getClient().announceHint("You rolled " + playerRoll, 100);
            } finally {
                client.releaseClient();
            }
        }
    }
}