/*
This file is part of the OdinMS Maple Story Server
Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
Matthias Butz <matze@odinms.de>
Jan Christian Meyer <vimes@odinms.de>

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
package server.cashshop;

import client.inventory.*;
import constants.ItemConstants;
import constants.ServerConstants;
import database.tables.AccountsTbl;
import kotlin.Triple;
import net.server.Server;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import database.DatabaseConnection;
import tools.Pair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import database.DatabaseStatements;

/*
 * @author Flav
 */
public class CashShop {
    
    private int accountId, characterId, nxCredit, maplePoint, nxPrepaid;
    private boolean opened;
    private ItemFactory factory;
    private List<Item> inventory = new ArrayList<>();
    private List<Integer> wishList = new ArrayList<>();
    private int notes = 0;
    private Lock lock = MonitoredReentrantLockFactory.createLock(MonitoredLockType.CASHSHOP);

    public CashShop(int accountId, int characterId, int jobType) throws SQLException {
        this.accountId = accountId;
        this.characterId = characterId;

        if (!ServerConstants.USE_JOINT_CASHSHOP_INVENTORY) {
            switch (jobType) {
                case 0:
                    factory = ItemFactory.CASH_EXPLORER;
                    break;
                case 1:
                    factory = ItemFactory.CASH_CYGNUS;
                    break;
                case 2:
                    factory = ItemFactory.CASH_ARAN;
                    break;
                default:
                    break;
            }
        } else {
            factory = ItemFactory.CASH_OVERALL;
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            Triple<Integer, Integer, Integer> cash = AccountsTbl.loadNXCash(accountId);
            this.nxCredit = cash.getFirst();
            this.maplePoint = cash.getSecond();
            this.nxPrepaid = cash.getThird();


            for (Pair<Item, MapleInventoryType> item : factory.loadItems(accountId, false)) {
                inventory.add(item.getLeft());
            }

            try (PreparedStatement ps = con.prepareStatement("SELECT sn FROM wish_lists WHERE charid = ?")) {
                ps.setInt(1, characterId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        wishList.add(rs.getInt("sn"));
                    }
                }
            }
        }
    }

    public int getCash(int type) {
        return switch (type) {
            case 1 -> nxCredit;
            case 2 -> maplePoint;
            case 4 -> nxPrepaid;
            default -> 0;
        };

    }

    public void gainCash(int type, int cash) {
        switch (type) {
            case 1 -> nxCredit += cash;
            case 2 -> maplePoint += cash;
            case 4 -> nxPrepaid += cash;
        }
    }

    public void gainCash(int type, CashItem buyItem, int world) {
        gainCash(type, -buyItem.getPrice());
        if (!ServerConstants.USE_ENFORCE_ITEM_SUGGESTION)
            Server.getInstance().getWorld(world).addCashItemBought(buyItem.getSN());
    }

    public boolean isOpened() {
        return opened;
    }

    public void open(boolean b) {
        opened = b;
    }

    public List<Item> getInventory() {
        lock.lock();
        try {
            return Collections.unmodifiableList(inventory);
        } finally {
            lock.unlock();
        }
    }

    public Item findByCashId(int cashId) {
        boolean isRing;
        Equip equip = null;
        for (Item item : getInventory()) {
            if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                equip = (Equip) item;
                isRing = equip.getRingId() > -1;
            } else {
                isRing = false;
            }

            if ((item.getPetId() > -1 ? item.getPetId() : isRing ? equip.getRingId() : item.getCashId()) == cashId) {
                return item;
            }
        }

        return null;
    }

    public void addToInventory(Item item) {
        lock.lock();
        try {
            inventory.add(item);
        } finally {
            lock.unlock();
        }
    }

    public void removeFromInventory(Item item) {
        lock.lock();
        try {
            inventory.remove(item);
        } finally {
            lock.unlock();
        }
    }

    public List<Integer> getWishList() {
        return wishList;
    }

    public void clearWishList() {
        wishList.clear();
    }

    public void addToWishList(int sn) {
        wishList.add(sn);
    }

    public void gift(int recipient, String from, String message, int sn) {
        gift(recipient, from, message, sn, -1);
    }

    public void gift(int recipient, String from, String message, int sn, int ringid) {
        try (Connection con = DatabaseConnection.getConnection()) {
            DatabaseStatements.Insert.into("gifts")
                    .add("\"to\"", recipient)
                    .add("\"from\"", from)
                    .add("message", message)
                    .add("sn", sn)
                    .add("ringid", ringid)
                    .executeUpdate(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Pair<Item, String>> loadGifts() {
        List<Pair<Item, String>> gifts = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM gifts WHERE \"to\" = ?")) {
                ps.setInt(1, characterId);
                try (ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        notes++;
                        CashItem cItem = CashItemFactory.getItem(rs.getInt("sn"));
                        Item item = cItem.toItem();
                        Equip equip = null;
                        item.setGiftFrom(rs.getString("from"));
                        if (item.getInventoryType().equals(MapleInventoryType.EQUIP)) {
                            equip = (Equip) item;
                            equip.setRingId(rs.getInt("ringid"));
                            gifts.add(new Pair<>(equip, rs.getString("message")));
                        } else
                            gifts.add(new Pair<>(item, rs.getString("message")));

                        if (CashItemFactory.isPackage(cItem.getItemId())) { //Packages never contains a ring
                            for (Item packageItem : CashItemFactory.getPackage(cItem.getItemId())) {
                                packageItem.setGiftFrom(rs.getString("from"));
                                addToInventory(packageItem);
                            }
                        } else {
                            addToInventory(equip == null ? item : equip);
                        }
                    }
                }
            }

            DatabaseStatements.Delete.from("gifts").where("\"to\"", characterId).execute(con);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return gifts;
    }

    public int getAvailableNotes() {
        return notes;
    }

    public void decreaseNotes() {
        notes--;
    }

    public void save(Connection con) throws SQLException {
        AccountsTbl.updateNXCash(accountId, nxCredit, maplePoint, nxPrepaid);
        List<Pair<Item, MapleInventoryType>> itemsWithType = new ArrayList<>();

        List<Item> inv = getInventory();
        for (Item item : inv) {
            itemsWithType.add(new Pair<>(item, item.getInventoryType()));
        }

        factory.saveItems(itemsWithType, accountId, con);
        DatabaseStatements.Delete.from("wish_lists").where("charid", characterId).execute(con);

        DatabaseStatements.BatchInsert statement = new DatabaseStatements.BatchInsert("wish_lists");
        for (int sn : wishList) {
            statement.add("charid", characterId);
            statement.add("sn", sn);
        }
        statement.execute(con);
    }

    private Item getCashShopItemByItemid(int itemid) {
        lock.lock();
        try {
            for (Item it : inventory) {
                if (it.getItemId() == itemid) {
                    return it;
                }
            }
        } finally {
            lock.unlock();
        }

        return null;
    }

    public synchronized Pair<Item, Item> openCashShopSurprise() {
        Item css = getCashShopItemByItemid(5222000);

        if (css != null) {
            CashItem cItem = CashItemFactory.getRandomCashItem();

            if (cItem != null) {
                if (css.getQuantity() > 1) {
                    /* if(NOT ENOUGH SPACE) { looks like we're not dealing with cash inventory limit whatsoever, k then
                        return null;
                    } */

                    css.setQuantity((short) (css.getQuantity() - 1));
                } else {
                    removeFromInventory(css);
                }

                Item item = cItem.toItem();
                addToInventory(item);

                return new Pair<>(item, css);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Item generateCouponItem(int itemId, short quantity) {
        CashItem it = new CashItem(77777777, itemId, 7777, ItemConstants.isPet(itemId) ? 30 : 0, quantity, quantity, true);
        return it.toItem();
    }
}
