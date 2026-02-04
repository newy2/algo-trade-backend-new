package com.newy.task.notification.adapter.out.persistence.querydsl

import com.newy.task.task.adapter.out.persistence.jpa.model.QTaskJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.TaskJpaEntity
import com.newy.task.task.domain.TaskStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class TaskNotificationQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
) {
    private val task = QTaskJpaEntity.taskJpaEntity

    fun findTasksNearingDeadline(deadline: OffsetDateTime): List<TaskJpaEntity> {
        return queryFactory.selectFrom(task)
            .where(
                task.endAt.between(deadline.minusMinutes(5), deadline),
                task.status.ne(TaskStatus.DONE)
            )
            .fetch()
    }
}