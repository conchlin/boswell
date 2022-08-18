package server;

import client.MapleClient;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class NameReservation {
    private static final NameReservation instance = new NameReservation();
    public static Map<Integer, String> nameReserves = new HashMap<>();

    public static NameReservation getInstance() {
        return instance;
    }

    public void addNameReserve(MapleClient c, String name) {
        if (!hasNameReserve(c)) nameReserves.put(c.getAccID(), name);
    }

    public void removeNameReserve(MapleClient c) {
        nameReserves.remove(c.getAccID());
    }

    /**
     * @param c client
     * @return String of name reservation associated with client
     */
    public String getNameReserved(MapleClient c) {
        try {
            return nameReserves.get(c.getAccID());
        } catch (NullPointerException e) {
            return null;
        }
    }

    public boolean hasNameReserve(MapleClient c) {
        return nameReserves.containsKey(c.getAccID());
    }

    public boolean isNameAvailable(String name) {
        return nameReserves.containsValue(name);
    }

    public static void saveNameReserves(MapleClient c, Connection con) throws SQLException {
        Statements.Delete.from("name_reserves").where("accid", c.getAccID()).execute(con);
        Statements.BatchInsert statement = new Statements.BatchInsert("name_reserves");
        for (Map.Entry<Integer, String> entry : nameReserves.entrySet()) {
            statement.add("accid", entry.getKey());
            statement.add("name", entry.getValue());
        }
        statement.execute(con);
    }
}

