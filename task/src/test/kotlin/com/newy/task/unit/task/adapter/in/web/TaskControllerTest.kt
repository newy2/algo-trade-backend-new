package com.newy.task.unit.task.adapter.`in`.web

import com.newy.task.spring.auth.annotation.CurrentUser
import com.newy.task.spring.auth.annotation.LoginRequired
import com.newy.task.spring.auth.model.LoginUser
import com.newy.task.task.adapter.`in`.web.TaskController
import com.newy.task.task.domain.SearchResult
import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskStatus
import com.newy.task.task.port.`in`.model.CreateTaskCommand
import com.newy.task.task.port.`in`.model.SearchTaskQuery
import com.newy.task.task.port.`in`.model.TaskId
import com.newy.task.task.port.`in`.model.UpdateTaskCommand
import com.newy.task.task.adapter.`in`.web.model.CreateTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.CreateTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.GetTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.SearchTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.SearchTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.UpdateTaskWebRequest
import com.newy.task.task.port.`in`.CreateTaskInPort
import com.newy.task.task.port.`in`.DeleteTaskInPort
import com.newy.task.task.port.`in`.GetTaskInPort
import com.newy.task.task.port.`in`.SearchTaskInPort
import com.newy.task.task.port.`in`.UpdateTaskInPort
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findParameterByName
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertEquals

abstract class BaseTaskControllerTest {
    protected fun createController(
        createTaskInPort: CreateTaskInPort = CreateTaskInPort { Long.MIN_VALUE },
        deleteTaskInPort: DeleteTaskInPort = DeleteTaskInPort { null },
        getTaskInPort: GetTaskInPort = GetTaskInPort { Task() },
        updateTaskInPort: UpdateTaskInPort = UpdateTaskInPort { null },
        searchTaskInPort: SearchTaskInPort = SearchTaskInPort {
            SearchResult(
                content = emptyList(),
                pageNumber = 0,
                totalPages = 0,
                totalElements = 0,
            )
        },
    ) = TaskController(
        createTaskInPort = createTaskInPort,
        deleteTaskInPort = deleteTaskInPort,
        getTaskInPort = getTaskInPort,
        updateTaskInPort = updateTaskInPort,
        searchTaskInPort = searchTaskInPort,
    )
}

@DisplayName("Controller - Task 생성 테스트")
class CreateTaskControllerTest : BaseTaskControllerTest() {
    private val webRequest = object {
        val currentUser = LoginUser(id = 1, nickname = "홍길동")
        val requestBody = CreateTaskWebRequest(title = "제목", status = "WAITING")
    }

    @Test
    fun `Controller 는 WebRequest 를 InPortModel 로 변환하여 InPort 에 전달한다`() {
        var captured: CreateTaskCommand? = null
        createController(createTaskInPort = { p -> 0L.also { captured = p } })
            .create(webRequest.currentUser, webRequest.requestBody)

        assertEquals(
            webRequest.requestBody.toInPortModel(webRequest.currentUser.id).copy(createdAt = captured!!.createdAt),
            captured
        )
    }

    @Test
    fun `요청이 성공하면 201 상태를 응답한다`() {
        val response = createController(createTaskInPort = { 123L })
            .create(webRequest.currentUser, webRequest.requestBody)

        assertEquals(
            ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateTaskWebResponse(id = 123L, message = "Task가 생성되었습니다.")),
            response
        )
    }
}

@DisplayName("Controller - Task 삭제 테스트")
class DeleteTaskControllerTest : BaseTaskControllerTest() {
    private val webRequest = object {
        val taskId: Long = 123
    }

    @Test
    fun `Controller 는 WebRequest 를 InPortModel 로 변환하여 InPort 에 전달한다`() {
        var captured: TaskId? = null
        createController(deleteTaskInPort = { captured = it }).delete(webRequest.taskId)
        assertEquals(TaskId(123), captured)
    }

    @Test
    fun `요청이 성공하면 204 상태를 응답한다`() {
        assertEquals(ResponseEntity.noContent().build(), createController().delete(webRequest.taskId))
    }
}

@DisplayName("Controller - Task 상세 조회 테스트")
class GetTaskControllerTest : BaseTaskControllerTest() {
    private val webRequest = object {
        val taskId: Long = 123
    }

    @Test
    fun `Controller 는 WebRequest 를 InPortModel 로 변환하여 InPort 에 전달한다`() {
        var captured: TaskId? = null
        createController(getTaskInPort = { p -> Task().also { captured = p } }).get(webRequest.taskId)
        assertEquals(TaskId(123), captured)
    }

    @Test
    fun `요청이 성공하면 200 상태를 응답한다`() {
        val foundTask = Task(id = 123, title = "제목", status = TaskStatus.DONE)
        assertEquals(
            ResponseEntity.ok(GetTaskWebResponse.fromDomainModel(foundTask)),
            createController(getTaskInPort = { foundTask }).get(webRequest.taskId)
        )
    }
}

