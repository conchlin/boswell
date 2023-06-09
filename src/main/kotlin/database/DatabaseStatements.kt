package database

import java.sql.*
import java.util.*


class DatabaseStatements {

    class Insert(private val table: String) : DatabaseOperation {
        private val columns = mutableListOf<String>()
        private val values = mutableListOf<Any>()

        fun add(column: String, value: Any): Insert {
            columns.add(column)
            values.add(value)
            return this
        }

        companion object {
            @JvmStatic
            fun into(table: String): Insert {
                return Insert(table)
            }
        }

        @Throws(SQLException::class)
        override fun execute(con: Connection) {}

        /**
         * returns the result so that we can see if the statement was successful or not
         */
        @Throws(SQLException::class)
        fun executeUpdate(con: Connection): Int {
            if (values.size == 0) return -1

            val columnsStr = "(${columns.joinToString(", ")})"
            val valuesStr = "(${Collections.nCopies(values.size, "?").joinToString(", ")})"
            val statement = "insert into $table $columnsStr values $valuesStr"
            val ps = con.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)

            setValues(ps, values)

            columns.clear()
            values.clear()

            if (ps.executeUpdate() <= 0) return -1

            ps.generatedKeys.use { rs ->
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }

            return -1
        }
    }

    class BatchInsert(private val table: String) : DatabaseOperation {
        private val columns = mutableListOf<String>()
        private val values = mutableListOf<Any?>()

        fun add(column: String, o: Any?) {
            if (column !in columns)
                columns.add(column)
            values.add(o)
        }

        @Throws(SQLException::class)
        override fun execute(con: Connection) {
            if (values.isEmpty()) return

            val columnsStr = "(${columns.joinToString(", ")})"
            val valuesStr = "(${Collections.nCopies(columns.size, "?").joinToString(", ")})"

            val sb = StringBuilder()
            sb.append("insert into ").append(table).append(" ")
            sb.append(columnsStr).append(" values ").append(valuesStr)

            val ps = con.prepareStatement(sb.toString())
            setValues(ps, values, columns.size)
            ps.executeBatch()
            ps.close()

            columns.clear()
            values.clear()
        }
    }


    class Update(private val table: String) : DatabaseOperation {
        private val columns = ArrayList<String>()
        private val values = ArrayList<Any>()
        private val condColumns = ArrayList<String>()
        private val condValues = ArrayList<Any>()

        fun set(column: String, o: Any): Update {
            columns.add("$column = ?")
            values.add(o)
            return this
        }

        fun where(column: String, o: Any): Update {
            cond(column, o)
            return this
        }

        fun cond(column: String, o: Any) {
            condColumns.add("$column = ?")
            condValues.add(o)
        }

        @Throws(SQLException::class)
        fun executeKeys(con: Connection): Int {
            if (values.isEmpty()) return -1
            val statementBuilder = StringBuilder("update $table set ")
            val setBuilder = StringBuilder()
            for (i in columns.indices) {
                if (i > 0) {
                    setBuilder.append(", ")
                }
                setBuilder.append(columns[i])
            }
            statementBuilder.append(setBuilder)
            if (condColumns.isNotEmpty()) {
                val condBuilder = StringBuilder(" where ")
                for (i in condColumns.indices) {
                    if (i > 0) {
                        condBuilder.append(" and ")
                    }
                    condBuilder.append(condColumns[i])
                }
                statementBuilder.append(condBuilder)
            }
            val ps = con.prepareStatement(statementBuilder.toString(), Statement.RETURN_GENERATED_KEYS)
            val allValues = ArrayList<Any>().apply {
                addAll(values)
                addAll(condValues)
            }
            setValues(ps, allValues)
            val rows = ps.executeUpdate()
            ps.close()
            return rows
        }

        @Throws(SQLException::class)
        override fun execute(con: Connection) {
            if (values.isEmpty()) return
            val statementBuilder = StringBuilder("update $table set ")
            val setBuilder = StringBuilder()
            for (i in columns.indices) {
                if (i > 0) {
                    setBuilder.append(", ")
                }
                setBuilder.append(columns[i])
            }
            statementBuilder.append(setBuilder)
            if (condColumns.isNotEmpty()) {
                val condBuilder = StringBuilder(" where ")
                for (i in condColumns.indices) {
                    if (i > 0) {
                        condBuilder.append(" and ")
                    }
                    condBuilder.append(condColumns[i])
                }
                statementBuilder.append(condBuilder)
            }
            val ps = con.prepareStatement(statementBuilder.toString())
            val allValues = ArrayList<Any>().apply {
                addAll(values)
                addAll(condValues)
            }
            setValues(ps, allValues)
            ps.executeUpdate()
            ps.close()
        }
    }

    class Delete private constructor(private val table: String) : DatabaseOperation {
        private val condColumns = mutableListOf<String>()
        private val condValues = mutableListOf<Any>()

        companion object {
            @JvmStatic
            fun from(table: String): Delete {
                return Delete(table)
            }
        }

        fun where(column: String, o: Any): Delete {
            condColumns.add("$column = ?")
            condValues.add(o)
            return this
        }

        @Throws(SQLException::class)
        override fun execute(con: Connection) {
            val sb = StringBuilder("delete from ").append(table)
            if (condColumns.isNotEmpty()) {
                sb.append(" where ")
                sb.append(condColumns.joinToString(" and "))
            }
            val ps = con.prepareStatement(sb.toString())
            setValues(ps, condValues)
            ps.execute()
            ps.close()
        }
    }

    class Truncate(private val table: String) : DatabaseOperation {
        override fun execute(con: Connection) {
            val statement = StringBuilder("TRUNCATE TABLE ").append(table).toString()
            val preparedStatement = con.prepareStatement(statement)
            preparedStatement.execute()
            preparedStatement.close()
        }
    }
}
