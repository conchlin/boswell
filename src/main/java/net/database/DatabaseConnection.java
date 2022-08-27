package net.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import constants.ServerConstants;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Frz - Big Daddy
 * @author The Real Spookster - some modifications to this beautiful code
 * @author Ronan - some connection pool to this beautiful code
 */
public class DatabaseConnection {
    private static HikariDataSource ds;


    public static Connection getConnection() throws SQLException {
        try {
            if (ds != null) return ds.getConnection();

            System.out.println("Attempting to make a connection before loading the database.");
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
        }
        return null;
    }

    private static int getNumberOfAccounts() {
        try {
            try (Connection con = DriverManager.getConnection(ServerConstants.DB_URL, ServerConstants.DB_USER, ServerConstants.DB_PASS);
                 PreparedStatement ps = con.prepareStatement("SELECT count(*) FROM accounts")) {
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getInt(1);
                }
            }
        } catch (SQLException sqle) {
            return 20;
        }
    }

    private static HikariConfig getConfig() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(ServerConstants.DB_URL);
        config.setUsername(ServerConstants.DB_USER);
        config.setPassword(ServerConstants.DB_PASS);
        config.setConnectionTimeout(SECONDS.toMillis(30)); // Hikari default
         /*
        As written in HikariCP docs the formula for counting connection pool size is
            connections = ((core_count * 2) + effective_spindle_count)
        but here we are just basing it off of the amount of registered accounts in the db
        */
        int poolSize = (int) Math.ceil(0.00202020202 * getNumberOfAccounts() + 9.797979798);
        if (poolSize < 10) poolSize = 10;
        else if (poolSize > 30) poolSize = 30;
        config.setMaximumPoolSize(poolSize);

        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtCacheSize", 25);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);

        return config;
    }

    /**
     * Load the DataSource upon startup for use to access the object.
     */
    public static void initialize() {
        if (ds == null) {
            System.out.println("Initializing connection pool...");
            final HikariConfig config = getConfig();
            Instant initStart = Instant.now();
            try {
                ds = new HikariDataSource(config);
                long initDuration = Duration.between(initStart, Instant.now()).toMillis();
                System.out.println("Connection pool initialized in " + initDuration + " ms");
            } catch (Exception e) {
                long timeout = Duration.between(initStart, Instant.now()).getSeconds();
                System.out.println("Failed to initialize database connection pool. Gave up after " + timeout + " seconds.");
            }
        }
    }

    /**
     * Properly close the DataSource
     * This should ONLY be called when shutting down the server
     */
    public void close() {
        ds.close();
    }
}
