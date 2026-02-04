package com.newy.task.integration.task.out.persistence

import com.newy.task.common.error.ResourceConflictException
import com.newy.task.integration.helper.DataJpaTestHelper
import com.newy.task.task.adapter.out.persistence.TaskAdapter
import com.newy.task.task.adapter.out.persistence.jpa.TaskAssignmentJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.TaskJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.UserJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.domain.UpdateTask
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@DataJpaTestHelper
@Transactional
@DisplayName("Task 생성 테스트")
class CreateTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired val taskJpaRepository: TaskJpaRepository,
    @Autowired val taskAssignmentJpaRepository: TaskAssignmentJpaRepository,
) : BaseTaskAdapterTest(userJpaRepository) {
    private val createTask = CreateTask(
        currentUserId = Long.MIN_VALUE,
        title = "Task 제목",
        description = "Task 설명",
        status = TaskStatus.IN_PROGRESS,
        priority = TaskPriority.LOW,
        startAt = OffsetDateTime.parse("2025-01-02T00:00:00.000Z"),
        endAt = OffsetDateTime.parse("2020-01-03T00:00:00.000Z"),
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.parse("2025-01-01T00:00:00.000Z"),
    )

    @Test
    fun `Task 는 domainModel 의 데이터를 저장하고, 담당자가 없는 경우 Assignment는 저장되지 않는다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val domainModel = createTask.copy(currentUserId = creator.id)
        val savedTaskId = taskAdapter.create(domainModel)

        val savedTask = taskJpaRepository.findById(savedTaskId).orElseThrow()
        val savedAssignments = taskAssignmentJpaRepository.findAll()

        assertEquals(domainModel.title, savedTask.title)
        assertEquals(domainModel.description, savedTask.description)
        assertEquals(domainModel.status, savedTask.status)
        assertEquals(domainModel.priority, savedTask.priority)
        assertEquals(domainModel.startAt, savedTask.startAt)
        assertEquals(domainModel.endAt, savedTask.endAt)
        assertEquals(domainModel.createdBy, savedTask.creator.id)
        assertEquals(domainModel.createdAt, savedTask.createdAt)
        assertEquals(domainModel.updatedBy, savedTask.updater.id)
        assertEquals(domainModel.updatedAt, savedTask.updatedAt)
        assertEquals(0, savedAssignments.size)
    }

    @Test
    fun `Task와 Assignment가 정상적으로 연쇄 저장된다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val user1 = saveUser(UserJpaEntity(nickname = "담당자1"))
        val user2 = saveUser(UserJpaEntity(nickname = "담당자2"))
        val assigneeIds = listOf(user1.id, user2.id)
        val domainModel = createTask.copy(
            currentUserId = creator.id,
            assigneeIds = assigneeIds,
        )
        taskAdapter.create(domainModel)

        val savedAssignments = taskAssignmentJpaRepository.findAll()
        assertEquals(2, savedAssignments.size)
        assertEquals(assigneeIds, savedAssignments.map { it.id.userId })
        savedAssignments.first().let {
            assertEquals(domainModel.createdBy, it.creator.id)
            assertEquals(domainModel.createdAt, it.createdAt)
        }
    }
}

