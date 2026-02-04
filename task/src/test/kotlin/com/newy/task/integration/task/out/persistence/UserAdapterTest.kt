package com.newy.task.integration.task.out.persistence

import com.newy.task.integration.helper.DataJpaTestHelper
import com.newy.task.task.adapter.out.persistence.UserAdapter
import com.newy.task.task.adapter.out.persistence.jpa.UserJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals

@DataJpaTestHelper
@Transactional
class UserAdapterTest(
    @Autowired val userAdapter: UserAdapter,
    @Autowired val userJpaRepository: UserJpaRepository
) {
    @Test
    fun `findUserIds - DB 에 저장된 사용자 ID 조회하기`() {
        val savedUserId1 = saveUser(UserJpaEntity(nickname = "홍길동")).id
        val savedUserId2 = saveUser(UserJpaEntity(nickname = "홍길순")).id
        val notSavedUserId = savedUserId2 + 1

        val requestUserIds = listOf(savedUserId1, savedUserId2, notSavedUserId)
        val savedUserIds = userAdapter.findUserIds(requestUserIds)

        val differenceUserIds = requestUserIds - savedUserIds
        assertEquals(listOf(notSavedUserId), differenceUserIds)
    }

    @Test
    fun `findUserIds - empty array 조회하기`() {
        val requestUserIds = emptyList<Long>()
        val savedUserIds = userAdapter.findUserIds(emptyList())

        val differenceUserIds = requestUserIds - savedUserIds
        assertEquals(emptyList(), differenceUserIds)
    }

    private fun saveUser(user: UserJpaEntity): UserJpaEntity {
        userJpaRepository.save(user)
        return user
    }
}