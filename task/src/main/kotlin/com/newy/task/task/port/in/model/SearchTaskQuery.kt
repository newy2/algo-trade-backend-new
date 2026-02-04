package com.newy.task.task.port.`in`.model

import com.newy.task.common.validation.SelfValidating
import com.newy.task.common.validation.ValidUtcDateTime
import com.newy.task.task.domain.SearchTask
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class SearchTaskQuery(
    @field:Size(min = 1, message = "keyword는 빈 문자열일 수 없습니다.")
    val keyword: String? = null,

    @field:Pattern(regexp = "WAITING|IN_PROGRESS|DONE")
    val status: String? = null,

    @field:Pattern(regexp = "LOW|MEDIUM|HIGH")
    val priority: String? = null,

    @field:ValidUtcDateTime(message = "startedAt 날짜 형식이 올바르지 않습니다.")
    val startAt: String? = null,

    @field:ValidUtcDateTime(message = "endedAt 날짜 형식이 올바르지 않습니다.")
    val endAt: String? = null,

    @field:Min(0)
    val page: Int,

    @field:Min(0)
    val size: Int,
) : SelfValidating() {
    init {
        validate()
    }

    fun toDomainModel() =
        normalize().run {
            SearchTask(
                keyword = keyword,
                status = status?.let { TaskStatus.valueOf(it) },
                priority = priority?.let { TaskPriority.valueOf(it) },
                startAt = startAt?.let { OffsetDateTime.parse(it) },
                endAt = endAt?.let { OffsetDateTime.parse(it) },
                page = page,
                size = size,
            )
        }

    private fun normalize() = copy(
        keyword = keyword?.trim(),
    )
}