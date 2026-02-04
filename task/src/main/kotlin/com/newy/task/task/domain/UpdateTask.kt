package com.newy.task.task.domain

import com.newy.task.common.error.ResourceConflictException
import java.time.OffsetDateTime

data class UpdateTask(
    private val currentUserId: Long,
    val version: Long,
    val taskId: Long,
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime,
    override val assigneeIds: List<Long> = emptyList(),
    val addedAssigneeIds: List<Long> = emptyList(),
    val deletedAssigneeIds: List<Long> = emptyList(),
) : AssigneeIdsValidator {
    val updatedBy: Long get() = currentUserId

    fun merge(savedTask: Task): UpdateTask {
        if (version != savedTask.version) {
            throw ResourceConflictException("Task 가 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해주세요.")
        }

        val oldAssigneeIds = savedTask.assignees.map { it.id }
        return copy(
            title = title ?: savedTask.title,
            description = description ?: savedTask.description,
            status = status ?: savedTask.status,
            priority = priority ?: savedTask.priority,
            startAt = startAt ?: savedTask.startAt,
            endAt = endAt ?: savedTask.endAt,
            addedAssigneeIds = assigneeIds - oldAssigneeIds,
            deletedAssigneeIds = oldAssigneeIds - assigneeIds,
        )
    }
}