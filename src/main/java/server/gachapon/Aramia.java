package server.gachapon;

public class Aramia extends GachTiers {

    @Override
    public int[] getCommonItems() {
        return new int[]{
            /* those special 100% event scrolls*/
            2044815, 2044512, 2044712,
            2044612, 2043312, 2043117, 2043217, 2043023, 2044417, 2044317,
            2043812, 2044117, 2044217, 2044025,
            /* 60% Scroll versions */
            2044501, 2044601, 2043701, 2043801, 2044801, 2044901, 2043301,
            2044701, 2043001, 2043002,
            /* 10% Scrolls */
            2044502, 2044602, 2043702, 2043802, 2044802, 2044902, 2043302,
            2044702, 2043002, 2044002
        };
    }

    @Override
    public int[] getUncommonItems() {
        return new int[]{
            2070011, // maple throwing stars
            1302033, // red maple flag
            1302065, // blue maple flag
            2000004, // elixir
            2000006, // power elixir
            2100120, // snail summoning bag
            2100126, // mushroom summoning bag
            2101004 // superslime summoning bag
        };
    }

    @Override
    public int[] getRareItems() {
        return new int[]{
            3010025, // under the maple tree chair
            1012098, // maple leaf
            1012101, // maple leaf
            1012102, // maple leaf
            1012103, // maple leaf
            1702778, // summer flower fairy weapon
            2049199 // chaos scroll of goodness
        };
    }
}
