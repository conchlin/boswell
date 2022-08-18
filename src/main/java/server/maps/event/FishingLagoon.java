package server.maps.event;

import client.MapleCharacter;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import constants.ExpTable;
import server.MapleItemInformationProvider;
import server.TimerManager;
import tools.MaplePacketCreator;
import tools.Randomizer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author CelinoSea
 * Modified by Saffron for Avenue
 * */

public class FishingLagoon {

    private static final FishingLagoon instance = new FishingLagoon();

    List<Integer> fishingReward = Arrays.asList(
            0, // Meso
            0,
            1, // EXP
            1,
            2012008, // Unripe Onyx Apple
            2022998, // Rotten Apple
            1302021, // Pico Pico Hammer
            1322023, // Blue Flowery Tube
            1322010, // Square Shovel
            1072043, // Smelly Gomushin
            1302000, // Sword
            1442011, // Surfboard
            4031626, // Golden Fish
            4031627, // White Bait (3cm)
            4031628, // Sailfish (120cm)
            4031630, // Carp (30cm)
            4031631, // Salmon(150cm)
            4031633, // Whitebait (3.6cm)
            4031634, // Whitebait (5cm)
            4031635, // Whitebait (6.5cm)
            4031636, // Whitebait (10cm)
            4031637, // Carp (53cm)
            4031638, // Carp (60cm)
            4031639, // Carp (100cm)
            4031640, // Carp (113cm)
            4031641, // Sailfish (128cm)
            4031642, // Sailfish (131cm)
            4031643, // Sailfish (140cm)
            4031644, // Sailfish (148cm)
            4031645, // Salmon (166cm)
            4031646, // Salmon (183cm)
            4031647, // Salmon (227cm)
            4031648 // Salmon (288cm)
            );

    public static final FishingLagoon getInstance() {
        return instance;
    }

    /**
     * Grab reward integer from fishReward list
     */
    public final int getFishingReward() {
        return fishingReward.get(Randomizer.nextInt(fishingReward.size()));
    }
}
