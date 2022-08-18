package constants;
 
/**
 * @brief ScriptableNPCConstants
 * @author GabrielSin <gabrielsin@playellin.net>
 * @date   16/09/2018
 * 
 * Adaptations to use Pair and Set, in order to suit a one-packet marshall,
 * by Ronan
 */

import java.util.HashSet;
import java.util.Set;
import tools.Pair;

public class ScriptableNPCConstants {
    
    public static final Set<Pair<Integer, String>> SCRIPTABLE_NPCS = new HashSet<Pair<Integer, String>>(){{
        add(new Pair<>(9200000, "Cody"));
        add(new Pair<>(9000032, "AgentW"));
        add(new Pair<>(9201088, "Barry"));
        add(new Pair<>(2041027, "Mason The Collector")); // daily challenge
        add(new Pair<>(9110113, "Shururu")); // daily challenge
        add(new Pair<>(1094000, "Bart")); // daily challenge
    }};
    
}
 