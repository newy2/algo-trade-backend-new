package com.newy.task.notification.adapter.out.persistence.jpa

import com.newy.task.notification.adapter.out.persistence.jpa.model.NotificationOutboxJpaEntity
import com.newy.task.notification.domain.NotificationStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationOutboxJpaRepository : JpaRepository<NotificationOutboxJpaEntity, Long> {
    fun findByStatusOrderByCreatedAtAsc(
        status: NotificationStatus,
        pageable: Pageable
    ): List<NotificationOutboxJpaEntity>

    fun existsByEventKey(key: String): Boolean
}