/*
 This file is part of the OdinMS Maple Story Server
 Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
 Matthias Butz <matze@odinms.de>
 Jan Christian Meyer <vimes@odinms.de>

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License version 3
 as published by the Free Software Foundation. You may not use, modify
 or distribute this program under any other version of the
 GNU Affero General Public License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package client.inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import net.server.audit.locks.MonitoredLockType;
import net.server.audit.locks.factory.MonitoredReentrantLockFactory;
import net.database.DatabaseConnection;
import tools.Pair;

/**
 *
 * @author Flav
 */
public enum ItemFactory {

    INVENTORY(1, false),
    STORAGE(2, true),
    CASH_EXPLORER(3, true),
    CASH_CYGNUS(4, true),
    CASH_ARAN(5, true),
    MERCHANT(6, false),
    CASH_OVERALL(7, true),
    MARRIAGE_GIFTS(8, false),
    DUEY(9, false);

    private final int value;
    private final boolean account;

    private static final int lockCount = 400;
    private static final Lock locks[] = new Lock[lockCount];  // thanks Masterrulax for pointing out a bottleneck issue here


    static {
        for (int i = 0; i < lockCount; i++) {
            locks[i] = MonitoredReentrantLockFactory.createLock(MonitoredLockType.ITEM, true);
        }
    }

    private ItemFactory(int value, boolean account) {
        this.value = value;
        this.account = account;
    }

    public int getValue() {
        return value;
    }

    public List<Pair<Item, MapleInventoryType>> loadItems(int id, boolean login) throws SQLException {
        if(value != 6) return loadItemsCommon(id, login);
        else return loadItemsMerchant(id, login);
    }

    public void saveItems(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        saveItems(items, null, id, con);
    }

    public synchronized void saveItems(List<Pair<Item, MapleInventoryType>> items, List<Short> bundlesList, int id, Connection con) throws SQLException {
        if(value != 6) saveItemsCommon(items, id, con);
        else saveItemsMerchant(items, bundlesList, id, con);
    }

    private static Equip loadEquipFromResultSet(ResultSet rs) throws SQLException {
        Equip equip = new Equip(rs.getInt("itemid"), (short) rs.getInt("position"));
        equip.setOwner(rs.getString("owner"));
        equip.setQuantity((short) rs.getInt("quantity"));
        equip.setAcc((short) rs.getInt("acc"));
        equip.setAvoid((short) rs.getInt("avoid"));
        equip.setDex((short) rs.getInt("dex"));
        equip.setHands((short) rs.getInt("hands"));
        equip.setHp((short) rs.getInt("hp"));
        equip.setInt((short) rs.getInt("int"));
        equip.setJump((short) rs.getInt("jump"));
        equip.setVicious((short) rs.getInt("vicious"));
        equip.setFlag((byte) rs.getInt("flag"));
        equip.setLuk((short) rs.getInt("luk"));
        equip.setMatk((short) rs.getInt("matk"));
        equip.setMdef((short) rs.getInt("mdef"));
        equip.setMp((short) rs.getInt("mp"));
        equip.setSpeed((short) rs.getInt("speed"));
        equip.setStr((short) rs.getInt("str"));
        equip.setWatk((short) rs.getInt("watk"));
        equip.setWdef((short) rs.getInt("wdef"));
        equip.setUpgradeSlots((byte) rs.getInt("upgradeslots"));
        equip.setLevel((byte) rs.getByte("level"));
        equip.setItemExp(rs.getInt("itemexp"));
        equip.setItemLevel(rs.getByte("itemlevel"));
        equip.setExpiration(rs.getLong("expiration"));
        equip.setGiftFrom(rs.getString("giftFrom"));
        equip.setRingId(rs.getInt("ringid"));

        return equip;
    }

