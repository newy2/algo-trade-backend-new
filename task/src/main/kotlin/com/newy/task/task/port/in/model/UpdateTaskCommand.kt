package com.newy.task.task.port.`in`.model

import com.newy.task.common.validation.SelfValidating
import com.newy.task.common.validation.UniqueElements
import com.newy.task.common.validation.ValidUtcDateTime
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.domain.UpdateTask
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.OffsetDateTime

data class UpdateTaskCommand(
    @field:Min(0)
    @field:NotNull(message = "version은 필수입니다.")
    val version: Long? = null,

    @field:Min(1)
    val currentUserId: Long,

    @field:Min(1)
    val taskId: Long,

    @field:Size(min = 1, message = "title은 빈 문자열일 수 없습니다.")
    val title: String? = null,

    val description: String? = null,

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

    val updatedAt: OffsetDateTime = OffsetDateTime.now(),
) : SelfValidating() {
    init {
        validate()
        validateAtLeastOneUpdatableField()
    }

    fun toDomainModel() =
        normalize().run {
            UpdateTask(
                version = version!!,
                currentUserId = currentUserId,
                taskId = taskId,
                title = title,
                description = description,
                status = status?.let { TaskStatus.valueOf(it) },
                priority = priority?.let { TaskPriority.valueOf(it) },
                startAt = startedAt?.let { OffsetDateTime.parse(it) },
                endAt = endedAt?.let { OffsetDateTime.parse(it) },
                assigneeIds = assigneeIds ?: emptyList(),
                updatedAt = updatedAt,
            )
        }

    private fun normalize() = copy(
        title = title?.trim(),
        description = description?.trim()
    )

    private fun validateAtLeastOneUpdatableField() {
        val updatableFields = mapOf(
            "title" to title,
            "description" to description,
            "status" to status,
            "priority" to priority,
            "startedAt" to startedAt,
            "endedAt" to endedAt,
            "assigneeIds" to assigneeIds,
        )
        if (updatableFields.values.all { it == null }) {
            val fieldStatus = updatableFields.entries.joinToString(", ") { "${it.key}=null" }
            throw IllegalArgumentException(
                "업데이트할 데이터가 최소 하나는 필요합니다. 필드 상태: {$fieldStatus}"
            )
        }
    }
}