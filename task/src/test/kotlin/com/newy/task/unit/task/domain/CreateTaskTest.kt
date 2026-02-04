package com.newy.task.unit.task.domain

import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.TaskStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import kotlin.test.assertEquals


class CreateTaskTest {
    val createTask = CreateTask(
        currentUserId = 123,
        title = "Task 제목",
        status = TaskStatus.IN_PROGRESS,
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.parse("2025-01-01T00:00:00.000Z"),
    )

    @Test
    fun `CreateTask 는 createBy, updateBy, updatedAt 을 자동으로 할당해야 한다`() {
        assertEquals(123, createTask.createdBy)
        assertEquals(123, createTask.updatedBy)
        assertEquals(OffsetDateTime.parse("2025-01-01T00:00:00.000Z"), createTask.createdAt)
        assertEquals(OffsetDateTime.parse("2025-01-01T00:00:00.000Z"), createTask.updatedAt)
    }

    @Test // TODO Refector? Service 테스트랑 중복되긴 하는데 일단 두자
    fun `DB 에 저장되지 않은 UserId 를 assigneeIds 전달한 경우 에러가 발생한다`() {
        val savedUserIds = listOf<Long>(1, 2)
        val notSavedUserIds = listOf<Long>(3, 4)
        val domainModel = createTask.copy(assigneeIds = notSavedUserIds)

        assertEquals(
            "존재하지 않는 사용자 ID가 포함되어 있습니다: [3, 4]",
            assertThrows<IllegalArgumentException> { domainModel.validateAssigneeIds(savedUserIds) }.message
        )
    }
}