    public static List<Pair<Item, Integer>> loadEquippedItems(int id, boolean isAccount, boolean login) throws SQLException {
        List<Pair<Item, Integer>> items = new ArrayList<>();

        StringBuilder query = new StringBuilder();
        query.append("SELECT * FROM ");
        query.append("(SELECT id, accountid FROM characters) AS accountterm ");
        query.append("RIGHT JOIN ");
        query.append("(SELECT * FROM (inventory_items LEFT JOIN inventory_equipment USING(inventoryitemid))) AS equipterm");
        query.append(" ON accountterm.id=equipterm.characterid ");
        query.append("WHERE accountterm.");
        query.append(isAccount ? "accountid" : "characterid");
        query.append(" = ?");
        query.append(login ? " AND inventorytype = " + MapleInventoryType.EQUIPPED.getType() : "");

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Integer cid = rs.getInt("characterid");
                        items.add(new Pair<>(loadEquipFromResultSet(rs), cid));
                    }
                }
            }
        }

        return items;
    }

    private List<Pair<Item, MapleInventoryType>> loadItemsCommon(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM inventory_items LEFT JOIN inventory_equipment USING(inventoryitemid) WHERE type = ? AND ");
            query.append(account ? "accountid" : "characterid").append(" = ?");

            if (login) {
                query.append(" AND inventorytype = ").append(MapleInventoryType.EQUIPPED.getType());
            }

            try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                ps.setInt(1, value);
                ps.setInt(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                        if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                            items.add(new Pair<Item, MapleInventoryType>(loadEquipFromResultSet(rs), mit));
                        } else {
                            Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) rs.getInt("quantity"), rs.getInt("petid"));
                            item.setOwner(rs.getString("owner"));
                            item.setExpiration(rs.getLong("expiration"));
                            item.setGiftFrom(rs.getString("giftFrom"));
                            item.setFlag(rs.getInt("flag"));
                            items.add(new Pair<>(item, mit));
                        }
                    }
                }
            }
        }

        return items;
    }

    private void saveItemsCommon(List<Pair<Item, MapleInventoryType>> items, int id, Connection con) throws SQLException {
        Lock lock = locks[id % 200];
        lock.lock();
        try {
            String query = "DELETE FROM inventory_equipment where inventoryitemid in (select inventoryitemid from inventory_items where type = ? and " +
                    (account ? "accountid" : "characterid") + " = ?)";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, value);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            query = "DELETE FROM inventory_items WHERE type = ? AND " +
                    (account ? "accountid" : "characterid") + " = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, value);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement("INSERT INTO inventory_items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                if (!items.isEmpty()) {
                    for (Pair<Item, MapleInventoryType> pair : items) {
                        Item item = pair.getLeft();
                        MapleInventoryType mit = pair.getRight();
                        ps.setInt(1, value);
                        ps.setInt(2, account ? -1 : id);
                        ps.setInt(3, account ? id : -1);
                        ps.setInt(4, item.getItemId());
                        ps.setInt(5, mit.getType());
                        ps.setInt(6, item.getPosition());
                        ps.setInt(7, item.getQuantity());
                        ps.setString(8, item.getOwner());
                        ps.setInt(9, item.getPetId());
                        ps.setInt(10, item.getFlag());
                        ps.setLong(11, item.getExpiration());
                        ps.setString(12, item.getGiftFrom());
                        ps.executeUpdate();

                        try (PreparedStatement pse = con.prepareStatement("INSERT INTO inventory_equipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                            if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                                try (ResultSet rs = ps.getGeneratedKeys()) {
                                    if (!rs.next()) {
                                        throw new RuntimeException("Inserting item failed.");
                                    }

                                    pse.setLong(1, rs.getLong(1));
                                }

                                Equip equip = (Equip) item;
                                pse.setInt(2, equip.getUpgradeSlots());
                                pse.setInt(3, equip.getLevel());
                                pse.setInt(4, equip.getStr());
                                pse.setInt(5, equip.getDex());
                                pse.setInt(6, equip.getInt());
                                pse.setInt(7, equip.getLuk());
                                pse.setInt(8, equip.getHp());
                                pse.setInt(9, equip.getMp());
                                pse.setInt(10, equip.getWatk());
                                pse.setInt(11, equip.getMatk());
                                pse.setInt(12, equip.getWdef());
                                pse.setInt(13, equip.getMdef());
                                pse.setInt(14, equip.getAcc());
                                pse.setInt(15, equip.getAvoid());
                                pse.setInt(16, equip.getHands());
                                pse.setInt(17, equip.getSpeed());
                                pse.setInt(18, equip.getJump());
                                pse.setInt(19, 0);
                                pse.setInt(20, equip.getVicious());
                                pse.setInt(21, equip.getItemLevel());
                                pse.setInt(22, equip.getItemExp());
                                pse.setInt(23, equip.getRingId());
                                pse.executeUpdate();
                            }
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private List<Pair<Item, MapleInventoryType>> loadItemsMerchant(int id, boolean login) throws SQLException {
        List<Pair<Item, MapleInventoryType>> items = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection()) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT * FROM inventory_items LEFT JOIN inventory_equipment USING(inventoryitemid) WHERE type = ? AND ");
            query.append(account ? "accountid" : "characterid").append(" = ?");

            if (login) {
                query.append(" AND inventorytype = ").append(MapleInventoryType.EQUIPPED.getType());
            }

            try (PreparedStatement ps = con.prepareStatement(query.toString())) {
                ps.setInt(1, value);
                ps.setInt(2, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        try (PreparedStatement ps2 = con.prepareStatement("SELECT bundles FROM inventory_merchant WHERE inventoryitemid = ?")) {
                            ps2.setLong(1, rs.getLong("inventoryitemid"));
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                short bundles = 0;
                                if (rs2.next()) {
                                    bundles = rs2.getShort("bundles");
                                }

                                MapleInventoryType mit = MapleInventoryType.getByType(rs.getByte("inventorytype"));

                                if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                                    items.add(new Pair<>(loadEquipFromResultSet(rs), mit));
                                } else {
                                    if (bundles > 0) {
                                        Item item = new Item(rs.getInt("itemid"), (byte) rs.getInt("position"), (short) (bundles * rs.getInt("quantity")), rs.getInt("petid"));
                                        item.setOwner(rs.getString("owner"));
                                        item.setExpiration(rs.getLong("expiration"));
                                        item.setGiftFrom(rs.getString("giftFrom"));
                                        item.setFlag(rs.getInt("flag"));
                                        items.add(new Pair<>(item, mit));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return items;
    }

    private void saveItemsMerchant(List<Pair<Item, MapleInventoryType>> items, List<Short> bundlesList, int id, Connection con) throws SQLException {
        Lock lock = locks[id % 200];
        lock.lock();
        try {
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM inventory_merchant WHERE characterid = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            String query = "DELETE inventoryitems, inventoryequipment FROM inventory_items LEFT JOIN inventory_equipment USING(inventoryitemid) WHERE type = ? AND " +
                    (account ? "accountid" : "characterid") + " = ?";
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, value);
                ps.setInt(2, id);
                ps.executeUpdate();
            }

            int i = 0;
            for (Pair<Item, MapleInventoryType> pair : items) {
                final Item item = pair.getLeft();
                final Short bundles = bundlesList.get(i);
                final MapleInventoryType mit = pair.getRight();
                i++;

                final int genKey;

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO inventory_items VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, value);
                    ps.setString(2, account ? null : String.valueOf(id));
                    ps.setString(3, account ? String.valueOf(id) : null);
                    ps.setInt(4, item.getItemId());
                    ps.setInt(5, mit.getType());
                    ps.setInt(6, item.getPosition());
                    ps.setInt(7, item.getQuantity());
                    ps.setString(8, item.getOwner());
                    ps.setInt(9, item.getPetId());
                    ps.setInt(10, item.getFlag());
                    ps.setLong(11, item.getExpiration());
                    ps.setString(12, item.getGiftFrom());
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) {
                            throw new RuntimeException("Inserting item failed.");
                        }

                        genKey = rs.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement("INSERT INTO inventory_merchant VALUES (DEFAULT, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, genKey);
                    ps.setInt(2, id);
                    ps.setInt(3, bundles);
                    ps.executeUpdate();
                }

                if (mit.equals(MapleInventoryType.EQUIP) || mit.equals(MapleInventoryType.EQUIPPED)) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO inventory_equipment VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                        ps.setInt(1, genKey);

                        Equip equip = (Equip) item;
                        ps.setInt(2, equip.getUpgradeSlots());
                        ps.setInt(3, equip.getLevel());
                        ps.setInt(4, equip.getStr());
                        ps.setInt(5, equip.getDex());
                        ps.setInt(6, equip.getInt());
                        ps.setInt(7, equip.getLuk());
                        ps.setInt(8, equip.getHp());
                        ps.setInt(9, equip.getMp());
                        ps.setInt(10, equip.getWatk());
                        ps.setInt(11, equip.getMatk());
                        ps.setInt(12, equip.getWdef());
                        ps.setInt(13, equip.getMdef());
                        ps.setInt(14, equip.getAcc());
                        ps.setInt(15, equip.getAvoid());
                        ps.setInt(16, equip.getHands());
                        ps.setInt(17, equip.getSpeed());
                        ps.setInt(18, equip.getJump());
                        ps.setInt(19, 0);
                        ps.setInt(20, equip.getVicious());
                        ps.setInt(21, equip.getItemLevel());
                        ps.setInt(22, equip.getItemExp());
                        ps.setInt(23, equip.getRingId());
                        ps.executeUpdate();
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }
}