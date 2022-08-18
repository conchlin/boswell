package client.command.commands.staff;

import client.MapleCharacter;
import client.MapleClient;

import client.command.Command;
import server.MapleItemInformationProvider;
import server.skills.SkillFactory;

/**
 *
 * @author Saffron
 */
public class OptimalBuffCommand extends Command {
     {
        setDescription("");
    }

    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        SkillFactory.getSkill(3121002).getEffect(SkillFactory.getSkill(3121002).getMaxLevel()).applyTo(player, true); // sharp eyes
        SkillFactory.getSkill(5121009).getEffect(SkillFactory.getSkill(5121009).getMaxLevel()).applyTo(player, true); // speed infusion
        MapleItemInformationProvider.getInstance().getItemEffect(2022179).applyTo(c.getPlayer()); // onyx apple
    }
}
