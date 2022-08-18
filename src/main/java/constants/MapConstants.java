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
        switch (mapid) {
            case 240040700: // - Leafre - Cave of Life - Entrance    //HT Maps
            case 240050000: // - Cave of Life - Cave Entrance
            case 240060200: // - Cave of Life - Horntail's Cave
            case 280030000: // - Last Mission - Zakum's Altar        //Zakum Maps
            case 211042200: // - El Nath - The Cave of Trial III
            case 211042300: // - El Nath - The Door to Zakum
            case 211042400: // - El Nath - Entrance to Zakum Altar
            case 220080000: // - Ludibrium - Deep Inside the Clocktower    //Papulatis Maps
            case 220080001: // - Ludibrium - Origin of Clocktower
            case 551030200: // - Malaysia - Spooky World                 //Scar & Targ
            case 970010000: // Maple Hill (Aramia Event)
            case 741000200: // fishing lagoon
            case 280020000: // zakum JQ stage 1
            case 280020001: // zakum JQ stage 2
                return true;
            default:
                return false;
        }
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
