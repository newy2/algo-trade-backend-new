package com.newy.task.task.domain

import java.time.OffsetDateTime

data class SearchTask(
    val keyword: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val page: Int,
    val size: Int,
)