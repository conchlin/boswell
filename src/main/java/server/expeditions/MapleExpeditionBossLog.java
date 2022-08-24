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
package server.expeditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.database.DatabaseConnection;

/**
 *
 * @author Conrad
 * @author Ronan
 */
public class MapleExpeditionBossLog {

    public enum BossLogEntry {
        ZAKUM(25, 1), // set all entries per day to 50 until we fix the system
        HORNTAIL(25, 1),
        PINKBEAN(25, 1),
        //PINKZAKUM(3, 1),
        //VONLEON(2, 1),
        //PAPULATUS(5, 1),
        SCARGA(25, 1);

        private int entries;
        private int timeLength;
        private int minChannel, maxChannel;

        private BossLogEntry(int entries, int timeLength) {
            this(entries, 0, Integer.MAX_VALUE, timeLength);
        }

        private BossLogEntry(int entries, int minChannel, int maxChannel, int timeLength) {
            this.entries = entries;
            this.minChannel = minChannel;
            this.maxChannel = maxChannel;
            this.timeLength = timeLength;
        }

        private static BossLogEntry getBossEntryByName(String name) {
            for (BossLogEntry b : BossLogEntry.values()) {
                if (name.contentEquals(b.name())) {
                    return b;
                }
            }

            return null;
        }

    }

    public static void resetBosslogs() {
        try ( Connection con = DatabaseConnection.getConnection()) {
            PreparedStatement ps;
            ps = con.prepareStatement("TRUNCATE boss_logs"); // delete all entries
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int countPlayerEntries(int cid, BossLogEntry boss) {
        int ret_count = 0;
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps;
            ps = con.prepareStatement("SELECT COUNT(*) FROM boss_logs WHERE characterid = ? AND bosstype = ?");
            ps.setInt(1, cid);
            ps.setObject(2, boss.name(), java.sql.Types.OTHER);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ret_count = rs.getInt(1);
            } else {
                ret_count = -1;
            }
            rs.close();
            ps.close();
            con.close();
            return ret_count;
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private static void insertPlayerEntry(int cid, BossLogEntry boss) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO boss_logs (characterid, bosstype) VALUES (?,?)");
            ps.setInt(1, cid);
            ps.setObject(2, boss.name(), java.sql.Types.OTHER);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean attemptBoss(int cid, int channel, MapleExpedition exped, boolean log) {
        BossLogEntry boss = BossLogEntry.getBossEntryByName(exped.getType().name());
        if (boss == null) {
            return true;
        }

        if (channel < boss.minChannel || channel > boss.maxChannel) {
            return false;
        }

        if (countPlayerEntries(cid, boss) >= boss.entries) {
            return false;
        }

        if (log) {
            insertPlayerEntry(cid, boss);
        }
        return true;
    }
}
