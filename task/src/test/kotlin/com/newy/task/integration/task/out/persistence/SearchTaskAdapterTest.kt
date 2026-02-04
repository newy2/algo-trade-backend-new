package com.newy.task.integration.task.out.persistence

import com.newy.task.integration.helper.DataJpaTestHelper
import com.newy.task.task.adapter.out.persistence.SearchTaskAdapter
import com.newy.task.task.adapter.out.persistence.TaskAdapter
import com.newy.task.task.adapter.out.persistence.jpa.UserJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.SearchTask
import com.newy.task.task.domain.TaskStatus.DONE
import com.newy.task.task.domain.TaskStatus.IN_PROGRESS
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@DataJpaTestHelper
@Transactional
@DisplayName("Task 검색 테스트")
class SearchTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired val searchTaskAdapter: SearchTaskAdapter,
) : BaseTaskAdapterTest(userJpaRepository) {
    private val createTask = CreateTask(
        title = "abc",
        status = IN_PROGRESS,
        currentUserId = Long.MAX_VALUE,
        assigneeIds = emptyList(),
        createdAt = OffsetDateTime.now()
    )

    private val searchTask = SearchTask(page = 0, size = 2)

    @Test
    fun `검색어와 필터가 없으면 전체 데이터를 페이징 처리만 한다`() {
        val user1 = saveUser(UserJpaEntity(nickname = "홍길동"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "PG 연동 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "알림 전송 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "피자 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "신발 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "비타민 먹기"))

        val result = searchTaskAdapter.search(searchTask)

        assertEquals(5, result.totalElements)
        assertEquals(3, result.totalPages)
        assertEquals(2, result.content.size)
        assertEquals("비타민 먹기", result.content[0].title)
        assertEquals("신발 주문하기", result.content[1].title)
    }

    @Test
    fun `Task 제목으로 검색할 수 있다`() {
        val user1 = saveUser(UserJpaEntity(nickname = "홍길동"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "PG 연동 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "알림 전송 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "피자 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "신발 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "비타민 먹기"))

        val result = searchTaskAdapter.search(searchTask.copy(keyword = "작업"))

        // TODO fix mysql
        assertEquals(2, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(2, result.content.size)
        assertEquals("알림 전송 작업", result.content[0].title)
        assertEquals("PG 연동 작업", result.content[1].title)
    }

    @Test
    fun `Task 설명으로 검색할 수 있다`() {
        val user1 = saveUser(UserJpaEntity(nickname = "홍길동"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, description = "PG 연동 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, description = "알림 전송 작업"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, description = "피자 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, description = "신발 주문하기"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, description = "비타민 먹기"))

        val result = searchTaskAdapter.search(searchTask.copy(keyword = "작업"))

        // TODO fix mysql
        assertEquals(2, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(2, result.content.size)
        assertEquals("알림 전송 작업", result.content[0].description)
        assertEquals("PG 연동 작업", result.content[1].description)
    }

    @Test
    fun `Task 담당자 이름으로 검색할 수 있다`() {
        val user1 = saveUser(UserJpaEntity(nickname = "홍길동"))
        val user2 = saveUser(UserJpaEntity(nickname = "김길동"))
        val user3 = saveUser(UserJpaEntity(nickname = "미스터 박"))

        val c = user1.id // linter 가 자동 줄바꿈해서 createdBy 를 c 로 축약해서 사용함
        createTaskForSearch(createTask.copy(currentUserId = c, title = "A", assigneeIds = listOf(user1.id)))
        createTaskForSearch(createTask.copy(currentUserId = c, title = "B", assigneeIds = listOf(user2.id)))
        createTaskForSearch(createTask.copy(currentUserId = c, title = "C", assigneeIds = listOf(user3.id)))
        createTaskForSearch(createTask.copy(currentUserId = c, title = "D", assigneeIds = listOf(user1.id, user3.id)))

        val result = searchTaskAdapter.search(searchTask.copy(keyword = "길동"))

        // TODO fix mysql
        assertEquals(3, result.totalElements)
        assertEquals(2, result.totalPages)
        assertEquals(2, result.content.size)
        assertEquals("D", result.content[0].title)
        assertEquals("B", result.content[1].title)
    }

    @Test
    fun `검색어와 필터는 AND 조건으로 사용한다`() {
        val user1 = saveUser(UserJpaEntity(nickname = "홍길동"))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "PG 연동 작업", status = DONE))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "알림 전송 작업", status = IN_PROGRESS))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "피자 주문하기", status = IN_PROGRESS))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "신발 주문하기", status = IN_PROGRESS))
        createTaskForSearch(createTask.copy(currentUserId = user1.id, title = "비타민 먹기", status = IN_PROGRESS))

        val result = searchTaskAdapter.search(searchTask.copy(keyword = "작업", status = IN_PROGRESS))

        // TODO fix mysql
        assertEquals(1, result.totalElements)
        assertEquals(1, result.totalPages)
        assertEquals(1, result.content.size)
        assertEquals("알림 전송 작업", result.content[0].title)
    }

    private fun createTaskForSearch(createTask: CreateTask) {
        searchTaskAdapter.index(taskAdapter.create(createTask))
    }
}