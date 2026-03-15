package com.newy.task.unit.spring.database

import com.newy.task.spring.database.DatabaseTypeProvider
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.PrintWriter
import java.lang.reflect.Proxy
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.SQLFeatureNotSupportedException
import javax.sql.DataSource
import kotlin.test.assertTrue

@DisplayName("DatabaseTypeProvider 테스트")
class DatabaseTypeProviderTest {
    @Test
    fun `DB 타입을 확인한 후 DataSource Connection 을 닫아야 한다`() {
        var closed = false
        val metaData = databaseMetaData(productName = "MySQL")
        val connection = connection(
            metaData = metaData,
            onClose = { closed = true },
        )
        val dataSource = fakeDataSource(connection)

        DatabaseTypeProvider(dataSource)

        assertTrue(closed)
    }

    private fun databaseMetaData(productName: String): DatabaseMetaData =
        Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(DatabaseMetaData::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getDatabaseProductName" -> productName
                "toString" -> "FakeDatabaseMetaData(productName=$productName)"
                else -> defaultValue(method.returnType)
            }
        } as DatabaseMetaData

    private fun connection(
        metaData: DatabaseMetaData,
        onClose: () -> Unit,
    ): Connection =
        Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Connection::class.java)
        ) { _, method, _ ->
            when (method.name) {
                "getMetaData" -> metaData
                "close" -> onClose()
                "toString" -> "FakeConnection"
                else -> defaultValue(method.returnType)
            }
        } as Connection

    private fun fakeDataSource(connection: Connection): DataSource =
        object : DataSource {
            override fun getConnection(): Connection = connection

            override fun getConnection(username: String?, password: String?): Connection = connection

            override fun getLogWriter(): PrintWriter? = null

            override fun setLogWriter(out: PrintWriter?) = Unit

            override fun setLoginTimeout(seconds: Int) = Unit

            override fun getLoginTimeout(): Int = 0

            override fun getParentLogger() = throw SQLFeatureNotSupportedException()

            override fun <T : Any?> unwrap(iface: Class<T>?): T = throw SQLFeatureNotSupportedException()

            override fun isWrapperFor(iface: Class<*>?): Boolean = false
        }

    private fun defaultValue(returnType: Class<*>): Any? =
        when (returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Byte.TYPE -> 0.toByte()
            java.lang.Short.TYPE -> 0.toShort()
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Float.TYPE -> 0f
            java.lang.Double.TYPE -> 0.0
            java.lang.Character.TYPE -> '\u0000'
            java.lang.Void.TYPE -> null
            else -> null
        }
}
