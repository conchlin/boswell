package server.gachapon;

/**
 *
 * @author SharpAceX(Alan)
 */
public abstract class GachTiers {

    public abstract int[] getCommonItems();

    public abstract int[] getUncommonItems();

    public abstract int[] getRareItems();

    public int[] getItems(int tier) {
        if (tier == 0) {
            return getCommonItems();
        } else if (tier == 1) {
            return getUncommonItems();
        } else if (tier == 2) {
            return getRareItems();
        }
        return null;
    }
}
