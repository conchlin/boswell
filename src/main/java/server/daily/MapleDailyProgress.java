package server.daily;

import client.MapleCharacter;
import server.Statements;
import tools.DatabaseConnection;
import tools.Randomizer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MapleDailyProgress {

    /*
    some challenges need additional progress tracking. those challenges are handled here.
````The dailyProgress table can be read using the following key:

    <charId, challengeString, num>
    The meaning of num depends on the challenge string

    ORE_COLLECT -> id of ore
    SCAVENGER -> id of etc
    PIG_PICK -> # pig
    PIG_BET -> amount of trophies bet
     */

    //list populating
    private static List<Integer> randomEtc = new ArrayList<>();
    private static List<Integer> jewels = List.of(4000, 4001, 4002);


    /**
     * use this to start and populate the random ore and etc needed to be collect for the day
     * not needed for pig race
     * @param chr
     * @param challenge
     */
    public static void startDaily(MapleCharacter chr, String challenge) {
        switch(challenge) {
            case"ORE_COLLECT":
                registerTodaysOre(chr.getId());
                break;
            case "SCAVENGER":
                registerTodaysEtc(chr.getId());
                break;
        }
    }

    protected static void removeProgress(String challenge, int chrId) {
        MapleCharacter.dailyProgress.remove(chrId, challenge);
    }

    // npc
    public static boolean hasProgressEntry(String challenge, int charId) {
        if (MapleCharacter.dailyProgress.contains(charId, challenge)) {
        //if (MapleCharacter.completedDailies.containsEntry(charId, challenge)) { // i think will work
            return true;
        }
        return false;
    }

    public static void resetDailyProgress() {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Truncate.wipe("daily_progress")
                    .execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        MapleCharacter.dailyProgress.clear();
    }

    // to be called once on server start
    public static void populateScavList() { // TODO idk if all 100 of these entries are good to use
        for(int i = 4000000; i <= 4000100; i++) {
            randomEtc.add(i);
        }
        // remove any bad IDs here
    }

    private static void registerTodaysEtc(int charId) {
        int etc = randomEtc.get(Randomizer.rand(0, randomEtc.size()));
        MapleCharacter.dailyProgress.put(charId, "SCAVENGER", etc);
    }

    private static void registerTodaysOre(int charId) {
        int ore = jewels.get(Randomizer.rand(0, jewels.size()));
        MapleCharacter.dailyProgress.put(charId, "ORE_COLLECT", ore);
    }

    /* pig_race related code below */

    /*
    players bet trophies on which pig they think will win the race
    every 24 hours a race happens
    5 pigs in total (20% chance to win)
    max amount if trophies you can bet at one time is
     */

}
