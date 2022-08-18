package client.command.commands.admin;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import server.daily.MapleDaily;
import server.daily.MapleDailyProgress;

public class ClearDailyCommand extends Command {
    {
        setDescription("only use this if a server restart has caused dailies to not be auto reset at 0:00 UTC");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleDaily.resetDaily();
        MapleDailyProgress.resetDailyProgress();
        player.dropMessage(5, "Daily Challenges have been cleared");
    }
}
