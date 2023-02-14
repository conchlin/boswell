package client.command.commands.player;

import client.MapleClient;
import client.command.Command;

public class TrophyCommand extends Command {
    {
        setDescription("pulls up the trophy exchange NPC window");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        //c.getAbstractPlayerInteraction().openNpc(9200000, "TrophyExchange");
    }
}
