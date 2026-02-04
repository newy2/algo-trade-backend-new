package com.newy.task.integration.notification.out.persistence

import com.newy.task.integration.helper.DataJpaTestHelper
import com.newy.task.notification.adapter.out.persistence.NotificationAdapter
import com.newy.task.notification.adapter.out.persistence.jpa.NotificationOutboxJpaRepository
import com.newy.task.notification.adapter.out.persistence.jpa.UserNotificationConfigJpaRepository
import com.newy.task.notification.adapter.out.persistence.jpa.model.UserNotificationConfigJpaEntity
import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationStatus
import com.newy.task.notification.domain.NotificationType.TASK_ASSIGNED
import com.newy.task.notification.domain.NotificationType.TASK_DEADLINE_IMMINENT
import com.newy.task.task.adapter.out.persistence.TaskAdapter
import com.newy.task.task.adapter.out.persistence.jpa.UserJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.TaskStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import kotlin.test.assertEquals

open class BaseNotificationAdapterTest(
    protected val userNotificationConfigRepository: UserNotificationConfigJpaRepository,
    protected val userJpaRepository: UserJpaRepository,
) {
    protected fun saveUserNotificationConfig(user: UserJpaEntity): UserJpaEntity =
        user.also {
            userNotificationConfigRepository.save(
                UserNotificationConfigJpaEntity(user = user, channelType = NotificationChannelType.LOG)
            )
        }

    protected fun saveUser(user: UserJpaEntity): UserJpaEntity {
        userJpaRepository.save(user)
        return user
    }
}

@DataJpaTestHelper
@Transactional
@DisplayName("알림 메세지 생성 & 조회 테스트")
class CreateAndGetPendingNotificationAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val notificationAdapter: NotificationAdapter,
    @Autowired userNotificationConfigJpaRepository: UserNotificationConfigJpaRepository,
    @Autowired val entityManager: TestEntityManager,
    @Autowired val notificationOutboxJpaRepository: NotificationOutboxJpaRepository
) : BaseNotificationAdapterTest(userNotificationConfigJpaRepository, userJpaRepository) {
    @Test
    fun `사용자의 UserNotificationConfig 가 있으면 Notification 을 생성한다`() {
        val user = saveUserNotificationConfig(saveUser(UserJpaEntity(nickname = "생성자")))
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        assertEquals(1, notificationAdapter.getPendingNotifications().size)
    }

    @Test
    fun `사용자의 UserNotificationConfig 가 없으면 Notification 을 생성하지 않는다`() {
        val user = saveUser(UserJpaEntity(nickname = "생성자"))
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        assertEquals(0, notificationAdapter.getPendingNotifications().size)
    }

    @Test
    fun `Notification 는 중복 생성 되지 않는다`() {
        val user = saveUserNotificationConfig(saveUser(UserJpaEntity(nickname = "생성자")))
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        assertEquals(1, notificationAdapter.getPendingNotifications().size)
    }

    @Test
    fun `Notification 은 과거 데이터(id ASC) 부터 조회한다`() {
        val user = saveUserNotificationConfig(saveUser(UserJpaEntity(nickname = "생성자")))
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        notificationAdapter.create(taskId = 456, assigneeIds = listOf(user.id), type = TASK_DEADLINE_IMMINENT)
        notificationAdapter.create(taskId = 789, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        val notifications = notificationAdapter.getPendingNotifications(size = 2)

        assertEquals(2, notifications.size)
        assertEquals("새로운 Task에 할당되었습니다. (Task ID: 123)", notifications[0].payload.message)
        assertEquals("Task 마감일 1일 전입니다. (Task ID: 456)", notifications[1].payload.message)
    }

    @Test
    fun `Notification 상태를 업데이트 한다`() {
        val user = saveUserNotificationConfig(saveUser(UserJpaEntity(nickname = "생성자")))
        notificationAdapter.create(taskId = 123, assigneeIds = listOf(user.id), type = TASK_ASSIGNED)
        var notification = notificationAdapter.getPendingNotifications().first()

        val jpaEntities = notificationOutboxJpaRepository.findByIdOrNull(notification.id)!!
        assertEquals(0, jpaEntities.retryCount)
        assertEquals(NotificationStatus.READY, jpaEntities.status)

        notificationAdapter.update(notification.copy().success())
        assertEquals(0, jpaEntities.retryCount)
        assertEquals(NotificationStatus.SENT, jpaEntities.status)

        notificationAdapter.update(notification.copy(retryCount = Notification.MAX_RETRY_COUNT).fail())
        assertEquals(Notification.MAX_RETRY_COUNT + 1, jpaEntities.retryCount)
        assertEquals(NotificationStatus.FAIL, jpaEntities.status)
    }
}

@DataJpaTestHelper
@Transactional
@DisplayName("마감일 1일전 Task 조회 테스트")
class GetDeadlineTaskAdapterTest(
    @Autowired userJpaRepository: UserJpaRepository,
    @Autowired val notificationAdapter: NotificationAdapter,
    @Autowired val taskAdapter: TaskAdapter,
    @Autowired userNotificationConfigJpaRepository: UserNotificationConfigJpaRepository,
) : BaseNotificationAdapterTest(userNotificationConfigJpaRepository, userJpaRepository) {
    private val createTask = CreateTask(
        currentUserId = Long.MAX_VALUE,
        title = "Task 제목",
        status = TaskStatus.IN_PROGRESS,
        createdAt = OffsetDateTime.now()
    )

    @Test
    fun `마감 시각이 5분 전부터 마감 시각까지인 Task 만 알림 대상이다`() {
        val user = saveUser(UserJpaEntity(nickname = "생성자"))
        val endAt = OffsetDateTime.parse("2026-01-10T00:00:00Z")
        taskAdapter.create(createTask.copy(currentUserId = user.id, endAt = endAt))

        val notifyWindowEnd = endAt.plusMinutes(5)
        assertEquals(0, notificationAdapter.getDeadlineTasks(deadline = endAt.minusSeconds(1)).size)
        assertEquals(1, notificationAdapter.getDeadlineTasks(deadline = endAt).size)
        assertEquals(1, notificationAdapter.getDeadlineTasks(deadline = notifyWindowEnd).size)
        assertEquals(0, notificationAdapter.getDeadlineTasks(deadline = notifyWindowEnd.plusSeconds(1)).size)
    }

    @Test
    fun `완료된 Task 는 알림 대상이 아니다`() {
        val user = saveUser(UserJpaEntity(nickname = "생성자"))
        val endAt = OffsetDateTime.parse("2026-01-10T00:00:00Z")
        taskAdapter.create(createTask.copy(currentUserId = user.id, endAt = endAt, status = TaskStatus.DONE))

        assertEquals(0, notificationAdapter.getDeadlineTasks(deadline = endAt).size)
    }

    @Test
    fun `마감 시각이 없는 Task 는 알림 대상이 아니다`() {
        val user = saveUser(UserJpaEntity(nickname = "생성자"))
        val endAt = OffsetDateTime.parse("2026-01-10T00:00:00Z")
        taskAdapter.create(createTask.copy(currentUserId = user.id, endAt = null))

        assertEquals(0, notificationAdapter.getDeadlineTasks(deadline = endAt).size)
    }
}