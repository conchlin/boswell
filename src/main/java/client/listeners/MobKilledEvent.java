package client.listeners;

import java.util.EventObject;

import client.MapleCharacter;
import server.life.MapleMonster;

public class MobKilledEvent extends EventObject {

    /**
     * Rien devs
     */
    private static final long serialVersionUID = -5187422275722511386L;

    private MapleMonster monster;

    private MapleCharacter killer;

    public MobKilledEvent(Object arg, MapleMonster monster, MapleCharacter killer) {
        super(arg);
        this.monster = monster;
        this.killer = killer;
    }

    public MapleMonster getMonster() {
        return monster;
    }

    public MapleCharacter getKiller() {
        return killer;
    }

}