@DataJpaTestHelper
@Transactional
@DisplayName("Task 삭제 테스트")
class DeleteTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired val taskJpaRepository: TaskJpaRepository,
    @Autowired val taskAssignmentJpaRepository: TaskAssignmentJpaRepository,
) : BaseTaskAdapterTest(userJpaRepository) {
    private val createTask = CreateTask(
        title = "테스트 업무",
        status = TaskStatus.IN_PROGRESS,
        currentUserId = Long.MAX_VALUE,
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.now()
    )

    @Test
    fun `Task와 Assignment가 정상적으로 연쇄 삭제된다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val user1 = saveUser(UserJpaEntity(nickname = "담당자1"))
        val user2 = saveUser(UserJpaEntity(nickname = "담당자2"))
        val savedTaskId = taskAdapter.create(
            createTask.copy(
                currentUserId = creator.id,
                assigneeIds = listOf(user1.id, user2.id),
            )
        )

        assertEquals(1, taskJpaRepository.count())
        assertEquals(2, taskAssignmentJpaRepository.count())

        taskAdapter.delete(savedTaskId)

        assertEquals(0, taskJpaRepository.count())
        assertEquals(0, taskAssignmentJpaRepository.count())
    }

    @Test
    fun `담당자가 없는 경우 Task만 삭제된다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val savedTaskId = taskAdapter.create(
            createTask.copy(
                currentUserId = creator.id,
                assigneeIds = emptyList(),
            )
        )

        assertEquals(1, taskJpaRepository.count())
        assertEquals(0, taskAssignmentJpaRepository.count())

        taskAdapter.delete(savedTaskId)

        assertEquals(0, taskJpaRepository.count())
        assertEquals(0, taskAssignmentJpaRepository.count())
    }

    @Test
    fun `Task 존재 여부 확인하기`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val savedTaskId = taskAdapter.create(createTask.copy(currentUserId = creator.id))
        val notSavedTaskId = savedTaskId + 1

        assertTrue(taskAdapter.exists(savedTaskId))
        assertFalse(taskAdapter.exists(notSavedTaskId))
    }
}

