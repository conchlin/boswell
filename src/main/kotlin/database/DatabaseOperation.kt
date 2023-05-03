package database

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Timestamp

interface DatabaseOperation {

    fun execute(con: Connection)

    fun setValues(ps: PreparedStatement, values: MutableList<Any?>, partition: Int) {
        for (i in values.indices step partition) {
            for (j in 0 until partition) {
                setValue(j, ps, values[i + j])
            }
            ps.addBatch()
        }
    }

    fun setValues(ps: PreparedStatement, values: List<Any>) {
        for (i in values.indices) {
            setValue(i, ps, values[i])
        }
    }

    fun setValue(i: Int, ps: PreparedStatement, o: Any?) {
        val index = i + 1
        when (o) {
            is String -> ps.setString(index, o)
            is Int -> ps.setInt(index, o)
            is Float -> ps.setFloat(index, o)
            is Long -> ps.setLong(index, o)
            is Number -> ps.setInt(index, o.toInt())
            is Boolean -> ps.setBoolean(index, o)
            is Timestamp -> ps.setTimestamp(index, o)
            else -> ps.setObject(index, o, java.sql.Types.OTHER)
        }
    }
}