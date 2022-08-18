package server.cashshop;

import client.inventory.Item;
import client.inventory.MapleInventoryType;
import client.inventory.MaplePet;
import constants.ItemConstants;
import net.server.Server;
import server.MapleItemInformationProvider;

public class CashItem {

    private int sn, itemId, price;
    private long period;
    private short count;
    private boolean onSale;
    private short gender;

    public CashItem(int sn, int itemId, int price, long period, short count, short gender, boolean onSale) {
        this.sn = sn;
        this.itemId = itemId;
        this.price = price;
        this.period = period;
        this.count = count;
        this.gender = gender;
        this.onSale = onSale;
    }

    public int getSN() {
        return sn;
    }

    public int getItemId() {
        return itemId;
    }

    public int getPrice() {
        return price;
    }

    public short getCount() {
        return count;
    }

    public boolean isOnSale() {
        return onSale;
    }

    public short getGender() {
        return gender;
    }

    public Item toItem() {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item item;

        int petid = -1;
        if (ItemConstants.isPet(itemId)) {
            petid = MaplePet.createPet(itemId);
            period = ii.getPetLife(itemId);
        }

        if (ItemConstants.getInventoryType(itemId).equals(MapleInventoryType.EQUIP)) {
            item = MapleItemInformationProvider.getInstance().getEquipById(itemId);
        } else {
            item = new Item(itemId, (byte) 0, count, petid);
        }

        if (ItemConstants.EXPIRING_ITEMS) {
            if (period == 1) {
                switch (itemId) {
                    case 5211048, 5360042 ->
                            // 4 Hour 2X coupons, the period is 1, but we don't want them to last a day.
                            item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 4));

                        /*
                        } else if(itemId == 5211047 || itemId == 5360014) { // 3 Hour 2X coupons, unused as of now
                        item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 3));
                         */
                    case 5211060 ->
                            // 2 Hour 3X coupons.
                            item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 2));
                    default -> item.setExpiration(Server.getInstance().getCurrentTime() + (1000 * 60 * 60 * 24));
                }
            } else {
                long time = 1000 * 60 * 60 * 24 * period;
                item.setExpiration(Server.getInstance().getCurrentTime() + (time));
            }
        }

        item.setSN(sn);
        return item;
    }
}
