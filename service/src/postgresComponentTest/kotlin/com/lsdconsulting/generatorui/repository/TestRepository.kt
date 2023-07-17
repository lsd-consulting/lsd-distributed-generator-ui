package com.lsdconsulting.generatorui.repository

import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class TestRepository {
    fun createTable(dataSource: DataSource) {
        val prepareDatabaseQuery = javaClass.getResourceAsStream("/db/prepareDatabase.sql")?.bufferedReader()?.readText()
        dataSource.connection.use { con ->
            con.prepareStatement(prepareDatabaseQuery).use { pst ->
                pst.executeUpdate()
            }
        }
    }

    fun clearTable(dataSource: DataSource) {
        dataSource.connection.use { con ->
            con.prepareStatement("truncate lsd.intercepted_interactions").use { pst ->
                pst.executeUpdate()
            }
        }
    }
}
