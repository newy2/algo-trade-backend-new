package com.newy.task.task.port.`in`.model

import com.newy.task.common.validation.SelfValidating
import com.newy.task.common.validation.UniqueElements
import com.newy.task.common.validation.ValidUtcDateTime
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.OffsetDateTime

data class CreateTaskCommand(
    @field:Min(1)
    val currentUserId: Long,

    @field:NotBlank(message = "title은 필수입니다.")
    val title: String? = null,

    val description: String? = null,

    @field:NotBlank(message = "status는 필수입니다.")
    @field:Pattern(regexp = "WAITING|IN_PROGRESS|DONE")
    val status: String? = null,

    @field:Pattern(regexp = "LOW|MEDIUM|HIGH")
    val priority: String? = null,

    @field:ValidUtcDateTime(message = "startedAt 날짜 형식이 올바르지 않습니다.")
    val startedAt: String? = null,

    @field:ValidUtcDateTime(message = "endedAt 날짜 형식이 올바르지 않습니다.")
    val endedAt: String? = null,

    @field:UniqueElements(message = "assigneeIds 는 중복된 데이터를 포함할 수 없습니다.")
    val assigneeIds: List<Long>? = null,

    val createdAt: OffsetDateTime = OffsetDateTime.now(),
) : SelfValidating() {
    init {
        validate()
    }

    fun toDomainModel() =
        normalize().run {
            CreateTask(
                currentUserId = currentUserId,
                title = title!!,
                description = description,
                status = TaskStatus.valueOf(status!!),
                priority = priority?.let { TaskPriority.valueOf(it) },
                startAt = startedAt?.let { OffsetDateTime.parse(it) },
                endAt = endedAt?.let { OffsetDateTime.parse(it) },
                assigneeIds = assigneeIds ?: emptyList(),
                createdAt = createdAt,
            )
        }

    private fun normalize() = copy(
        title = title?.trim(),
        description = description?.trim()
    )

}