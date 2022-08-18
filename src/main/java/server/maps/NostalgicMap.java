package server.maps;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NostalgicMap { 

    private static HashMap<Integer, String> nostalgicRates = new HashMap<>() {{ // <mob, expModifier>
        // string because maps cant do doubles :(
        put(9400640, "0.50"); // jester
        put(9400639, "0.80"); // dead scarecrow
        put(9400638, "0.80"); // rotting skeleton
        put(9420540, "0.80"); // gallo
        put(8141000, "1.10"); // ghost pirate
        put(7160000, "1.10"); // daul ghost pirate
        put(7140000, "1.10"); // spirit viking
        put(8141100, "1.10"); // gigantic spirit viking
        put(8142000, "1.10"); // phantom watch
        put(8143000, "1.10"); // grim phantom watch
        put(7130010, "1.10"); // death teddy
        put(7130300, "1.10"); // master death teddy
        put(8200000, "1.10"); // eye of time
        put(8200001, "1.10"); // memory monk
        put(8200002, "1.10"); // memory monk trainee
        put(8200003, "1.10"); // memory guardian
        put(8200004, "1.10"); // memory guardian chief
        put(8200005, "1.10"); // oblivion monk
        put(8200006, "1.10"); // oblivion monk trainee
        put(8200007, "1.10"); // oblivion guardian
        put(8200008, "1.10"); // chief oblivion guardian
        put(8200009, "1.10"); // Qualm Monk
        put(8200010, "1.10"); // Qualm Monk Trainee
        put(8200011, "1.10"); // Qualm Guardian
        put(8200012, "1.10"); // Chief Qualm Guardian
        put(5110301, "1.12"); // roid
        put(5110302, "1.12"); // neo huroid
        put(3210201, "1.15"); // jrlioner
        put(3210202, "1.15"); // jrgrupin
        put(3210200, "1.15"); // jrcellion
        put(5120001, "1.15"); // cellion
        put(5120002, "1.15"); // lioner
        put(5120003, "1.15"); // grupin
        put(9500110, "1.15"); // star pixie
        put(4230106, "1.15"); // lunar pixie
        put(5120000, "1.15"); // luster pixie
        put(5150001, "1.15"); // skeleton soldier
        put(6230602, "1.15"); // officer skeleton
        put(6130208, "1.15"); // kru
        put(7130104, "1.15"); // captain
        put(5130107, "1.15"); // coolie zombies
        put(8142100, "1.20"); // squid
        put(8141300, "1.20"); // rissell squid
        put(8150100, "1.20"); // shark
        put(8150101, "1.20"); // cold shark
        put(7130020, "1.20"); // goby
        put(8140600, "1.20"); // bonefish
        put(5150000, "1.20"); // mixed golem
        put(5130102, "1.20"); // dark stone golem
    }};


    private static ListMultimap<Integer, Integer> nostalgicMap = ArrayListMultimap.create(); // <map, mob>

    /**
     * Grabs the string value used to modify the exprate
     * Converts the string value to double
     */
    public static double getNostalgicRate(int mob) {
        if (nostalgicRates.containsKey(mob)) {
            return Double.parseDouble(nostalgicRates.get(mob));
        }
        return 1.0;
    }

    /// TODO double check these <k,v>
    public static void populateNostalgicMobList() {
        nostalgicMap.put(682010203, 9400640); // jester
        nostalgicMap.put(551030100, 9420540); // gallo
        nostalgicMap.put(682010202, 9400639); // Scarecrow
        nostalgicMap.put(682010201, 9400638); // rotting skeletons
        nostalgicMap.put(251010400, 6130208); // kru
        nostalgicMap.put(251010401, 6130208);
        nostalgicMap.put(251010402, 6130208);
        nostalgicMap.put(251010403, 6130208);
        nostalgicMap.put(251010401, 7130104); // captain
        nostalgicMap.put(251010402, 7130104);
        nostalgicMap.put(251010403, 7130104);
        nostalgicMap.put(251010410, 7130104);
        nostalgicMap.put(230040000, 7130020); // goby
        nostalgicMap.put(230040000, 8140600); // bone fish
        nostalgicMap.put(220060200, 7140000); // ghost pirates
        nostalgicMap.put(220060201, 7160000); // dual ghost pirates
        nostalgicMap.put(220060301, 8141000); // spirit viking
        nostalgicMap.put(220060301, 8141100); // gigantic spirit viking
        nostalgicMap.put(220070200, 7130010); // death teddy
        nostalgicMap.put(220070201, 7130300); // master death teddy
        nostalgicMap.put(220070300, 8142000); // phantom watch
        nostalgicMap.put(220070301, 8143000); // grim phantom watch
        nostalgicMap.put(261020300, 5110301); // roid
        nostalgicMap.put(261020400, 5110301);
        nostalgicMap.put(261020400, 5110302); // neo huroid
        nostalgicMap.put(261020500, 5110302);
        nostalgicMap.put(230040200, 8142100); // squid
        nostalgicMap.put(230040300, 8142100);
        nostalgicMap.put(230040200, 8141300); // risell squid
        nostalgicMap.put(230040300, 8141300);
        nostalgicMap.put(101030108, 6230602); // officer skeleton
        nostalgicMap.put(101030109, 6230602);
        nostalgicMap.put(101030110, 6230602);
        nostalgicMap.put(101030111, 6230602);
        nostalgicMap.put(101030112, 6230602);
        nostalgicMap.put(101030106, 5150001); // skeleton soldier
        nostalgicMap.put(101030107, 5150001);
        nostalgicMap.put(101030110, 5150001);
        nostalgicMap.put(101030111, 5150001);
        nostalgicMap.put(101030112, 5150001);
        nostalgicMap.put(200010000, 9500110); // star pixie
        nostalgicMap.put(200020000, 9500110);
        nostalgicMap.put(200030000, 9500110);
        nostalgicMap.put(200040000, 9500110);
        nostalgicMap.put(200050000, 9500110);
        nostalgicMap.put(200060000, 9500110);
        nostalgicMap.put(200070000, 9500110);
        nostalgicMap.put(200080000, 9500110);
        nostalgicMap.put(200020000, 4230106); // lunar pixie
        nostalgicMap.put(200030000, 4230106);
        nostalgicMap.put(200040000, 4230106);
        nostalgicMap.put(200050000, 4230106);
        nostalgicMap.put(200070000, 4230106);
        nostalgicMap.put(200080000, 4230106);
        nostalgicMap.put(200070000, 5120000); // luster pixie
        nostalgicMap.put(200080000, 5120000); // luster pixie
        nostalgicMap.put(200010100, 3210200); // jr cellion
        nostalgicMap.put(200010110, 3210200);
        nostalgicMap.put(200010100, 3210201); // jr lioner
        nostalgicMap.put(200010120, 3210201);
        nostalgicMap.put(200010121, 3210201);
        nostalgicMap.put(200010100, 3210202); // jr grupin
        nostalgicMap.put(200010130, 3210202);
        nostalgicMap.put(200010131, 3210202);
        nostalgicMap.put(200010111, 5120001); // cellion
        nostalgicMap.put(200010200, 5120001);
        nostalgicMap.put(200010121, 5120002); // lioner
        nostalgicMap.put(200010200, 5120002);
        nostalgicMap.put(200010131, 5120003); // grupin
        nostalgicMap.put(200010200, 5120003);
        nostalgicMap.put(270010100, 8200000); // eye of time
        nostalgicMap.put(270010200, 8200000);
        nostalgicMap.put(270010300, 8200000);
        nostalgicMap.put(270010400, 8200000);
        nostalgicMap.put(270010500, 8200000);
        nostalgicMap.put(270020100, 8200000);
        nostalgicMap.put(270020200, 8200000);
        nostalgicMap.put(270020300, 8200000);
        nostalgicMap.put(270020400, 8200000);
        nostalgicMap.put(270020500, 8200000);
        nostalgicMap.put(270030100, 8200000);
        nostalgicMap.put(270030200, 8200000);
        nostalgicMap.put(270030300, 8200000);
        nostalgicMap.put(270030400, 8200000);
        nostalgicMap.put(270030500, 8200000);
        nostalgicMap.put(270010100, 8200001); // memory monk
        nostalgicMap.put(270010200, 8200002); // memory monk trainee
        nostalgicMap.put(270010300, 8200003); // memory guardian
        nostalgicMap.put(270010400, 8200003);
        nostalgicMap.put(270010400, 8200004); // chief memory guardian
        nostalgicMap.put(270010500, 8200004);
        nostalgicMap.put(270020100, 8200005); // qualm monk
        nostalgicMap.put(270020200, 8200006); // qualm monk trainee
        nostalgicMap.put(270020300, 8200007); // qualm guardian
        nostalgicMap.put(270020400, 8200007);
        nostalgicMap.put(270020400, 8200008); // chief qualm guardian
        nostalgicMap.put(270020500, 8200008);
        nostalgicMap.put(270030100, 8200009); // oblivion monk
        nostalgicMap.put(270030200, 8200010); // oblivion monk trainee
        nostalgicMap.put(270030300, 8200011); // oblivion guardian
        nostalgicMap.put(270030400, 8200011);
        nostalgicMap.put(270030400, 8200012); // oblivion qualm guardian
        nostalgicMap.put(270030500, 8200012);
        nostalgicMap.put(105040306, 5150000); // mixed golem
        nostalgicMap.put(105040306, 5130102); // dark stone golem
        nostalgicMap.put(211041100, 5130107); // coolie zombie
        nostalgicMap.put(211041200, 5130107); // coolie zombie
        nostalgicMap.put(211041300, 5130107); // coolie zombie
        nostalgicMap.put(211041400, 5130107); // coolie zombie
    }

    /**
     * Returns list of all mob values associated with map key
     */
    public static List getNostalgicMobs(int map) {
        List matches = new ArrayList();
        for (Map.Entry<Integer, Integer> entry : nostalgicMap.entries()) {
            if (entry.getKey().equals(map)) {
                matches.add(entry.getValue());
            }
        }
        return matches;
    }

    /**
     * Check if certain map is part of the nostalgic buff system
     */
    public static boolean isNostalgicMap(int map) {
        if (nostalgicMap.containsKey(map)) {
            return true;
        }
        return false;
    }
}
