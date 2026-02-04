package com.newy.task.spring.config

import com.p6spy.engine.logging.Category
import com.p6spy.engine.spy.P6SpyOptions
import com.p6spy.engine.spy.appender.MessageFormattingStrategy
import jakarta.annotation.PostConstruct
import org.hibernate.engine.jdbc.internal.FormatStyle
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*

@Configuration
@Profile("dev", "test")
class P6SpyConfig : MessageFormattingStrategy {

    @PostConstruct
    fun setLogMessageFormat() {
        // P6Spy가 현재 클래스의 formatMessage를 사용하도록 등록
        P6SpyOptions.getActiveInstance().logMessageFormat = this::class.java.name
    }

    override fun formatMessage(
        connectionId: Int,
        now: String?,
        elapsed: Long,
        category: String?,
        prepared: String?,
        sql: String?,
        url: String?
    ): String {
        val formattedSql = formatSql(category, sql)
        return "[$category] | $elapsed ms | $formattedSql"
    }

    private fun formatSql(category: String?, sql: String?): String? {
        if (sql.isNullOrBlank()) return sql

        // SQL 실행(STATEMENT) 단계에서만 포맷팅 적용
        return if (Category.STATEMENT.getName() == category) {
            val trimmedSql = sql.trim().lowercase(Locale.ROOT)
            if (trimmedSql.startsWith("create") || trimmedSql.startsWith("alter") || trimmedSql.startsWith("comment")) {
                FormatStyle.DDL.getFormatter().format(sql)
            } else {
                FormatStyle.BASIC.getFormatter().format(sql)
            }
        } else {
            sql
        }
    }
}