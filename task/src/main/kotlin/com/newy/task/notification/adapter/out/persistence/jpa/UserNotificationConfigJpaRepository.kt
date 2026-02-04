package com.newy.task.notification.adapter.out.persistence.jpa

import com.newy.task.notification.adapter.out.persistence.jpa.model.UserNotificationConfigJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserNotificationConfigJpaRepository : JpaRepository<UserNotificationConfigJpaEntity, Long> {
    fun findAllByUserIdIn(userIds: Collection<Long>): List<UserNotificationConfigJpaEntity>
}