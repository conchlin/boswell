package server.partyquest;

/**
 *
 * @author Haku
 */

import java.util.Calendar;

import static constants.ServerConstants.ENABLE_PQ_TOUR_FEATURE;
import static constants.PartyQuestConstants.*;

public class PQTour {

    static String[] pqList = {KERNING_CITY_PQ, LUDIBIRUM_PQ, LUDI_MAZE_PQ, HENESYS_PQ};
    static Calendar d = Calendar.getInstance();

    private static String generateTourPQ() {
        String tourPQ = pqList[0]; // Defaults to first value

        // weekly basis
        if (ENABLE_PQ_TOUR_FEATURE) {
            switch (d.get(Calendar.DAY_OF_MONTH)) {
                case 1, 2, 3, 4, 5, 6, 7 -> tourPQ = pqList[0];
                case 8, 9, 10, 11, 12, 13, 14 -> tourPQ = pqList[1];
                case 15, 16, 17, 18, 19, 20, 21 -> tourPQ = pqList[2];
                case 22, 23, 24, 25, 26, 27, 28, 30, 31 -> tourPQ = pqList[3];
            }
        }
        return tourPQ;
    }

    private static boolean willItemDrop(double percent) {
        double roll100 = Math.floor(Math.random() * (101)); //Generate random number
        if(roll100 <= percent) {
            return true;
        }
        return false;
    }

    private static int randomlyPickIndexFromArray(int [] array) {
        return (int) Math.floor(Math.random() * (array.length));
    }

    public static int handleRewards(String pqName) {
        int rewardIds[] = {2049100, 2340000};
        int trashIds[] = {4000377, 4000378}; //For the lols
        int randomIntRewards = randomlyPickIndexFromArray(rewardIds); //Generate random number to randomize array value
        int randomIntTrash = randomlyPickIndexFromArray(trashIds); //Generate random number to randomize array value

        int reward = switch (pqName) {
            case KERNING_CITY_PQ -> willItemDrop(KPQ_DROP_CHANCE) ? rewardIds[randomIntRewards] : trashIds[randomIntTrash];
            case HENESYS_PQ -> willItemDrop(HPQ_DROP_CHANCE) ? rewardIds[randomIntRewards] : trashIds[randomIntTrash];
            case LUDI_MAZE_PQ -> willItemDrop(LMPQ_DROP_CHANCE) ? rewardIds[randomIntRewards] : trashIds[randomIntTrash];
            case LUDIBIRUM_PQ -> willItemDrop(LPQ_DROP_CHANCE) ? rewardIds[randomIntRewards] : trashIds[randomIntTrash];
            default -> trashIds[randomIntTrash];
        };
        return reward;
    }

    public static String getTourPQ() {
        return generateTourPQ();
    }

}
