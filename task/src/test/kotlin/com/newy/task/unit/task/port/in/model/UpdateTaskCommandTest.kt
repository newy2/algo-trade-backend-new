package com.newy.task.unit.task.port.`in`.model

import com.newy.task.task.domain.UpdateTask
import com.newy.task.task.port.`in`.model.UpdateTaskCommand
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import kotlin.test.assertEquals

class UpdateTaskCommandTest {
    val updatedAt = OffsetDateTime.now()
    private val inPortModel = UpdateTaskCommand(
        version = 100,
        currentUserId = 1,
        taskId = 2,
        title = "Task 제목",
        description = "Task 설명",
        updatedAt = updatedAt,
    )

    @Nested
    inner class ToDomainModelTest {
        private val domainModel = UpdateTask(
            version = 100,
            currentUserId = 1,
            taskId = 2,
            title = "Task 제목",
            description = "Task 설명",
            updatedAt = updatedAt,
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
    fun `version 는 0 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(version = null) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(version = -2) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(version = -1) }
        assertDoesNotThrow { inPortModel.copy(version = 0) }
        assertDoesNotThrow { inPortModel.copy(version = 1) }
    }

    @Test
    fun `currentUserId 는 1 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = -1) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = 0) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 1) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 2) }
    }


    @Test
    fun `taskId 는 1 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = -1) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(currentUserId = 0) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 1) }
        assertDoesNotThrow { inPortModel.copy(currentUserId = 2) }
    }

    @Test
    fun `title 은 빈 문자열을 사용할 수 없다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(title = "") }
        assertDoesNotThrow("Task title 업데이트는 옵셔널이다") { inPortModel.copy(title = null) }
        assertDoesNotThrow { inPortModel.copy(title = "a") }
    }

    @Test
    fun `status 는 WAITING, IN_PROGRESS, DONE 만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(status = "NOT_SUPPORT_VALUE") }
        assertDoesNotThrow("Task status 업데이트는 옵셔널이다") { inPortModel.copy(priority = null) }
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

    @Test
    fun `최소 1개 이상의 업데이트 필드 값이 있어야 한다`() {
        assertThrows<IllegalArgumentException> {
            inPortModel.copy(
                title = null,
                description = null,
                priority = null,
                startedAt = null,
                endedAt = null,
                assigneeIds = null,
            )
        }
    }
}