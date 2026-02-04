package com.newy.task.task.domain

import java.time.OffsetDateTime

data class Task(
    val id: Long = Long.MIN_VALUE,
    val title: String = "",
    val description: String? = null,
    val status: TaskStatus = TaskStatus.IN_PROGRESS,
    val priority: TaskPriority? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val assignees: List<TaskAssignee> = emptyList(),
    val version: Long = Long.MIN_VALUE,
)

data class TaskAssignee(
    val id: Long,
    val name: String
)