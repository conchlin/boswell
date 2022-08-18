package server.gachapon;

/**
 *
 * @author SharpAceX(Alan)
 */
public class NautilusHarbor extends GachTiers {

    @Override
    public int[] getCommonItems() {
        return new int[]{
            /* Pirate Equips */
            1002634, 1052095, 1002622, 1052119, 1072303,
            1082198, 1482007, 1492008, 1002631, 1052116,
            1072306, 1082201, 1482008, 1492007, 1002625,
            1002628, 1052107, 1052110, 1052113, 1052119,
            1072294, 1072297, 1072300, 1082189, 1082192,
            1082195, 1482004, 1482005, 1482006, 1492004,
            1492005, 1492006,
            /* Bullets */ 2330005, 2330004
        };
    }

    @Override
    public int[] getUncommonItems() {
        return new int[]{
            /* Dark Scrolls */2044903, 2044904, 2044803, 2044804, 1482012, 1492012
        };
    }

    @Override
    public int[] getRareItems() {
        return new int[]{};
    }

}
