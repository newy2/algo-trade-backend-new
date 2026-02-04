package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.port.`in`.model.UpdateTaskCommand

data class UpdateTaskWebRequest(
    val version: Long?,
    val title: String?,
    val description: String?,
    val priority: String?,
    val status: String?,
    val startedAt: String?,
    val endedAt: String?,
    val assigneeIds: List<Long>?,
) {
    fun toInPortModel(currentUserId: Long, taskId: Long) =
        UpdateTaskCommand(
            version = version,
            currentUserId = currentUserId,
            taskId = taskId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            startedAt = startedAt,
            endedAt = endedAt,
            assigneeIds = assigneeIds,
        )
}