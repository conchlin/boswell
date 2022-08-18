/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.gachapon;

/**
 *
 * @author Reed
 */
public class EventGach extends GachTiers {
    
    public static final int eventNPC = -0; // id of the npc being used set to -0 so it is now confused with an actual npc for now
    
    @Override
    public int[] getCommonItems() {
        return new int [] {};            
    }
    
    @Override
    public int[] getUncommonItems() {
        return new int [] {};
    }
    
    @Override
    public int[] getRareItems() {
        return new int [] {};
    }
}
