package server.partyquest;

import client.MapleCharacter;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import server.Statements;
import tools.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BossQuest {

    protected int points;
    protected int attempts; // 3 max for now i guess
    public static final Map<Integer, Integer> bossLimit = new LinkedHashMap<>();
    public static final Map<Integer, Integer> bossPoints = new LinkedHashMap<>();

    public int getAttempts(int id) {
        return bossLimit.get(id);
    }

    public void addAttempts(int id, int atmpts) {
        attempts = atmpts;
        bossLimit.put(id, atmpts);

    }

    public int setAttempts(int a) {
        return attempts = a;
    }

    public void addPoints(int id, int amount) {
        points += amount;
        bossPoints.put(id, points);
    }

    public void spendPoints(int id, int amount) {
        points -= amount;
        bossPoints.put(id, points);
    }

    public int setPoints(int p) {
        return points = p;
    }

    public int getPoints(int id) {
        return bossPoints.get(id);
    }

    public static void saveBossQuest(MapleCharacter player, Connection con) throws SQLException  {
        Statements.Delete.from("boss_quest").where("id", player.getId()).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("bossquest");
        for (Map.Entry<Integer, Integer> entry : bossLimit.entrySet()) {
            statement.add("id", entry.getKey());
            statement.add("attempts", entry.getValue());
        }
        for (Map.Entry<Integer, Integer> entry : bossPoints.entrySet()) {
            // we already have the key in the table
            statement.add("points", entry.getValue());

        }
        statement.execute(con);
    }

    public static void resetBPQ() {
        try (Connection con = DatabaseConnection.getConnection()) {
            Statements.Update("boss_quest")
                    .set("attempts", 0)
                    .where("id", "> 1") // everyone
                    .execute(con);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
