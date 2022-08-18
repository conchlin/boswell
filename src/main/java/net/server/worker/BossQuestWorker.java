package net.server.worker;

import server.daily.BossQuest;

public class BossQuestWorker implements Runnable {
    @Override
    public void run() {
        BossQuest.resetBPQ();
    }
}
