package client.command.commands.staff;

import client.MapleCharacter;
import client.MapleClient;
import client.command.Command;
import network.packet.field.CField;
import server.TimerManager;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import tools.MaplePacketCreator;

/**
 *
 * @author Saffron
 */

public class DpsTestCommand extends Command {
    {
        setDescription("");
    }
    
    @Override
    public void execute(MapleClient c, String[] params) {
        MapleCharacter player = c.getPlayer();
        MapleMonster mon = MapleLifeFactory.getMonster(8510100);
        int monHp = 100000000;
        
        mon.setHp(monHp);
        //mon.setStartingHp(monHp);
        player.getClient().announce(CField.Packet.onClock(true, 10)); // 10 seconds to prepare to test dmg
        player.getClient().getSession().write(MaplePacketCreator.earnTitleMessage("tracking starts at 0:00"));
        
        TimerManager.getInstance().schedule(() -> {
                player.getClient().announce(CField.Packet.onClock(true, 240)); // 4 minutes to test dmg 
                player.getMap().spawnMonsterOnGroundBelow(mon, player.getPosition());
                
                TimerManager.getInstance().schedule(() -> {
                    int remainingHp = mon.getHp();
                    int totalDmg = monHp - remainingHp;
                    int dpm = totalDmg / 4; // damage per minute
                    
                    System.out.println("Total Damage: " + totalDmg);
                    System.out.println("Damage per Minute: " + dpm);
                    
                    //mon.getMap().damageMonster(player, mon, Integer.MAX_VALUE);
                    mon.setHp(0); // kill at the end of timeslot
                    player.announce(CField.Packet.onDestroyClock());
                }, 240000); // 4 minutes
        }, 10000); // 10 seconds
    }
}
