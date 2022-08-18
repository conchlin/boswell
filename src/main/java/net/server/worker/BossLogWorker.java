package net.server.worker;

import server.expeditions.MapleExpeditionBossLog;

/**
 * @author Ronan
 */
public class BossLogWorker implements Runnable {

    @Override
    public void run() {
        MapleExpeditionBossLog.resetBosslogs();
    }
}