@DataJpaTestHelper
@Transactional
@DisplayName("Task 조회 테스트")
class GetTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired val taskJpaRepository: TaskJpaRepository,
    @Autowired val taskAssignmentJpaRepository: TaskAssignmentJpaRepository,
) : BaseTaskAdapterTest(userJpaRepository) {
    private val createTask = CreateTask(
        title = "테스트 업무",
        status = TaskStatus.IN_PROGRESS,
        currentUserId = Long.MAX_VALUE,
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.now()
    )

    @Test
    fun `담당자가 없는 경우 Task만 조회된다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val task = createTask.copy(
            currentUserId = creator.id,
            assigneeIds = emptyList(),
        )

        val savedTaskId = taskAdapter.create(task)
        val foundTask = taskAdapter.get(savedTaskId)!!

        assertEquals(task.title, foundTask.title)
        assertEquals(task.description, foundTask.description)
        assertEquals(task.status, foundTask.status)
        assertEquals(task.priority, foundTask.priority)
        assertEquals(task.startAt, foundTask.startAt)
        assertEquals(task.endAt, foundTask.endAt)

        assertEquals(0, foundTask.assignees.count())
    }

    @Test
    fun `Task 담당자는 영어, 한글 순으로 정렬한다`() {
        val creator = saveUser(UserJpaEntity(nickname = "김 생성자"))
        val user1 = saveUser(UserJpaEntity(nickname = "홍 담당자1"))
        val user2 = saveUser(UserJpaEntity(nickname = "윤 담당자2"))
        val user3 = saveUser(UserJpaEntity(nickname = "Jay"))
        val user4 = saveUser(UserJpaEntity(nickname = "jhon"))
        val user5 = saveUser(UserJpaEntity(nickname = "aime"))
        val task = createTask.copy(
            currentUserId = creator.id,
            assigneeIds = listOf(creator.id, user1.id, user2.id, user3.id, user4.id, user5.id),
        )

        val savedTaskId = taskAdapter.create(task)
        val foundTask = taskAdapter.get(savedTaskId)!!

        assertEquals(
            listOf("aime", "Jay", "jhon", "김 생성자", "윤 담당자2", "홍 담당자1"),
            foundTask.assignees.map { it.name },
            "영어 대소문자 구분 없이 알파벳 -> 한글 순으로 정렬 한다"
        )
    }

    @Test
    fun `Task 가 없는 경우 null 을 리턴한다`() {
        val notSavedTaskId = 99.toLong()
        assertNull(taskAdapter.get(notSavedTaskId))
    }
}

@DataJpaTestHelper
@Transactional
@DisplayName("Task 업데이트 테스트")
class UpdateTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired val taskJpaRepository: TaskJpaRepository,
    @Autowired val entityManager: TestEntityManager,
) : BaseTaskAdapterTest(userJpaRepository) {
    private val createTask = CreateTask(
        title = "테스트 업무",
        status = TaskStatus.IN_PROGRESS,
        currentUserId = Long.MAX_VALUE,
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.now()
    )

    @Test
    fun `Task 변경 시 updater, updatedAt 도 변경되어야 한다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val updater = saveUser(UserJpaEntity(nickname = "수정자"))
        val createdAt = OffsetDateTime.parse("2026-01-26T03:00:00Z")
        val updatedAt = OffsetDateTime.parse("2026-01-26T04:00:00Z")

        val savedTaskId = taskAdapter.create(
            createTask.copy(
                currentUserId = creator.id,
                assigneeIds = emptyList(),
                createdAt = createdAt
            )
        )

        val savedTask = taskJpaRepository.findByIdOrNull(savedTaskId)!!

        assertEquals("테스트 업무", savedTask.title)
        assertEquals(createdAt, savedTask.updatedAt)
        assertEquals(creator.id, savedTask.updater.id)

        taskAdapter.update(
            UpdateTask(
                version = savedTask.version,
                taskId = savedTaskId,
                currentUserId = updater.id,
                updatedAt = updatedAt,
                title = "제목을 변경함"
            ).merge(taskAdapter.get(savedTaskId)!!),
        )

        assertEquals("제목을 변경함", savedTask.title)
        assertEquals(updatedAt, savedTask.updatedAt)
        assertEquals(updater.id, savedTask.updater.id)
    }

    @Test
    fun `Task 담장자를 변경할 수 있어야 한다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val user1 = saveUser(UserJpaEntity(nickname = "담당자1"))
        val user2 = saveUser(UserJpaEntity(nickname = "담당자2"))
        val user3 = saveUser(UserJpaEntity(nickname = "담당자3"))

        val beforeAssigneeIds = listOf(user1.id, user2.id)
        val afterAssigneeIds = listOf(user1.id, user3.id)

        val savedTaskId = taskAdapter.create(
            createTask.copy(
                currentUserId = creator.id,
                assigneeIds = beforeAssigneeIds,
            )
        )

        val savedTask = taskJpaRepository.findByIdOrNull(savedTaskId)!!
        assertEquals(2, savedTask.assignments.size)
        assertEquals("담당자1", savedTask.assignments[0].user.nickname)
        assertEquals("담당자2", savedTask.assignments[1].user.nickname)

        taskAdapter.update(
            UpdateTask(
                version = savedTask.version,
                taskId = savedTaskId,
                currentUserId = creator.id,
                updatedAt = OffsetDateTime.now(),
                assigneeIds = afterAssigneeIds,
            ).merge(taskAdapter.get(savedTaskId)!!),
        )

        assertEquals(2, savedTask.assignments.size)
        assertEquals("담당자1", savedTask.assignments[0].user.nickname)
        assertEquals("담당자3", savedTask.assignments[1].user.nickname)
    }

    @Test
    fun `Task 변경은 낙관적 락을 사용한다`() {
        val creator = saveUser(UserJpaEntity(nickname = "생성자"))
        val savedTaskId = taskAdapter.create(
            createTask.copy(
                currentUserId = creator.id,
                assigneeIds = emptyList(),
            )
        )
        val sameVersion = 0L

        taskAdapter.update(
            UpdateTask(
                version = sameVersion,
                taskId = savedTaskId,
                currentUserId = creator.id,
                updatedAt = OffsetDateTime.now(),
                title = "사용자 A 가 먼저 수정함"
            ).merge(taskAdapter.get(savedTaskId)!!),
        )
        entityManager.flush()
        entityManager.clear()

        assertThrows<ResourceConflictException> {
            taskAdapter.update(
                UpdateTask(
                    version = sameVersion,
                    taskId = savedTaskId,
                    currentUserId = creator.id,
                    updatedAt = OffsetDateTime.now(),
                    title = "사용자 B 가 나중에 수정함"
                ).merge(taskAdapter.get(savedTaskId)!!),
            )
            entityManager.flush()
            
        }
    }
}

abstract class BaseTaskAdapterTest(
    protected val userJpaRepository: UserJpaRepository,
) {
    protected fun saveUser(user: UserJpaEntity): UserJpaEntity {
        userJpaRepository.save(user)
        return user
    }
}