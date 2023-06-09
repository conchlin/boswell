package database

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class InsertTest {

    private val testTable = "test_table"

    @Test
    fun testExecute() {
        val con = createConnection()
        try {
            createTestTable(con)

            val insert = DatabaseStatements.Insert.into(testTable)
                .add("name", "John")
                .add("age", 30)

            insert.executeUpdate(con)

            val select = "select * from $testTable where name = 'John' and age = 30"
            val ps: PreparedStatement = con.prepareStatement(select)
            val rs: ResultSet = ps.executeQuery()
            assertTrue(rs.next())

        } finally {
            dropTestTable(con)
            con.close()
        }
    }

    private fun createConnection(): Connection {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/boswell-local", "postgres", "password")
    }

    private fun createTestTable(con: Connection) {
        val statement = con.createStatement()
        statement.execute("create table $testTable (id serial primary key, name varchar(255), age int)")
        statement.close()
    }

    private fun dropTestTable(con: Connection) {
        val statement = con.createStatement()
        statement.execute("drop table if exists $testTable")
        statement.close()
    }

}