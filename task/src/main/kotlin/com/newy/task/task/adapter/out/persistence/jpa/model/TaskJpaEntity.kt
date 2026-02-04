package com.newy.task.task.adapter.out.persistence.jpa.model

import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskAssignee
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "task")
class TaskJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    var title: String,

    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: TaskStatus,

    @Enumerated(EnumType.STRING)
    @Column
    var priority: TaskPriority? = null,

    var startAt: OffsetDateTime? = null,

    var endAt: OffsetDateTime? = null,

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "task", cascade = [CascadeType.ALL], orphanRemoval = true)
    val assignments: MutableList<TaskAssignmentJpaEntity> = mutableListOf(),

    @Version
    var version: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", updatable = false)
    val creator: UserJpaEntity,

    @Column(updatable = false)
    val createdAt: OffsetDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    var updater: UserJpaEntity,

    var updatedAt: OffsetDateTime,
) {
    constructor(createTask: CreateTask, creator: UserJpaEntity, updater: UserJpaEntity) : this(
        title = createTask.title,
        description = createTask.description,
        status = createTask.status,
        priority = createTask.priority,
        startAt = createTask.startAt,
        endAt = createTask.endAt,
        creator = creator,
        createdAt = createTask.createdAt,
        updater = updater,
        updatedAt = createTask.updatedAt
    )

    fun addAssignments(newAssignments: List<TaskAssignmentJpaEntity>) {
        this.assignments.addAll(newAssignments)
    }

    fun toDomainModel() =
        Task(
            id = id,
            title = title,
            description = description,
            status = status,
            priority = priority,
            startAt = startAt,
            endAt = endAt,
            version = version,
            assignees =
                assignments
                    .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.user.nickname })
                    .map { TaskAssignee(id = it.user.id, name = it.user.nickname) }
        )
}