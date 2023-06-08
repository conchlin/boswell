package database

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager

internal class BatchInsertTest {

    private val tableName = "test_table"
    private val column1 = "col1"
    private val column2 = "col2"
    private val column3 = "col3"
    private val value1 = "value1"
    private val value2 = 2
    private val value3 = true

    @Test
    fun testExecute() {
        val connection = createConnection()
        createTable(connection)

        val batchInsert = DatabaseStatements.BatchInsert(tableName)
        batchInsert.add(column1, value1)
        batchInsert.add(column2, value2)
        batchInsert.add(column3, value3)

        assertDoesNotThrow { batchInsert.execute(connection) }

        val resultSet = connection.createStatement().executeQuery("SELECT * FROM $tableName")
        assertTrue(resultSet.next())
        assertEquals(value1, resultSet.getString(column1))
        assertEquals(value2, resultSet.getInt(column2))
        assertEquals(value3, resultSet.getBoolean(column3))
    }

    private fun createConnection(): Connection {
        // you'll need to change this to match your local instance
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/boswell-local", "postgres", "password")
    }

    private fun createTable(connection: Connection) {
        val statement = connection.createStatement()
        val sql = """
            CREATE TABLE IF NOT EXISTS $tableName (
                $column1 TEXT,
                $column2 INTEGER,
                $column3 BOOLEAN
            );
        """.trimIndent()
        statement.executeUpdate(sql)
        statement.close()
    }

}