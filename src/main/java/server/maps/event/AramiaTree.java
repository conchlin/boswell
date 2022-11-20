package server.maps.event;

import client.MapleCharacter;
import client.MapleClient;
import client.inventory.Item;
import net.server.Server;
import network.packet.ReactorPool;
import server.TimerManager;
import server.maps.MapleMap;
import server.maps.MapleReactor;
import tools.MaplePacketCreator;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author Saffron
 * @NPC 9000055 (Aramia)
 * @Map 970010000
 * @Reactor 9702000
 * 
 * Part of the Summer 2020 event
 */

public class AramiaTree {
    private static final AramiaTree instance = new AramiaTree();
    private static Integer aramiaTree = 0; // 5000 max
    public static int count = 0;
    public static int interval = 0;
    public Map<Integer, Integer> sunshineEntry = new HashMap<>(); // track the amount players contribute
    public static Map<Integer, Integer> contributor = new HashMap<>();

    public static final AramiaTree getInstance() {
        return instance;
    }

    public static Integer getTreeSize() { return aramiaTree; }

    public static int getCount() {
        return count;
    }

    public void addCount(int c) {
        count += c;
    }

    public static int getInterval() {
        return interval;
    }

    public void addInterval(int i) {
        interval += i;
    }

    /**
     * When a player gives sunshines to the Maple Tree we add to the community pool.
     * We also check the tree fullness so see if we need to update the reactor.
     * Every 2000 leaves we +1 to the reactor's state which then resets at 10000
     * leaves. Upon resetting it drops golden leaves based on the amount of players
     * on the map.
     *
     * @param c mapleclient instance
     * @param sunshines etc (id: 4001165) 
     */
    public void addSunshines(MapleClient c, int sunshines) {
        final MapleMap map = c.getChannelServer().getMapFactory().getMap(970010000);
        final MapleReactor reactor = map.getReactorByName("mapleTree");

        aramiaTree += sunshines;
        addSunshineEntry(c, sunshines); // track the amount they add
        System.out.println(sunshineEntry);
        announceProgress(c);

        switch (reactor.getState()) {
            case 0, 1, 2, 3, 4, 5 -> {
                if (aramiaTree >= 1000 * (1 + reactor.getState())) {
                    reactor.setState((byte) (reactor.getState() + 1));
                    map.broadcastMessage(ReactorPool.Packet.onReactorChangeState(reactor, reactor.getState()));
                }
            }
            default -> {}
        }

        if (aramiaTree >= 5000) { // time for the tree to explode
            Item drop;
            int posX = reactor.getPosition().x;
            int posY = reactor.getPosition().y;
            final Point dropPos = new Point(posX, posY);
            byte p = 1;

            map.resetReactors();
            for (MapleCharacter chr : c.getPlayer().getMap().getCharacters()) { // one drop per char in the map
                drop = new Item(4001168, (short) 0, (short) 1);
                dropPos.x = (int) (posX + ((p % 2 == 0) ? (25 * ((p + 1) / 2)) : -(25 * (p / 2))));
                p++;
                reactor.getMap().dropFromReactor(chr, reactor, drop, dropPos, (short)0);
            }

            addContributors(c);
            System.out.println("contributor size is " + contributor.size());
            System.out.println("contributors for this aramia tree instance: " + contributor);

            clear(); // reset tree and sunshineEntry
        }
    }
    
    /**
     * Send a server notice that the tree has reached a specific sunshine amount
     * This is triggered in 20% intervals
     * At max fullness we put a 90 second clock and clear the items at 0
     */
    public void announceProgress(MapleClient c) {
        if (aramiaTree < 999) {
            return;
        } else if (aramiaTree / 1000 > getCount() && getCount() <= 4) { // every +20% fullness
            addCount(1);
            if (getInterval() < getCount()) {
                Server.getInstance().broadcastMessage(
                        c.getWorld(), MaplePacketCreator.serverNotice(
                                6, "[Event] Aramia's Tree has reached " + getCount() * 20 + "%"));
            }

            addInterval(1);
        }

        if (getCount() == 5) { // aramiaTree == 10000
            Server.getInstance().broadcastMessage(
                    c.getWorld(), MaplePacketCreator.serverNotice(
                            6, "[Event] All the Sunshine has turned the leaves to gold and they have fallen to the ground!"));
            for (MapleCharacter victim : c.getPlayer().getMap().getCharacters()) {
                victim.announce(MaplePacketCreator.getClock(90));
            }

            TimerManager.getInstance().schedule(() -> {
                c.getPlayer().getMap().clearDrops();
                // clear all drops in the map after 90 seconds so people cant come back in and loot multiple
            }, 90000);
        }
    }

    /**
     * If an entry for that player already exists we add sunshines to id key.
     * If doesnt exist we create a new entry
     * @param c
     * @param amount sunshines being added
     */
    public void addSunshineEntry(MapleClient c, int amount) {
        if (sunshineEntry.containsKey(c.getPlayer().getId())) {
            increment(sunshineEntry, c.getPlayer().getId(), amount);
        } else {
            sunshineEntry.put(c.getPlayer().getId(), amount);
        }
    }

    /**
     * Check for value keys greater than 500 in the sunshineEntry hashmap
     * We then add those to the contributor list
     * @param c
     */
    public void addContributors(MapleClient c) {
        sunshineEntry.forEach((key, value) -> {
            if (value >= 500) {
                contributor.put(key, value);
            }
        });
    }

    /**
     * does the player have an entry in the contributor list?
     * @param chr
     * @return
     */
    public static boolean isContributor(MapleCharacter chr) {
        if (contributor.containsKey(chr.getId())) {
            return true;
        }
        return false;
    }

    /**
     * Add most recent sunshine contribution to total amount given in their mapped entry
     * @param map sunshineEntry
     * @param key player id
     * @param increase sunshines added
     */
    public static<K> void increment(Map<K,Integer> map, K key, int increase) {
        map.merge(key, increase, (a,b) -> a + b);
    }

    public void removeSunshineEntry(MapleCharacter chr) {
        sunshineEntry.remove(chr.getId());
    }

    public static void removeContributor(MapleCharacter chr) {
        contributor.remove(chr.getId());
    }

    /**
     * Reset the tree
     * Rather than setting everything to zero we account for sunshine overflow
     */
    public void clear() {
        aramiaTree -= 5000;
        count = 0;
        interval = 0;

        sunshineEntry.clear();
    }
}
