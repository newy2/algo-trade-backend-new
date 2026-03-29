package com.newy.task.notification.adapter.out.persistence.querydsl

import com.newy.task.task.adapter.out.persistence.jpa.model.QTaskAssignmentJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.QTaskJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.QUserJpaEntity
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
    private val assignment = QTaskAssignmentJpaEntity.taskAssignmentJpaEntity
    private val user = QUserJpaEntity.userJpaEntity

    fun findTasksNearingDeadline(deadline: OffsetDateTime): List<TaskJpaEntity> {
        return queryFactory.selectFrom(task)
            .distinct()
            .leftJoin(task.assignments, assignment).fetchJoin()
            .leftJoin(assignment.user, user).fetchJoin()
            .where(
                task.endAt.between(deadline.minusMinutes(5), deadline),
                task.status.ne(TaskStatus.DONE)
            )
            .fetch()
    }
}
