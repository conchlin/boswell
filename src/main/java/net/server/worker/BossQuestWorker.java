package net.server.worker;

import server.partyquest.BossQuest;

public class BossQuestWorker implements Runnable {
    @Override
    public void run() {
        BossQuest.resetBPQ();
    }
}
