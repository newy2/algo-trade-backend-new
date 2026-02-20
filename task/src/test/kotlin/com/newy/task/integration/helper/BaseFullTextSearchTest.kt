package com.newy.task.integration.helper

import com.newy.task.spring.database.DatabaseTypeProvider
import org.springframework.test.context.transaction.TestTransaction

interface BaseFullTextSearchTest {
    val databaseTypeProvider: DatabaseTypeProvider

    fun cleanupForMySql()

    fun fullTextSearchTestTemplate(callable: () -> Unit) {
        if (!databaseTypeProvider.isMySql()) {
            callable()
        } else {
            commitForMySqlFullTextIndex()
            try {
                callable()
            } finally {
                cleanupForMySql()
            }
        }
    }

    private fun commitForMySqlFullTextIndex() {
        // 테스트 코드에서 MySQL 의 FULL TEXT INDEX 를 사용하기 위해 commit 해야 한다
        TestTransaction.flagForCommit()
        TestTransaction.end()
    }
}