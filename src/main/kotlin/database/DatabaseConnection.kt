package database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import constants.ServerConstants
import java.sql.*
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit.SECONDS

class DatabaseConnection {
    companion object {
        private var ds: HikariDataSource? = null

        /**
         * This handles both the initialization of the database pool and all
         * connection needs
         */
        @JvmStatic @Throws(SQLException::class)
        fun getConnection(): Connection? {
            if (ds == null) {
                initialize()
            }

            return try {
                ds?.connection ?: run {
                    println("Attempting to make a connection before loading the database.")
                    null
                }
            } catch (ex: SQLException) {
                ex.printStackTrace(System.err)
                null
            }
        }

        private fun getConfig(): HikariConfig {
            val config = HikariConfig()

            config.jdbcUrl = ServerConstants.DB_URL
            config.username = ServerConstants.DB_USER
            config.password = ServerConstants.DB_PASS
            config.connectionTimeout = SECONDS.toMillis(30) // Hikari default
            /*
            As written in HikariCP docs the formula for counting connection pool size is
                connections = ((core_count * 2) + effective_spindle_count)
            but here we are just basing it off of the amount of registered accounts in the db
            */
            val poolSize =
                (0.00202020202 * getNumberOfAccounts() + 9.797979798).toInt().coerceIn(10, 30)
            config.maximumPoolSize = poolSize

            config.addDataSourceProperty("cachePrepStmts", true)
            config.addDataSourceProperty("prepStmtCacheSize", 25)
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048)

            return config
        }

        private fun getNumberOfAccounts(): Int {
            return try {
                DriverManager.getConnection(
                    ServerConstants.DB_URL,
                    ServerConstants.DB_USER,
                    ServerConstants.DB_PASS
                ).use { con ->
                    con.prepareStatement("SELECT count(*) FROM accounts").use { ps ->
                        ps.executeQuery().use { rs ->
                            if (rs.next()) rs.getInt(1) else 0
                        }
                    }
                }
            } catch (sqle: SQLException) {
                20
            }
        }

        /**
         * This no longer needs to be called upon server startup bc getConnection now exclusively uses
         * this method
         */
        private fun initialize() {
            println("Initializing connection pool...")
            val config = getConfig()
            val initStart = Instant.now()
            try {
                ds = HikariDataSource(config)
                val initDuration = Duration.between(initStart, Instant.now()).toMillis()
                println("Connection pool initialized in $initDuration ms")
            } catch (e: Exception) {
                val timeout = Duration.between(initStart, Instant.now()).seconds
                println("Failed to initialize database connection pool. Gave up after $timeout seconds.")
            }
        }

        /**
         * Properly close the DataSource
         * This should ONLY be called when shutting down the server
         */
        @JvmStatic
        fun close() {
            ds?.close()
        }
    }
}