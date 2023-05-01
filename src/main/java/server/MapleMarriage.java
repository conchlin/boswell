/*
    This file is part of the HeavenMS MapleStory Server
    Copyleft (L) 2016 - 2018 RonanLana

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package server;

import client.MapleClient;
import client.MapleCharacter;
import client.inventory.Item;
import client.inventory.ItemFactory;
import client.inventory.MapleInventory;
import client.inventory.MapleInventoryType;
import client.inventory.manipulator.MapleInventoryManipulator;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import database.DatabaseConnection;
import tools.Pair;

/**
 *
 * @author Ronan
 */
public class MapleMarriage /*extends EventInstanceManager*/ {
/*
    public MapleMarriage(*//*EventManager em, *//*String name) {
        super();
    }
    
    public boolean giftItemToSpouse(int cid) {
        return this.getIntProperty("wishlistSelection") == 0;
    }

    public List<String> getWishlistItems(boolean groom) {
        String strItems = this.getProperty(groom ? "groomWishlist" : "brideWishlist");
        if (strItems != null) {
            return Arrays.asList(strItems.split("\r\n"));
        }

        return new LinkedList<>();
    }

    public void initializeGiftItems() {
        List<Item> groomGifts = new ArrayList<>();
        this.setObjectProperty("groomGiftlist", groomGifts);

        List<Item> brideGifts = new ArrayList<>();
        this.setObjectProperty("brideGiftlist", brideGifts);
    }

    public List<Item> getGiftItems(MapleClient c, boolean groom) {
        if (c.tryacquireClient()) {
            try {
                List<Item> gifts = getGiftItemsList(groom);
                synchronized (gifts) {
                    return new LinkedList<>(gifts);
                }
            } finally {
                c.releaseClient();
            }
        }
        
        return new LinkedList<>();
    }

    private List<Item> getGiftItemsList(boolean groom) {
        return (List<Item>) this.getObjectProperty(groom ? "groomGiftlist" : "brideGiftlist");
    }

    public Item getGiftItem(MapleClient c, boolean groom, int idx) {
        try {
            return getGiftItems(c, groom).get(idx);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public void addGiftItem(boolean groom, Item item) {
        List<Item> gifts = getGiftItemsList(groom);
        synchronized (gifts) {
            gifts.add(item);
        }
    }

    public void removeGiftItem(boolean groom, Item item) {
        List<Item> gifts = getGiftItemsList(groom);
        synchronized (gifts) {
            gifts.remove(item);
        }
    }
        
    public Boolean isMarriageGroom(MapleCharacter chr) {
        Boolean groom = null;
        try {
            int groomid = this.getIntProperty("groomId"), brideid = this.getIntProperty("brideId");
            if (chr.getId() == groomid) {
                groom = true;
            } else if (chr.getId() == brideid) {
                groom = false;
            }
        } catch (NumberFormatException ignored) {}

        return groom;
    }

    public static boolean claimGiftItems(MapleClient c, MapleCharacter chr) {
        List<Item> gifts = loadGiftItemsFromDb(c, chr.getId());
        if (MapleInventory.checkSpot(chr, gifts)) {
            try (Connection con = DatabaseConnection.getConnection()) {
                ItemFactory.MARRIAGE_GIFTS.saveItems(new LinkedList<>(), chr.getId(), con);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (Item item : gifts) {
                MapleInventoryManipulator.addFromDrop(chr.getClient(), item, false);
            }

            return true;
        }

        return false;
    }
        
    public static List<Item> loadGiftItemsFromDb(MapleClient c, int cid) {
        List<Item> items = new LinkedList<>();
        if (c.tryacquireClient()) {
            try {
                try {
                    for (Pair<Item, MapleInventoryType> it : ItemFactory.MARRIAGE_GIFTS.loadItems(cid, false)) {
                        items.add(it.getLeft());
                    }
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            } finally {
                c.releaseClient();
            }
        }
        
        return items;
    }
        
    public void saveGiftItemsToDb(MapleClient c, boolean groom, int cid) {
        MapleMarriage.saveGiftItemsToDb(c, getGiftItems(c, groom), cid);
    }

    public static void saveGiftItemsToDb(MapleClient c, List<Item> giftItems, int cid) {
        List<Pair<Item, MapleInventoryType>> items = new LinkedList<>();
        for (Item it : giftItems) {
            items.add(new Pair<>(it, it.getInventoryType()));
        }

        if (c.tryacquireClient()) {
            try {
                try (Connection con = DatabaseConnection.getConnection()) {
                    ItemFactory.MARRIAGE_GIFTS.saveItems(items, cid, con);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } finally {
                c.releaseClient();
            }
        }
    }*/
}
