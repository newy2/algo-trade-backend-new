package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.port.`in`.model.CreateTaskCommand

data class CreateTaskWebRequest(
    val title: String?,
    val description: String? = null,
    val priority: String? = null,
    val status: String?,
    val startedAt: String? = null,
    val endedAt: String? = null,
    val assigneeIds: List<Long>? = null,
) {
    fun toInPortModel(currentUserId: Long) =
        CreateTaskCommand(
            currentUserId = currentUserId,
            title = title,
            description = description,
            status = status,
            priority = priority,
            startedAt = startedAt,
            endedAt = endedAt,
            assigneeIds = assigneeIds,
        )
}