@DisplayName("Controller - Task 변경 테스트")
class UpdateTaskControllerTest : BaseTaskControllerTest() {
    private val webRequest = object {
        val currentUser = LoginUser(id = 1, nickname = "홍길동")
        val taskId: Long = 123
        val requestBody = UpdateTaskWebRequest(
            version = 22,
            title = "제목",
            description = "설명",
            status = "WAITING",
            priority = "LOW",
            startedAt = "2026-01-26T03:00:00Z",
            endedAt = "2026-01-27T03:00:00Z",
            assigneeIds = listOf<Long>(200, 300),
        )
    }


    @Test
    fun `Controller 는 SpringWebRequestModel 를 InPortModel 로 변환하여 InPort 에 전달해야 한다`() {
        var captured: UpdateTaskCommand? = null
        createController(
            updateTaskInPort = UpdateTaskInPort { captured = it }
        ).update(
            currentUser = webRequest.currentUser,
            taskId = webRequest.taskId,
            requestBody = webRequest.requestBody
        )

        val expected = webRequest.requestBody
            .toInPortModel(currentUserId = webRequest.currentUser.id, taskId = webRequest.taskId)
            .copy(updatedAt = captured!!.updatedAt)
        assertEquals(expected, captured)
    }

    @Test
    fun `요청이 성공하면 204 상태를 응답한다`() {
        val controller = createController()
        assertEquals(
            ResponseEntity.noContent().build(),
            controller.update(
                currentUser = webRequest.currentUser,
                taskId = webRequest.taskId,
                requestBody = webRequest.requestBody
            )
        )
    }
}


@DisplayName("Controller - Task 검색 테스트")
class SearchTaskControllerTest : BaseTaskControllerTest() {
    private val webRequest = object {
        val request = SearchTaskWebRequest(
            keyword = "검색어",
            status = "WAITING",
            priority = "LOW",
            startAt = "2026-01-26T03:00:00Z",
            endAt = "2026-01-27T03:00:00Z",
            page = 0,
            size = 5,
        )
    }

    @Test
    fun `Controller 는 SpringWebRequestModel 를 InPortModel 로 변환하여 InPort 에 전달해야 한다`() {
        var captured: SearchTaskQuery? = null
        createController(searchTaskInPort = { p -> SearchResult().also { captured = p } })
            .search(webRequest.request)

        assertEquals(webRequest.request.toInPortModel(), captured)
    }

    @Test
    fun `요청이 성공하면 200 상태를 응답한다`() {
        OffsetDateTime.now()
        val searchResult = SearchResult(
            content = emptyList(),
            pageNumber = Int.MIN_VALUE,
            totalPages = Int.MIN_VALUE,
            totalElements = Long.MIN_VALUE,
        )

        val controller = createController(searchTaskInPort = { searchResult })

        assertEquals(
            ResponseEntity.ok(SearchTaskWebResponse.fromDomainModel(searchResult)),
            controller.search(webRequest.request)
        )
    }
}

class TaskControllerAnnotationTest {
    @Test
    fun `Controller 클래스는 아래 애너테이션을 사용해야 한다`() {
        TaskController::class.let {
            assertTrue(it.hasAnnotation<RestController>())
            assertTrue(it.hasAnnotation<LoginRequired>())
            assertEquals("/api/v1", it.findAnnotation<RequestMapping>()?.value?.first())
        }
    }

    @Test
    fun `Controller 클래스의 메서드는 아래 애너테이션을 사용해야 한다`() {
        TaskController::create.let {
            assertEquals("/tasks", it.findAnnotation<PostMapping>()?.value?.first())
            assertTrue(it.findParameterByName("currentUser")?.hasAnnotation<CurrentUser>()!!)
            assertTrue(it.findParameterByName("requestBody")?.hasAnnotation<RequestBody>()!!)
        }

        TaskController::delete.let {
            assertEquals("/tasks/{taskId}", it.findAnnotation<DeleteMapping>()?.value?.first())
            assertTrue(it.findParameterByName("taskId")?.hasAnnotation<PathVariable>()!!)
        }

        TaskController::get.let {
            assertEquals("/tasks/{taskId}", it.findAnnotation<GetMapping>()?.value?.first())
            assertTrue(it.findParameterByName("taskId")?.hasAnnotation<PathVariable>()!!)
        }

        TaskController::update.let {
            assertEquals("/tasks/{taskId}", it.findAnnotation<PatchMapping>()?.value?.first())
            assertTrue(it.findParameterByName("currentUser")?.hasAnnotation<CurrentUser>()!!)
            assertTrue(it.findParameterByName("taskId")?.hasAnnotation<PathVariable>()!!)
            assertTrue(it.findParameterByName("requestBody")?.hasAnnotation<RequestBody>()!!)
        }

        TaskController::search.let {
            assertEquals("/tasks", it.findAnnotation<GetMapping>()?.value?.first())
            assertNotNull(it.findParameterByName("request"))
        }
    }
}