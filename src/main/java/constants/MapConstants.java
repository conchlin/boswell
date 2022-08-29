/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package constants;

/**
 *
 * @author Saffron
 */
public class MapConstants {
    
    public static final int FREE_MARKET = 910000000;
    public static final int FISH_LAGOON = 741000200;
    
    public static boolean isRestrictedFMMap(int mapid) {
        return switch (mapid) {
            // - Leafre - Cave of Life - Entrance
            // HT Maps
            // - Cave of Life - Cave Entrance
            // - Cave of Life - Horntail's Cave
            // - Last Mission - Zakum's Altar
            // Zakum Maps
            // - El Nath - The Cave of Trial III
            // - El Nath - The Door to Zakum
            // - El Nath - Entrance to Zakum Altar
            // - Ludibrium - Deep Inside the Clocktower
            // Papulatis Maps
            // - Ludibrium - Origin of Clocktower
            // - Malaysia - Spooky World
            // Scar & Targ
            // Maple Hill (Aramia Event)
            // fishing lagoon
            // zakum JQ stage 1
            // zakum JQ stage 2
            case 240040700, 240050000, 240060200, 280030000, 211042200, 211042300,
                    211042400, 220080000, 220080001, 551030200, 970010000, 741000200,
                    280020000, 280020001 -> // zakum JQ stage 2
                    true;
            default -> false;
        };
    }

    public static boolean isMapleIsland(int mapid) {
        return mapid < 2000001;
    }
    
    public static boolean isAranIntro(int mapid) {
        return mapid >= 914000200 && mapid <= 914000220;
    }
    
    public static boolean isToTMap(int mapid) {
        // these maps need to be tested might not have all of them
        return mapid >= 270010100 && mapid <= 270060000;
    }
}
