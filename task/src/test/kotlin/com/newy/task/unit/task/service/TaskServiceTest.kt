package com.newy.task.unit.task.service

import com.newy.task.common.error.NotFoundResourceException
import com.newy.task.notification.port.out.CreateNotificationOutPort
import com.newy.task.task.port.`in`.model.CreateTaskCommand
import com.newy.task.task.port.`in`.model.SearchTaskQuery
import com.newy.task.task.port.`in`.model.TaskId
import com.newy.task.task.port.`in`.model.UpdateTaskCommand
import com.newy.task.task.service.TaskService
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.SearchResult
import com.newy.task.task.domain.SearchTask
import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskAssignee
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.domain.UpdateTask
import com.newy.task.task.port.out.CreateTaskOutPort
import com.newy.task.task.port.out.DeleteTaskOutPort
import com.newy.task.task.port.out.ExistsTaskOutPort
import com.newy.task.task.port.out.FindUserIdsOutPort
import com.newy.task.task.port.out.GetTaskOutPort
import com.newy.task.task.port.out.IndexSearchTaskOutPort
import com.newy.task.task.port.out.ReIndexSearchTaskOutPort
import com.newy.task.task.port.out.SearchTaskOutPort
import com.newy.task.task.port.out.UpdateTaskOutPort
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertEquals


abstract class BaseTaskServiceTest {
    protected fun createService(
        userIdsOutPort: FindUserIdsOutPort = FindUserIdsOutPort { listOf(1, 2) },
        existsTaskOutPort: ExistsTaskOutPort = ExistsTaskOutPort { true },
        createTaskOutPort: CreateTaskOutPort = CreateTaskOutPort { Long.MIN_VALUE },
        deleteTaskOutPort: DeleteTaskOutPort = DeleteTaskOutPort { },
        getTaskOutPort: GetTaskOutPort = GetTaskOutPort { Task(version = 0) },
        updateTaskOutPort: UpdateTaskOutPort = UpdateTaskOutPort { },
        searchTaskOutPort: SearchTaskOutPort = SearchTaskOutPort {
            SearchResult(
                content = emptyList(),
                pageNumber = 0,
                totalPages = 0,
                totalElements = 0,
            )
        },
        indexSearchTaskOutPort: IndexSearchTaskOutPort = IndexSearchTaskOutPort { },
        reIndexSearchTaskOutPort: ReIndexSearchTaskOutPort = ReIndexSearchTaskOutPort { },
        createNotificationOutPort: CreateNotificationOutPort = CreateNotificationOutPort { _, _, _ -> },
    ) = TaskService(
        findUserIdsOutPort = userIdsOutPort,
        existsTaskOutPort = existsTaskOutPort,
        createTaskOutPort = createTaskOutPort,
        deleteTaskOutPort = deleteTaskOutPort,
        getTaskOutPort = getTaskOutPort,
        updateTaskOutPort = updateTaskOutPort,
        searchTaskOutPort = searchTaskOutPort,
        indexSearchTaskOutPort = indexSearchTaskOutPort,
        reIndexSearchTaskOutPort = reIndexSearchTaskOutPort,
        createNotificationOutPort = createNotificationOutPort,
    )
}

@DisplayName("Service - Task 생성 테스트")
class CreateTaskServiceTest : BaseTaskServiceTest() {
    private val inPortModel = CreateTaskCommand(
        currentUserId = 1,
        title = "제목",
        status = "WAITING"
    )

    @Test
    fun `InPortModel 을 DomainModel 로 변환하여 CreateTaskOutPort 에 전달해야 한다`() {
        var captured: CreateTask? = null
        createService(createTaskOutPort = { p -> 0L.also { captured = p } }).create(inPortModel)
        assertEquals(inPortModel.toDomainModel(), captured)
    }

    @Test
    fun `Service 는 CreateTaskOutPort 의 반환 값을 반환한다`() {
        assertEquals(123, createService(createTaskOutPort = { 123 }).create(inPortModel))
    }

    @Test
    fun `Task 를 생성한 후, IndexSearchTaskOutPort 를 호출해야 한다`() {
        var log = ""
        createService(
            createTaskOutPort = { p -> 123L.also { log += "createTask " } },
            indexSearchTaskOutPort = { taskId -> log += "indexTask(taskId: $taskId) " },
        ).create(inPortModel)
        assertEquals("createTask indexTask(taskId: 123) ", log)
    }

    @Test
    fun `Task 를 생성한 후, assigneeIds 가 있는 경우에 CreateNotificationOutPort 를 호출해야 한다`() {
        var log = ""
        createService(
            createTaskOutPort = { p -> 123L.also { log += "createTask " } },
            createNotificationOutPort = { taskId, assigneeIds, type ->
                log += "createNotify(taskId: $taskId, assigneeIds: $assigneeIds, type: $type) "
            }
        ).create(inPortModel.copy(assigneeIds = listOf(1)))

        assertEquals(
            "createTask createNotify(taskId: 123, assigneeIds: [1], type: TASK_ASSIGNED) ",
            log
        )
    }

