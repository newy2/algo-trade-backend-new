package com.newy.task.task.adapter.out.persistence.querydsl.model

import com.newy.task.task.domain.SearchTask
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import java.time.OffsetDateTime

/**
 * 작업을 검색하기 위한 필터 조건 DTO
 */
data class TaskSearchCondition(
    val keyword: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
) {
    companion object {
        fun fromDomainModel(searchTask: SearchTask) =
            TaskSearchCondition(
                keyword = searchTask.keyword,
                status = searchTask.status,
                priority = searchTask.priority,
                startAt = searchTask.startAt,
                endAt = searchTask.endAt,
            )
    }
}