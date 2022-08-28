package net.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Statements {

    private static void set_values(PreparedStatement ps, List<Object> values, int partition) throws SQLException {
        for (int i = 0; i < values.size(); i += partition) {
            for (int j = 0; j < partition; j++) {
                set_value(j, ps, values.get(i + j));
            }
            ps.addBatch();
        }
    }

    private static void set_values(PreparedStatement ps, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            set_value(i, ps, values.get(i));
        }
    }

    private static void set_value(int i, PreparedStatement ps, Object o) throws SQLException {
        i++;
        if (o instanceof String) {
            ps.setString(i, (String) o);
        } else if (o instanceof Integer) {
            ps.setInt(i, (int) o);
        } else if (o instanceof Float) {
            ps.setFloat(i, (float) o);
        } else if (o instanceof Long) {
            ps.setLong(i, (long) o);
        } else if (o instanceof Number) {
            ps.setInt(i, ((Number) o).intValue());
        } else if (o instanceof Boolean) {
            ps.setBoolean(i, (boolean) o);
        } else if (o instanceof Timestamp) {
            ps.setTimestamp(i, (Timestamp) o);
        } else {
            ps.setObject(i, o, java.sql.Types.OTHER);
        }
    }

    public static class Insert {
        private List<String> columns = new ArrayList<>();
        private List<Object> values = new ArrayList<>();

        private String table;

        public Insert(String table) {
            this.table = table;
        }

        public Insert add(String column, Object o) {
            columns.add(column);
            values.add(o);
            return this;
        }

        public static Insert into(String table) {
            return new Insert(table);
        }

        public int execute(Connection con) throws SQLException {
            if (values.size() == 0) return -1;

            String columns_str = "(" + String.join(", ", columns) + ")";
            String values_str = "(" + String.join(", ", Collections.nCopies(values.size(), "?")) + ")";
            String statement = "insert into " + table + " " + columns_str + " values " + values_str;
            PreparedStatement ps = con.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);

            set_values(ps, values);

            columns.clear();
            values.clear();

            if (ps.executeUpdate() <= 0) return -1;

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int ret = rs.getInt(1);
                    return ret;
                }
            }

            return -1;
        }
    }

    public static class BatchInsert {
        private List<String> columns = new ArrayList<>();
        private List<Object> values = new ArrayList<>();

        private String table;

        public BatchInsert(String table) {
            this.table = table;
        }

        public void add(String column, Object o) {
            if (!columns.contains(column))
                columns.add(column);
            values.add(o);
        }

        public void execute(Connection con) throws SQLException {
            if (values.size() == 0) return;

            String columns_str = "(" + String.join(", ", columns) + ")";
            String values_str = "(" + String.join(", ", Collections.nCopies(columns.size(), "?")) + ")";
            String statement = "insert into " + table + " " + columns_str + " values " + values_str;
            PreparedStatement ps = con.prepareStatement(statement);
            set_values(ps, values, columns.size());
            ps.executeBatch();
            ps.close();
            
            columns.clear();
            values.clear();
        }
    }

    public static Update Update(String table) {
        return new Update(table);
    }

    public static class Update {
        private List<String> columns = new ArrayList<>();
        private List<Object> values = new ArrayList<>();

        private List<String> cond_columns = new ArrayList<>();
        private List<Object> cond_values = new ArrayList<>();

        private String table;

        public Update(String table) {
            this.table = table;
        }

        public Update set(String column, Object o) {
            columns.add(column + " = ?");
            values.add(o);
            return this;
        }

        public Update where(String column, Object o) {
            this.cond(column, o);
            return this;
        }

        public void cond(String column, Object o) {
            cond_columns.add(column + " = ?");
            cond_values.add(o);
        }

        public int execute_keys(Connection con) throws SQLException {
            if (values.size() == 0) return -1;
            String set = String.join(", ", columns);
            String cond = String.join(" and ", cond_columns);

            String statement = "update " + table + " set " + set;
            if (cond.length() > 0)
                statement += " where " + cond;
            PreparedStatement ps = con.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);

            List<Object> all_values = new ArrayList<Object>() {{
                addAll(values);
                addAll(cond_values);
            }};

            set_values(ps, all_values);

            int rows = ps.executeUpdate();
            ps.close();
            return rows;
        }

        public void execute(Connection con) throws SQLException {
            if (values.size() == 0) return;
            String set = String.join(", ", columns);
            String cond = String.join(" and ", cond_columns);

            String statement = "update " + table + " set " + set;
            if (cond.length() > 0)
                statement += " where " + cond;
            PreparedStatement ps = con.prepareStatement(statement);

            List<Object> all_values = new ArrayList<Object>() {{
                addAll(values);
                addAll(cond_values);
            }};

            set_values(ps, all_values);

            ps.executeUpdate();
            ps.close();
        }
    }

    public static class Delete {
        private List<String> cond_columns = new ArrayList<>();
        private List<Object> cond_values = new ArrayList<>();

        private String table;

        public static Delete from(String table) {
            return new Delete(table);
        }

        public Delete(String table) {
            this.table = table;
        }

        public Delete where(String column, Object o) {
            cond_columns.add(column + " = ?");
            cond_values.add(o);
            return this;
        }

        public void execute(Connection con) throws SQLException {
            String cond = String.join(" and ", cond_columns);
            String statement = "delete from " + table + " where " + cond;
            PreparedStatement ps = con.prepareStatement(statement);
            set_values(ps, cond_values);
            ps.execute();
            ps.close();
        }
    }

    public static class Truncate {
        private String table;

        public static Truncate wipe(String table) {return new Truncate(table); }

        public Truncate(String table) {
            this.table = table;
        }

        public void execute(Connection con) throws SQLException {
            String statement = "truncate " + table;
            PreparedStatement ps = con.prepareStatement(statement);
            ps.execute();
            ps.close();
        }
    }
}
