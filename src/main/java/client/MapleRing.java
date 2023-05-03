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
package client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.DatabaseStatements;
import tools.Pair;
import database.DatabaseConnection;

/**
 *
 * @author Danny
 */
public class MapleRing implements Comparable<MapleRing> {
    private int ringId;
    private int ringId2;
    private int partnerId;
    private int itemId;
    private String partnerName;
    private boolean equipped = false;

    public MapleRing(int id, int id2, int partnerId, int itemid, String partnername) {
        this.ringId = id;
        this.ringId2 = id2;
        this.partnerId = partnerId;
        this.itemId = itemid;
        this.partnerName = partnername;
    }

    public static MapleRing loadFromDb(int ringId) {
        MapleRing ret = null;
        try (Connection con = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM rings WHERE id = ?")) { // Get ring details..
                ps.setInt(1, ringId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret = new MapleRing(ringId, rs.getInt("partnerRingId"),
                                rs.getInt("partnerChrId"),
                                rs.getInt("itemid"),
                                rs.getString("partnerName"));
                    }
                }
            }

            return ret;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static void removeRing(final MapleRing ring) {
        if (ring == null) {
            return;
        }

        try (Connection con = DatabaseConnection.getConnection()) {

            try (PreparedStatement ps = con.prepareStatement("DELETE FROM rings WHERE id=?")) {
                ps.setInt(1, ring.getRingId());
                ps.addBatch();

                ps.setInt(1, ring.getPartnerRingId());
                ps.addBatch();

                ps.executeBatch();
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE inventory_equipment SET ringid=-1 WHERE ringid=?")) {
                ps.setInt(1, ring.getRingId());
                ps.addBatch();

                ps.setInt(1, ring.getPartnerRingId());
                ps.addBatch();

                ps.executeBatch();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static Pair<Integer, Integer> createRing(int itemid, final MapleCharacter partner1, final MapleCharacter partner2) {
        if (partner1 == null) {
            return new Pair<>(-3, -3);
        } else if (partner2 == null) {
            return new Pair<>(-2, -2);
        }

        try (Connection con = DatabaseConnection.getConnection()) {
            DatabaseStatements.Insert statement = new DatabaseStatements.Insert("rings");
            statement.add("itemid", itemid);
            statement.add("partnerchrid", partner2.getId());
            statement.add("partnername", partner2.getName());

            int ringid1 = statement.executeUpdate(con);

            statement = new DatabaseStatements.Insert("rings");
            statement.add("itemid", itemid);
            statement.add("partnerringid", ringid1);
            statement.add("partnerchrid", partner1.getId());
            statement.add("partnername", partner1.getName());

            int ringid2 = statement.executeUpdate(con);

            DatabaseStatements.Update update = new DatabaseStatements.Update("rings");
            update.set("partnerringid", ringid2);
            update.cond("id", ringid1);
            update.execute(con);

            return new Pair<>(ringid1, ringid2);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return new Pair<>(-1, -1);
        }
    }

    public int getRingId() {
        return ringId;
    }

    public int getPartnerRingId() {
        return ringId2;
    }

    public int getPartnerChrId() {
        return partnerId;
    }

    public int getItemId() {
        return itemId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public boolean equipped() {
        return equipped;
    }

    public void equip() {
        this.equipped = true;
    }

    public void unequip() {
        this.equipped = false;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MapleRing) {
            if (((MapleRing) o).getRingId() == getRingId()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + this.ringId;
        return hash;
    }

    @Override
    public int compareTo(MapleRing other) {
        if (ringId < other.getRingId()) {
            return -1;
        } else if (ringId == other.getRingId()) {
            return 0;
        }
        return 1;
    }
}
