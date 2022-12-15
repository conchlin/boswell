/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.gachapon;

import tools.Randomizer;

/**
 *
 * @author SharpAceX(Alan) System modified by Reed for MapleAvenue
 *
 */
public class MapleGach {

    private static final MapleGach instance = new MapleGach();

    public enum Gachapon {

        GLOBAL(-1, -1, -1, -1, new Global()), // always added to potential prize list
        HENESYS(9100100, 40, 30, 10, new Henesys()),
        ELLINIA(9100101,40, 30, 10, new Ellinia()),
        PERION(9100102, 40, 30, 10, new Perion()),
        KERNING_CITY(9100103, 40, 30, 10, new KerningCity()),
        SLEEPYWOOD(9100104, 40, 30, 10, new Sleepywood()),
        MUSHROOM_SHRINE(9100105, 40, 30, 10, new MushroomShrine()),
        SHOWA_SPA_MALE(9100106, 40, 30, 10, new ShowaSpaMale()),
        SHOWA_SPA_FEMALE(9100107, 70, 30, 10, new ShowaSpaFemale()),
        NEW_LEAF_CITY(9100109, 40, 30, 10, new NewLeafCity()),
        NAUTILUS_HARBOR(9100117, 40, 30, 10, new NautilusHarbor()),
        SINGAPORE(9100111, 40, 30, 10, new Singapore());
        //EVENT_MAP(EventGach.eventNPC, 40, 30, 10, new EventGach()), // unused
        //ARAMIA(9200000, 40, 25, 15, new Aramia()); // summer event

        private GachTiers gachapon;
        private int npcId;
        private int common;
        private int uncommon;
        private int rare;

        private Gachapon(int npcid, int c, int u, int r, GachTiers gt) {
            npcId = npcid;
            gachapon = gt;
            common = c;
            uncommon = u;
            rare = r;
        }

        /**
         * Used to determine if we concat the global list
         * @param npcId id specified in enum above
         * @return seasonal gach or not
         */
        public boolean isSeasonalEventGach(int npcId) {
            // removed reference of custom NPC Nina and replaced with cody
            // so that it will be more compatible with the base v83 files
            return npcId == 9200000;
        }

        private int getTier() {
            int chance = Randomizer.nextInt(common + uncommon + rare) + 1;
            if (npcId == EventGach.eventNPC) {
                if (chance > 1) {
                    return 1; // constant rate for all event items (uncommon)
                }
            } else if (chance > common + uncommon) {
                return 2; //Rare
            } else if (chance > common) {
                return 1; //Uncommon
            }
            return 0; //Common
        }

        public int[] getItems(int tier) {
            return gachapon.getItems(tier);
        }

        /**
         * Grabs item from gachapon list based on tier
         * We check to see if the gachapon is an event gach
         * if gach is an event gach we do not add the global list
         * @param tier 0, 1, or 2
         * @return length of specific gach list or gach list + global list
         */
        public int getItem(int tier) {
            int[] gacha = getItems(tier);
            int[] global = GLOBAL.getItems(tier);
            if (!isSeasonalEventGach(npcId)) {
                int chance = Randomizer.nextInt(gacha.length + global.length);
                return chance < gacha.length ? gacha[chance] : global[chance - gacha.length];
            } else {
                int chance = Randomizer.nextInt(gacha.length);
                return chance < gacha.length ? gacha[chance] : gacha[chance - gacha.length];
            }
        }

        public static Gachapon getByNpcId(int npcId) {
            for (Gachapon gacha : Gachapon.values()) {
                if (npcId == gacha.npcId) {
                    return gacha;
                }
            }
            return null;
        }
    }

    public static MapleGach getInstance() {
        return instance;
    }

    public MapleGachaponItem process(int npcId) {
        Gachapon gacha = Gachapon.getByNpcId(npcId);
        int tier = gacha.getTier();
        int item = gacha.getItem(tier);
        return new MapleGachaponItem(tier, item);
    }

    public class MapleGachaponItem {

        private int id;
        private int tier;

        public MapleGachaponItem(int t, int i) {
            id = i;
            tier = t;
        }

        public int getTier() {
            return tier;
        }

        public int getId() {
            return id;
        }

    }
}
