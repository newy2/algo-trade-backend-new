package com.newy.task.task.adapter.out.persistence.jpa.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.OffsetDateTime

@Embeddable
data class TaskAssigneeId(
    val taskId: Long = 0,
    val userId: Long = 0
) : Serializable

@Entity
@Table(name = "task_assignment")
class TaskAssignmentJpaEntity(
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    val task: TaskJpaEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: UserJpaEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    val creator: UserJpaEntity,

    @Column(updatable = false)
    val createdAt: OffsetDateTime,
) {
    @EmbeddedId
    val id: TaskAssigneeId = TaskAssigneeId(taskId = task.id, userId = user.id)
}
