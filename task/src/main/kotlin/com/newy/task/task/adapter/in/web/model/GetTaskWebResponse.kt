package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import java.time.OffsetDateTime

data class GetTaskWebResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority?,
    val startAt: OffsetDateTime?,
    val endAt: OffsetDateTime?,
    val assignees: List<GetTaskWebResponseAssignee>,
) {
    companion object {
        fun fromDomainModel(domainModel: Task) =
            GetTaskWebResponse(
                id = domainModel.id,
                title = domainModel.title,
                description = domainModel.description,
                status = domainModel.status,
                priority = domainModel.priority,
                startAt = domainModel.startAt,
                endAt = domainModel.endAt,
                assignees = domainModel.assignees.map {
                    GetTaskWebResponseAssignee(
                        id = it.id,
                        name = it.name,
                    )
                },
            )
    }
}

data class GetTaskWebResponseAssignee(
    val id: Long,
    val name: String,
)