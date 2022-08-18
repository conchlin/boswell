package server.daily;

import client.MapleCharacter;

import server.Statements;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;


/**
 *
 * @author Saffron
 */

public class MapleDaily {

    /*
    each completed daily is to be given an individual "dailies" database entry
    syntax: char_id, daily_string
    this corresponds with the completedDailies list
     */

    /*
    login -> handler

    ore_collect -> mapledailyprogress
    scavenger -> mapledailyprogress
    mob_hunt -> mapledailyprogress
    pig_race -> mapledailyprogress/handler
    bloc_guess -> guess the amount of blocs used in challenge closest x wins

    ocean_explore -> chance
    red_bandit -> chance
    beach -> chance
    trash_bin -> chance
     */

    /**
     * reward handling for all challenges (npc)
     * not inventory-safe provide etc check before using this
     * @param challenge
     * @param chr
     * @param chance these challenges are handled differently since you dont win 100% of the time
     */
    public static void completeDaily(String challenge, MapleCharacter chr, boolean chance) {
        DailyLogin login = new DailyLogin();

        if (chance) { // if a challenge needs custom handling
            switch(challenge) {
                case "LOGIN":
                    login.completeLogin(chr); // reward
                    MapleDailyProgress.removeProgress("LOGIN", chr.getId());
                    break;
                case "ORE_COLLECT":
                case "SCAVENGER":
                    chr.getClient().getAbstractPlayerInteraction().gainItem(4008002, (short) 100); // bronze trophy
                    //removeProgress(challenge, chr.getId());
                    break;
                case "LOTTERY":
                    // insert entry info here
                    // actual execution of the lottery will be through a "server worker" file
            }
        }
        chr.getClient().announceHint("[Daily] You have completed today's " + challenge + " Challenge!", 75);
        chr.saveCharToDB(); // let's make sure we always save upon completion
        MapleCharacter.completedDailies.put(chr.getId(), challenge);
    }

    // npc
    public static boolean hasCompletedEntry(String challenge, int charId) {
        if (MapleCharacter.completedDailies.containsEntry(charId, challenge)) { // i think will work
            return true;
        }
        return false;
    }

    public static void resetDaily() {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Truncate.wipe("daily")
                    .execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        MapleCharacter.completedDailies.clear();
    }
}