    @Test
    fun `Task 에 assigneeIds 가 없는 경우 CreateNotificationOutPort 를 호출하지 않는다`() {
        var log = ""
        createService(
            createTaskOutPort = { p -> 123L.also { log += "createTask " } },
            createNotificationOutPort = { _, _, _ -> log += "MUST_NOT_BE_CALLED " },
        ).create(inPortModel.copy(assigneeIds = emptyList()))
        assertEquals("createTask ", log)
    }

    @Test
    fun `DB 에 저장되지 않은 assigneeIds 전달한 경우 에러가 발생한다`() {
        val savedUserIds = listOf<Long>(1)
        val notSavedUserIds = listOf<Long>(2)
        val requestAssigneeIds = savedUserIds + notSavedUserIds

        val exception = assertThrows<IllegalArgumentException> {
            createService(userIdsOutPort = { savedUserIds })
                .create(inPortModel.copy(assigneeIds = requestAssigneeIds))
        }
        assertEquals("존재하지 않는 사용자 ID가 포함되어 있습니다: [2]", exception.message)
    }
}

@DisplayName("Service - Task 삭제 테스트")
class DeleteTaskServiceTest : BaseTaskServiceTest() {
    private val inPortModel = TaskId(
        value = 123,
    )

    @Test
    fun `DB 에 저장된 Task 가 없으면 에러가 발생한다`() {
        val exception = assertThrows<NotFoundResourceException> {
            createService(getTaskOutPort = { null }).get(inPortModel)
        }
        assertEquals("존재하지 않는 Task ID 입니다: 123", exception.message)
    }

    @Test
    fun `Service 는 InPortValue 의 값을 OutPort 에 전달해야 한다`() {
        var captured: Long = 0
        createService(deleteTaskOutPort = { captured = it }).delete(inPortModel)
        assertEquals(inPortModel.value, captured)
    }
}

@DisplayName("Service - Task 조회 테스트")
class GetTaskServiceTest : BaseTaskServiceTest() {
    private val inPortModel = TaskId(
        value = 123,
    )

    @Test
    fun `Task 를 찾을 수 없으면 에러가 발생한다`() {
        val exception = assertThrows<NotFoundResourceException> {
            createService(getTaskOutPort = { null }).get(inPortModel)
        }
        assertEquals("존재하지 않는 Task ID 입니다: 123", exception.message)
    }

    @Test
    fun `Service 는 InPortValue 의 값을 OutPort 에 전달해야 한다`() {
        var captured: Long = 0L
        createService(getTaskOutPort = { p -> Task().also { captured = p } }).get(inPortModel)
        assertEquals(inPortModel.value, captured)
    }
}


@DisplayName("Service - Task 업데이트 테스트")
class UpdateTaskServiceTest : BaseTaskServiceTest() {
    private val inPortModel = UpdateTaskCommand(
        taskId = 123,
        version = 0,
        currentUserId = 1,
        title = "Task 제목 변경",
    )

    private val savedTask = Task(
        id = 123,
        version = 0,
        title = "Task 제목",
        status = TaskStatus.IN_PROGRESS,
    )

    @Test
    fun `DB 에 저장되지 않은 TaskId 를 전달한 경우 에러가 발생한다`() {
        val exception = assertThrows<NotFoundResourceException> {
            createService(getTaskOutPort = { null }).update(inPortModel)
        }
        assertEquals("존재하지 않는 Task ID 입니다: 123", exception.message)
    }

    @Test
    fun `UpdateTask 와 DB 에 저장된 Task 를 merge 하여 UpdateTaskOutPort 에 전달해야 한다`() {
        var captured: UpdateTask? = null
        createService(
            getTaskOutPort = { savedTask },
            updateTaskOutPort = { captured = it },
        ).update(inPortModel)
        assertEquals(inPortModel.toDomainModel().merge(savedTask), captured!!)
    }

    @Test
    fun `Task 를 업데이트 한 후, ReIndexSearchTaskOutPort 를 호출해야 한다`() {
        var log = ""
        createService(
            updateTaskOutPort = { log += "updateTask " },
            reIndexSearchTaskOutPort = { taskId -> log += "reIndexTask(taskId: $taskId) " },
        ).update(inPortModel)
        assertEquals("updateTask reIndexTask(taskId: 123) ", log)
    }

