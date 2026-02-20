package com.newy.task.spring.database

import org.springframework.stereotype.Component
import javax.sql.DataSource

enum class DatabaseType {
    MYSQL,
    POSTGRESQL,
}

@Component
class DatabaseTypeProvider(
    dataSource: DataSource
) {
    lateinit var databaseType: DatabaseType

    init {
        val meta = dataSource.connection.metaData
        databaseType = when {
            meta.databaseProductName.contains("MySQL", true) -> DatabaseType.MYSQL
            meta.databaseProductName.contains("PostgreSQL", true) -> DatabaseType.POSTGRESQL
            else -> throw IllegalStateException("Unknown DatabaseType ${meta.databaseProductName}")
        }
    }

    fun isMySql(): Boolean = databaseType == DatabaseType.MYSQL
}
