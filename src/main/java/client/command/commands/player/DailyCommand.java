package client.command.commands.player;

import client.MapleClient;
import client.command.Command;

public class DailyCommand extends Command {
    {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        c.getAbstractPlayerInteraction().openNpc(9201051, "dailies");
    }
}