    @Test
    fun `Task 를 업데이트 한 후, assigneeIds 가 있는 경우에 CreateNotificationOutPort 를 호출해야 한다`() {
        var log = ""
        createService(
            updateTaskOutPort = { log += "updateTask " },
            createNotificationOutPort = { taskId, assigneeIds, type ->
                log += "createNotify(taskId: $taskId, assigneeIds: $assigneeIds, type: $type) "
            }
        ).update(inPortModel.copy(assigneeIds = listOf(1)))
        assertEquals(
            "updateTask createNotify(taskId: 123, assigneeIds: [1], type: TASK_ASSIGNED) ",
            log
        )
    }

    @Test
    fun `Task 에 assigneeIds 가 없는 경우 CreateNotificationOutPort 를 호출하지 않는다`() {
        var log = ""
        createService(
            updateTaskOutPort = { log += "updateTask " },
            createNotificationOutPort = { _, _, _ -> log += "MUST_NOT_BE_CALLED " },
        ).update(inPortModel.copy(assigneeIds = emptyList()))
        assertEquals("updateTask ", log)
    }

    @Test
    fun `Task 에 assigneeIds 가 삭제된 경우 CreateNotificationOutPort 를 호출하지 않는다`() {
        val savedAssignees = listOf(TaskAssignee(id = 1, name = "홍길동"))
        val replaceAssigneeIds = emptyList<Long>()

        var log = ""
        createService(
            getTaskOutPort = { savedTask.copy(assignees = savedAssignees) },
            updateTaskOutPort = { log += "updateTask " },
            createNotificationOutPort = { _, _, _ -> log += "MUST_NOT_BE_CALLED " },
        ).update(inPortModel.copy(assigneeIds = replaceAssigneeIds))
        assertEquals("updateTask ", log)
    }

    @Test
    fun `Task 에 assigneeIds 가 추가된 경우, 추가된 assigneeIds 만 CreateNotificationOutPort 에 전달한다`() {
        val savedAssignees = listOf(TaskAssignee(id = 1, name = "홍길동"))
        val replaceAssigneeIds = listOf<Long>(1, 2)

        var log = ""
        createService(
            getTaskOutPort = { savedTask.copy(assignees = savedAssignees) },
            updateTaskOutPort = { log += "updateTask " },
            createNotificationOutPort = { _, assigneeIds, _ -> log += "createNotify(assigneeIds: $assigneeIds) " },
        ).update(inPortModel.copy(assigneeIds = replaceAssigneeIds))
        assertEquals("updateTask createNotify(assigneeIds: [2]) ", log)
    }

    @Test
    fun `DB 에 저장되지 않은 UserId 를 assigneeIds 전달한 경우 에러가 발생한다`() {
        val savedUserIds = listOf<Long>(1)
        val notSavedUserIds = listOf<Long>(2)
        val requestAssigneeIds = savedUserIds + notSavedUserIds

        val exception = assertThrows<IllegalArgumentException> {
            createService(userIdsOutPort = { savedUserIds })
                .update(inPortModel.copy(assigneeIds = requestAssigneeIds))
        }
        assertEquals("존재하지 않는 사용자 ID가 포함되어 있습니다: [2]", exception.message)
    }
}


@DisplayName("Service - Task 검색 테스트")
class SearchTaskServiceTest : BaseTaskServiceTest() {
    private val inPortModel = SearchTaskQuery(
        page = 0,
        size = 20,
        keyword = "검색어",
    )

    @Test
    fun `Service 는 InPortModel 을 DomainModel 로 변환하여 OutPort 에 전달해야 한다`() {
        var captured: SearchTask? = null
        createService(searchTaskOutPort = { p -> SearchResult().also { captured = p } }).search(inPortModel)
        assertEquals(inPortModel.toDomainModel(), captured!!)
    }
}

class TaskServiceAnnotationTest {
    @Test
    fun `Service 클래스는 아래 애너테이션을 사용해야 한다`() {
        assertTrue(TaskService::class.hasAnnotation<Service>())
    }

    @Test
    fun `Controller 클래스의 메서드는 아래 애너테이션을 사용해야 한다`() {
        TaskService::create.let {
            assertTrue(it.hasAnnotation<Transactional>())
            assertFalse(it.findAnnotation<Transactional>()?.readOnly!!)
        }

        TaskService::delete.let {
            assertTrue(it.hasAnnotation<Transactional>())
            assertFalse(it.findAnnotation<Transactional>()?.readOnly!!)
        }

        TaskService::get.let {
            assertTrue(it.hasAnnotation<Transactional>())
            assertTrue(it.findAnnotation<Transactional>()?.readOnly!!)
        }

        TaskService::update.let {
            assertTrue(it.hasAnnotation<Transactional>())
            assertFalse(it.findAnnotation<Transactional>()?.readOnly!!)
        }

        TaskService::search.let {
            assertTrue(it.hasAnnotation<Transactional>())
            assertTrue(it.findAnnotation<Transactional>()?.readOnly!!)
        }
    }
}