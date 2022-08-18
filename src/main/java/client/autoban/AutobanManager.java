/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package client.autoban;

import client.MapleCharacter;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import net.server.Server;
import tools.FilePrinter;

/**
 *
 * @author kevintjuh93
 */
public class AutobanManager {
    private WeakReference<MapleCharacter> chr;
    private Map<AutobanFactory, Integer> points = new HashMap<>();
    private Map<AutobanFactory, Long> lastTime = new HashMap<>();
    private int misses = 0;
    private int lastmisses = 0;
    private int samemisscount = 0;
    private long spam[] = new long[20];
    private long timestamp[] = new long[20];
    private byte timestampcounter[] = new byte[20];

    public AutobanManager(MapleCharacter nchr) {
        this.chr = new WeakReference<MapleCharacter>(nchr);
    }

    public void addPoint(AutobanFactory fac, String reason) {
        if (getCharacter().isGM() || getCharacter().isBanned()) {
            return;
        }
        if (lastTime.containsKey(fac)) {
            if (lastTime.get(fac) < (Server.getInstance().getCurrentTime() - fac.getExpire())) {
                points.put(fac, points.get(fac) / 2); //So the points are not completely gone.
            }
        }
        if (fac.getExpire() != -1) {
            lastTime.put(fac, Server.getInstance().getCurrentTime());
        }
        if (points.containsKey(fac)) {
            points.put(fac, points.get(fac) + 1);
        } else {
            points.put(fac, 1);
        }
        if (points.get(fac) >= fac.getMaximum()) {
            getCharacter().autoban(reason, fac);
            //chr.autoban("Autobanned for " + fac.name() + " ;" + reason, 1);
            //chr.sendPolice("You have been blocked by #bMooplePolice for the HACK reason#k.");
        }
        // Lets log every single point too.
        FilePrinter.print(FilePrinter.AUTOBAN_WARNING, MapleCharacter.makeMapleReadable(getCharacter().getName()) + " caused " + fac.name() + " " + reason);
    }

    public void addMiss() {
        this.misses++;
    }

    public void resetMisses() {
        if (lastmisses == misses && misses > 6) {
            samemisscount++;
        }
        if (samemisscount > 4)
            getCharacter().sendPolice("You will be disconnected for miss godmode.");
            //chr.autoban("Autobanned for : " + misses + " Miss godmode", 1);
        else if (samemisscount > 0) {
            this.lastmisses = misses;
        }
        this.misses = 0;
    }
    
    //Don't use the same type for more than 1 thing
    public void spam(int type) {
        this.spam[type] = Server.getInstance().getCurrentTime();
    }
    
    public void spam(int type, int timestamp) {
        this.spam[type] = timestamp;
    }

    public long getLastSpam(int type) {
        return spam[type];
    }

    /**
     * Timestamp checker
     *
     *  <code>type</code>:<br>
     * 1: Pet Food<br>
     * 2: InventoryMerge<br>
     * 3: InventorySort<br>
     * 4: SpecialMove<br>
     * 5: UseCatchItem<br>
     * 6: Item Drop<br>
     * 7: Chat<br>
     * 8: HealOverTimeHP<br>
     * 9: HealOverTimeMP<br>
     *
     * @param type type
     * @return Timestamp checker
     */
    public void setTimestamp(int type, int time, int times) {
        if (this.timestamp[type] == time) {  
            this.timestampcounter[type]++;
            if (this.timestampcounter[type] >= times) {
                FilePrinter.print(FilePrinter.EXPLOITS, "Player " + chr + " was caught spamming TYPE " + type + " and has been disconnected.");
            }
            return;
        }
        this.timestamp[type] = time;
    }
    
    private MapleCharacter getCharacter() {
    	return chr.get();
    }

    public long getTimestamp(int type) {
        return this.timestamp[type];
    }
}
