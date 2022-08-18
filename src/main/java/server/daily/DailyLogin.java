package server.daily;

import client.MapleCharacter;
import tools.MaplePacketCreator;
import tools.Randomizer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;

public class DailyLogin {

    private static boolean duration;

    protected HashMap<Integer, Integer> loginRewards = new HashMap<>() {{
        // 31 entries for max amount of days in a month
        put(1, 5071000); // megaphone
        put(2, 4008002); // bronze trophy
        put(3, 0); // mesos
        put(4, 5220000); // gach
        put(5, 5072000); // smega
        put(6, 5040000); // telerock
        put(7, 5076000); // item mega
        put(8, 1); // nx
        put(9, 5040001); // viprock
        put(10, 5220000); // gach
        put(11, 0); // mesos
        put(12, 4008002); // bronze
        put(13, 5072000); //
        put(14, 0); // mesos
        put(15, 5040000); // telerock
        put(16, 5072000); // smega
        put(17, 5220000); // gach
        put(18, 5040001); // viprock
        put(19, 1); // nx
        put(20, 5040000); // telerock
        put(21, 5072000); // item mega
        put(22, 0); // mesos
        put(23, 4008001); // bronze
        put(24, 5220000); // gach
        put(25, 5040001); // viprock
        put(26, 5071000); // megaphone
        put(27, 1); // nx
        put(28, 5076000); // item mega
        put(29, 5040000); // telerock
        put(30, 0); // mesos
        put(31, 5220000); // gach
    }};

    public static void onlineDurationCheck(MapleCharacter chr) {
        if (duration) return;
        if (System.currentTimeMillis() - chr.getLoginTime() >= (60000 * 60)) { // 60 minutes
            chr.message("[Daily] You have reached your daily login goal!");
            MapleCharacter.dailyProgress.put(chr.getId(), "LOGIN", 1);
            duration = true;
        }
    }

    private int getTodaysReward() {
        int today = LocalDate.now().getDayOfMonth();
        return loginRewards.get(today);
    }

/*    private int calculateRewardAmount(MapleCharacter chr) {
        int reward = getTodaysReward();
        //int streak = chr.getClient().getLoginStreak();
        int streakMod;

        switch (reward) {
            case 0: // mesos
                //streakMod = Randomizer.rand(streak * 50000, streak * 100000);
                break;
            case 1: // NX
                //streakMod = Randomizer.rand(streak * 1000, streak * 1500);
                break;
            default:
                //streakMod = (streak / 7) + 1;
                break;
        }
        //return streakMod;
    }*/

    public void completeLogin(MapleCharacter chr) {
        if (getTodaysReward() == 0) {
            // perhaps make this scale with char level so it's more useful
            chr.gainMeso(Randomizer.rand(45000, 55000));
        } else if (getTodaysReward() == 1) {
            //    chr.getCashShop().gainCash(1, calculateRewardAmount(chr));
            chr.getCashShop().gainCash(1, Randomizer.rand(2000, 3500));
        } else {
            // add code for login streaks that would replace the 2
            chr.getClient().getAbstractPlayerInteraction().gainItem(getTodaysReward(), (short) 2);
        }
    }
}