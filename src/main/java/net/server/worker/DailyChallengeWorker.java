package net.server.worker;

import server.daily.MapleDaily;
import server.daily.MapleDailyProgress;

public class DailyChallengeWorker implements Runnable {

    @Override
    public void run() {
        //DailyLottery.executeLottery();
        MapleDaily.resetDaily();
        MapleDailyProgress.resetDailyProgress();
    }
}