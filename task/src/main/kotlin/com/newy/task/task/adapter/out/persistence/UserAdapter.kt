package com.newy.task.task.adapter.out.persistence

import com.newy.task.task.adapter.out.persistence.jpa.UserJpaRepository
import com.newy.task.task.port.out.FindUserIdsOutPort
import org.springframework.stereotype.Component

@Component
class UserAdapter(
    private val userJpaRepository: UserJpaRepository
) : FindUserIdsOutPort {
    override fun findUserIds(userIds: List<Long>): List<Long> {
        if (userIds.isEmpty()) {
            return emptyList()
        }

        return userJpaRepository.findAllIdsByIdIn(userIds)
    }

}