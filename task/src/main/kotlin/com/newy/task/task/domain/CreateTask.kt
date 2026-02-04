package com.newy.task.task.domain

import java.time.OffsetDateTime

data class CreateTask(
    private val currentUserId: Long,
    val title: String,
    val description: String? = null,
    val status: TaskStatus,
    val priority: TaskPriority? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    override val assigneeIds: List<Long> = emptyList(),
    val createdAt: OffsetDateTime,
) : AssigneeIdsValidator {
    val createdBy: Long get() = currentUserId
    val updatedBy: Long get() = currentUserId
    val updatedAt: OffsetDateTime get() = createdAt
}

