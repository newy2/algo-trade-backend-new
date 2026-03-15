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
    var databaseType: DatabaseType = dataSource.connection.use { connection ->
        val productName = connection.metaData.databaseProductName
        when {
            productName.contains("MySQL", true) -> DatabaseType.MYSQL
            productName.contains("PostgreSQL", true) -> DatabaseType.POSTGRESQL
            else -> throw IllegalStateException("Unknown DatabaseType $productName")
        }
    }

    fun isMySql(): Boolean = databaseType == DatabaseType.MYSQL
}
