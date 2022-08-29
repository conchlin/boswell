/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    Copyleft (L) 2016 - 2018 RonanLana (HeavenMS)

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
package net.server.channel.handlers;

import client.MapleClient;
import client.MapleCharacter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.AbstractMaplePacketHandler;
import net.server.Server;
import server.MapleItemInformationProvider;
import net.database.DatabaseConnection;
import tools.FilePrinter;
import tools.Pair;
import tools.data.input.SeekableLittleEndianAccessor;

/**
 *
 * @author Penguins (Acrylic)
 * @author Ronan (HeavenMS)
 */
public final class CouponCodeHandler extends AbstractMaplePacketHandler {

    private static List<Pair<Integer, Pair<Integer, Integer>>> getNXCodeItems(MapleCharacter chr, Connection con, int codeid) throws SQLException {
        Map<Integer, Integer> couponItems = new HashMap<>();
        Map<Integer, Integer> couponPoints = new HashMap<>(5);
        try {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode_items WHERE codeid = ?")) {
                ps.setInt(1, codeid);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int type = rs.getInt("type"), quantity = rs.getInt("quantity");

                        if (type < 5) {
                            Integer i = couponPoints.get(type);
                            if (i != null) {
                                couponPoints.put(type, i + quantity);
                            } else {
                                couponPoints.put(type, quantity);
                            }
                        } else {
                            int item = rs.getInt("item");

                            Integer i = couponItems.get(item);
                            if (i != null) {
                                couponItems.put(item, i + quantity);
                            } else {
                                couponItems.put(item, quantity);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<Pair<Integer, Pair<Integer, Integer>>> ret = new LinkedList<>();
        if (!couponItems.isEmpty()) {
            for (Entry<Integer, Integer> e : couponItems.entrySet()) {
                int item = e.getKey(), qty = e.getValue();

                if (MapleItemInformationProvider.getInstance().getName(item) == null) {
                    item = 4000000;
                    qty = 1;

                    FilePrinter.printError(FilePrinter.UNHANDLED_EVENT, "Error trying to redeem itemid " + item + " from codeid " + codeid + ".");
                }

                if (!chr.canHold(item, qty)) {
                    return null;
                }

                ret.add(new Pair<>(5, new Pair<>(item, qty)));
            }
        }

        if (!couponPoints.isEmpty()) {
            for (Entry<Integer, Integer> e : couponPoints.entrySet()) {
                ret.add(new Pair<>(e.getKey(), new Pair<>(777, e.getValue())));
            }
        }

        return ret;
    }

    private static Pair<Integer, List<Pair<Integer, Pair<Integer, Integer>>>> getNXCodeResult(MapleCharacter chr, String code) {
        MapleClient c = chr.getClient();
        int codeid = 0;
        List<Pair<Integer, Pair<Integer, Integer>>> ret = new LinkedList<>();
        if (!c.attemptCsCoupon()) {
            return new Pair<>(-5, null);
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM nxcode WHERE code = ?")) {
                ps.setString(1, code);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return new Pair<>(-1, null);
                    }

                    if (rs.getString("retriever") != null) {
                        return new Pair<>(-2, null);
                    }

                    if (rs.getLong("expiration") < Server.getInstance().getCurrentTime()) {
                        return new Pair<>(-3, null);
                    }

                    codeid = rs.getInt("id");
                }
            }

            ret = getNXCodeItems(chr, con, codeid);
            if (ret == null) {
                return new Pair<>(-4, null);
            }

            try (PreparedStatement ps = con.prepareStatement("UPDATE nxcode SET retriever = ? WHERE code = ?")) {
                ps.setString(1, chr.getName());
                ps.setString(2, code);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        c.resetCsCoupon();
        return new Pair<>(0, ret);
    }
    
    private static int parseCouponResult(int res) {
        return switch (res) {
            case -1 -> 0xB0;
            case -2 -> 0xB3;
            case -3 -> 0xB2;
            case -4 -> 0xBB;
            default -> 0xB1;
        };
    }
    
    @Override
    public final void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        /*
        slea.skip(2);
        String code = slea.readMapleAsciiString();
        
        if (c.tryacquireClient()) {
            try {
                Pair<Integer, List<Pair<Integer, Pair<Integer, Integer>>>> codeRes = getNXCodeResult(c.getPlayer(), code.toUpperCase());
                int type = codeRes.getLeft();
                if (type < 0) {
                    c.announce(MaplePacketCreator.showCashShopMessage((byte) parseCouponResult(type)));
                } else {
                    List<Item> couponPackage = new LinkedList<>();

                    for (Pair<Integer, Pair<Integer, Integer>> p : codeRes.getRight()) {
                        type = p.getLeft();
                        int quantity = p.getRight().getRight();

                        CashShop cs = c.getPlayer().getCashShop();
                        switch (type) {
                            case 0:
                            case 4:
                                cs.gainCash(1, quantity);    //nxCredit
                                break;
                            case 1:
                                cs.gainCash(2, quantity);    //maplePoint
                                break;
                            case 2:
                                cs.gainCash(4, quantity);    //nxPrepaid
                                break;
                            case 3:
                                cs.gainCash(1, quantity);
                                cs.gainCash(4, (quantity / 5000));
                                break;

                            default:
                                int item = p.getRight().getLeft();
                                
                                short qty;
                                if (quantity > Short.MAX_VALUE) {
                                    qty = Short.MAX_VALUE;
                                } else if (quantity < Short.MIN_VALUE) {
                                    qty = Short.MIN_VALUE;
                                } else {
                                    qty = (short) quantity;
                                }
                                
                                if (MapleItemInformationProvider.getInstance().isCash(item)) {
                                    Item it = CashShop.generateCouponItem(item, qty);

                                    cs.addToInventory(it);
                                    couponPackage.add(it);
                                } else {
                                    MapleInventoryManipulator.addById(c, item, qty, "", -1);
                                }

                                //c.announce(MaplePacketCreator.showCouponRedeemedItem(item));
                                break;
                        }
                    }

                    if (!couponPackage.isEmpty()) {
                        c.announce(MaplePacketCreator.showBoughtCashPackage(couponPackage, c.getAccID()));
                    } else {
                        c.announce(MaplePacketCreator.showBoughtQuestItem(0));
                    }

                    c.enableCSActions();
                }
            } finally {
                c.releaseClient();
            }
        }*/
    }
}
