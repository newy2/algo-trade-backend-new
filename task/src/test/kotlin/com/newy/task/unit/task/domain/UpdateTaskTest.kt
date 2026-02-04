package com.newy.task.unit.task.domain

import com.newy.task.common.error.ResourceConflictException
import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskAssignee
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.domain.UpdateTask
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import kotlin.test.assertEquals


class UpdateTaskTest {
    val updateTask = UpdateTask(
        currentUserId = 123,
        version = 0,
        taskId = 1,
        title = "Task 제목",
        updatedAt = OffsetDateTime.parse("2025-01-01T00:00:00.000Z"),
    )

    val savedTask = Task(
        id = 1,
        version = 0,
        title = "Task 제목",
        status = TaskStatus.IN_PROGRESS,
    )

    @Test
    fun `UpdateTask 는 updateBy 을 자동으로 할당해야 한다`() {
        assertEquals(123, updateTask.updatedBy)
    }

    @Test
    fun `UpdateTask 의 null 값은 DB에 저장된 Task 데이터로 채워져야 한다`() {
        val savedTask = savedTask.copy(
            description = "Task 설명",
            priority = TaskPriority.MEDIUM,
            startAt = OffsetDateTime.parse("2025-01-02T00:00:00.000Z"),
            endAt = OffsetDateTime.parse("2025-01-03T00:00:00.000Z"),
        )

        assertEquals(
            UpdateTask(
                currentUserId = 123,
                version = 0,
                taskId = 1,
                title = updateTask.title, // UpdateTask 에서 변경
                description = savedTask.description,
                status = savedTask.status,
                priority = savedTask.priority,
                startAt = savedTask.startAt,
                endAt = savedTask.endAt,
                updatedAt = updateTask.updatedAt, // UpdateTask 에서 생성
                assigneeIds = emptyList(),
            ),
            updateTask.merge(savedTask)
        )
    }

    @Test
    fun `UpdateTask 에 신규 추가, 삭제된 assignee 를 알 수 있어야 한다`() {
        val savedTask = savedTask.copy(
            assignees = listOf(
                TaskAssignee(id = 2, name = ""),
                TaskAssignee(id = 3, name = ""),
            )
        )
        val merged = updateTask.copy(assigneeIds = listOf(2, 4)).merge(savedTask)
        assertEquals(listOf<Long>(4), merged.addedAssigneeIds, "신규 추가된 assigneeIds")
        assertEquals(listOf<Long>(3), merged.deletedAssigneeIds, "삭제된 assigneeIds")
    }

    @Test
    fun `UpdateTask 의 version 과 DB에 저장된 version 과 다른 경우 에러가 발생한다`() {
        val updateTask = updateTask.copy(version = 0L)
        val savedTask = savedTask.copy(version = 1L)
        assertEquals(
            "Task 가 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해주세요.",
            assertThrows<ResourceConflictException> { updateTask.merge(savedTask) }.message
        )
    }
}