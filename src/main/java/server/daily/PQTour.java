package server.daily;

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
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    tourPQ = pqList[0];
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case 14:
                    tourPQ = pqList[1];
                    break;
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                    tourPQ = pqList[2];
                    break;
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                case 27:
                case 28:
                case 30:
                case 31:
                    tourPQ = pqList[3];
                    break;
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

        int reward;
        switch(pqName){
            case KERNING_CITY_PQ:
                reward = willItemDrop(KPQ_DROP_CHANCE) ? rewardIds[randomIntRewards]:trashIds[randomIntTrash];
                break;
            case HENESYS_PQ:
                reward = willItemDrop(HPQ_DROP_CHANCE) ? rewardIds[randomIntRewards]:trashIds[randomIntTrash];
                break;
            case LUDI_MAZE_PQ:
                reward = willItemDrop(LMPQ_DROP_CHANCE) ? rewardIds[randomIntRewards]:trashIds[randomIntTrash];
                break;
            case LUDIBIRUM_PQ:
                reward = willItemDrop(LPQ_DROP_CHANCE) ? rewardIds[randomIntRewards]:trashIds[randomIntTrash];
                break;
            default:
                reward = trashIds[randomIntTrash];
                break;
        }
        return reward;
    }

    public static String getTourPQ() {
        return generateTourPQ();
    }

}
