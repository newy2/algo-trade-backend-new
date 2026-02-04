package com.newy.task.unit.task.port.`in`.model

import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.port.`in`.model.CreateTaskCommand
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import kotlin.test.assertEquals

class CreateTaskCommandTest {
    val createdAt = OffsetDateTime.now()
    private val inPortModel = CreateTaskCommand(
        currentUserId = 1,
        title = "Task 제목",
        description = "Task 설명",
        status = "WAITING",
        createdAt = createdAt,
    )

    @Nested
    inner class ToDomainModelTest {
        private val domainModel = CreateTask(
            currentUserId = 1,
            title = "Task 제목",
            description = "Task 설명",
            status = TaskStatus.WAITING,
            assigneeIds = emptyList(),
            createdAt = createdAt,
        )

        @Test
        fun `InPortModel 은 DomainModel 로 변환할 수 있어야 한다`() {
            assertEquals(domainModel, inPortModel.toDomainModel())
        }

        @Test
        fun `title 과 description 은 trim 처리 한다`() {
            assertEquals(domainModel, inPortModel.copy(title = "  Task 제목  ").toDomainModel())
            assertEquals(domainModel, inPortModel.copy(description = "  Task 설명  ").toDomainModel())
        }
    }

    @Test
    fun `currentUserId 는 1 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = -1) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = 0) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 1) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 2) }
    }

    @Test
    fun `status 는 WAITING, IN_PROGRESS, DONE 만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(status = "NOT_SUPPORT_VALUE") }
        assertThrows<ConstraintViolationException>("status는 필수값이다") { inPortModel.copy(status = null) }
        assertDoesNotThrow { inPortModel.copy(status = "WAITING") }
        assertDoesNotThrow { inPortModel.copy(status = "IN_PROGRESS") }
        assertDoesNotThrow { inPortModel.copy(status = "DONE") }
    }

    @Test
    fun `priority 는 LOW, MEDIUM, HIGH 만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(priority = "NOT_SUPPORT_VALUE") }
        assertDoesNotThrow { inPortModel.copy(priority = null) }
        assertDoesNotThrow { inPortModel.copy(priority = "LOW") }
        assertDoesNotThrow { inPortModel.copy(priority = "MEDIUM") }
        assertDoesNotThrow { inPortModel.copy(priority = "HIGH") }
    }

    @Test
    fun `assigneeIds 는 중복된 element 를 포함할 수 없다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(assigneeIds = listOf(1, 1)) }
        assertDoesNotThrow { inPortModel.copy(assigneeIds = null) }
        assertDoesNotThrow { inPortModel.copy(assigneeIds = listOf()) }
        assertDoesNotThrow { inPortModel.copy(assigneeIds = listOf(1, 2)) }
    }

    @Test
    fun `statedAt 은 UTC 형식만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(startedAt = "2026-01-25T03:00:00+09:00") }
        assertDoesNotThrow { inPortModel.copy(startedAt = null) }
        assertDoesNotThrow { inPortModel.copy(startedAt = "2026-01-26T03:00:00Z") }
    }

    @Test
    fun `endedAt 은 UTC 형식만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(endedAt = "2026-01-25T03:00:00+09:00") }
        assertDoesNotThrow { inPortModel.copy(endedAt = null) }
        assertDoesNotThrow { inPortModel.copy(endedAt = "2026-01-26T03:00:00Z") }
    }
}