package com.newy.task.unit.notification.service

import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationPayload
import com.newy.task.notification.domain.NotificationStatus
import com.newy.task.notification.service.NotificationService
import com.newy.task.task.domain.Task
import com.newy.task.task.domain.TaskAssignee
import com.newy.task.notification.port.out.CreateNotificationOutPort
import com.newy.task.notification.port.out.GetDeadlineTasksOutPort
import com.newy.task.notification.port.out.GetPendingNotificationsOutPort
import com.newy.task.notification.port.out.SendNotificationOutPort
import com.newy.task.notification.port.out.UpdateNotificationOutPort
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import kotlin.test.assertEquals

open class NotificationServiceTest {
    protected val now = OffsetDateTime.parse("2026-01-26T04:00:00Z")
    protected fun createService(
        getPendingNotificationsOutPort: GetPendingNotificationsOutPort = GetPendingNotificationsOutPort { emptyList<Notification>() },
        updateNotificationOutPort: UpdateNotificationOutPort = UpdateNotificationOutPort {},
        getDeadlineTasksOutPort: GetDeadlineTasksOutPort = GetDeadlineTasksOutPort { emptyList() },
        createNotificationOutPort: CreateNotificationOutPort = CreateNotificationOutPort { _, _, _ -> },
        sendNotificationOutPorts: List<SendNotificationOutPort> = emptyList(),
    ) =
        NotificationService(
            getPendingNotificationsOutPort = getPendingNotificationsOutPort,
            updateNotificationOutPort = updateNotificationOutPort,
            getDeadlineTasksOutPort = getDeadlineTasksOutPort,
            createNotificationOutPort = createNotificationOutPort,
            sendNotificationOutPorts = sendNotificationOutPorts,
        )
}

@DisplayName("알림 전송 Service 테스트")
class SendNotificationServiceTest : NotificationServiceTest() {
    val notification = Notification(
        id = 1,
        payload = NotificationPayload(
            message = "메세지"
        ),
        status = NotificationStatus.READY,
        channelType = NotificationChannelType.APP,
        retryCount = 0,
    )

    @Test
    fun `Sender 를 찾을 수 없는 경우 알린 전송이 실패한다`() {
        var captured: Notification? = null
        val noSender = emptyList<SendNotificationOutPort>()
        createService(
            getPendingNotificationsOutPort = { listOf(notification) },
            updateNotificationOutPort = { captured = it },
            sendNotificationOutPorts = noSender,
        ).sendNotification(now)

        assertEquals(notification.fail(now), captured)
    }

    @Test
    fun `Sender 에서 에러가 발생하면 알린 전송이 실패한다`() {
        var captured: Notification? = null
        val failSender = listOf(
            object : SendNotificationOutPort {
                override fun supports(channelType: NotificationChannelType) = true
                override fun send(payload: NotificationPayload) = throw Exception("send failed")
            }
        )
        createService(
            getPendingNotificationsOutPort = { listOf(notification) },
            updateNotificationOutPort = { captured = it },
            sendNotificationOutPorts = failSender,
        ).sendNotification(now)

        assertEquals(notification.fail(now), captured)
    }

    @Test
    fun `알림 전송이 성공한다`() {
        var captured: Notification? = null
        val failSender = listOf(
            object : SendNotificationOutPort {
                override fun supports(channelType: NotificationChannelType) = true
                override fun send(payload: NotificationPayload) {}
            }
        )
        createService(
            getPendingNotificationsOutPort = { listOf(notification) },
            updateNotificationOutPort = { captured = it },
            sendNotificationOutPorts = failSender,
        ).sendNotification(now)

        assertEquals(notification.success(now), captured)
    }
}

@DisplayName("마감일 임박 Task 알림 생성 Service 테스트")
class CreateDeadlineTaskNotificationServiceTest : NotificationServiceTest() {
    @Test
    fun `마감일 1일 전 Task를 조회한다`() {
        var captured: OffsetDateTime? = null
        createService(
            getDeadlineTasksOutPort = { p -> emptyList<Task>().also { captured = p } },
        ).createDeadlineTaskNotification(now)

        assertEquals(now.plusDays(1), captured)
    }

    @Test
    fun `Task assignee 가 있는 경우에 알림 전송 요청 메세지를 등록한다`() {
        var log: String = ""
        createService(
            getDeadlineTasksOutPort = {
                listOf(Task(id = 123, assignees = listOf(TaskAssignee(id = 1, name = "홍길동"))))
            },
            createNotificationOutPort = { taskId, assigneeIds, type ->
                log += "createNotify(taskId: $taskId, assigneeIds: $assigneeIds, type: $type) "
            }
        ).createDeadlineTaskNotification(now)

        assertEquals("createNotify(taskId: 123, assigneeIds: [1], type: TASK_DEADLINE_IMMINENT) ", log)
    }

    @Test
    fun `Task assignee 가 있는 경우에 알림 전송 요청 메세지를 등록하지 않는다`() {
        var log: String = ""
        createService(
            getDeadlineTasksOutPort = {
                listOf(Task(id = 123, assignees = emptyList()))
            },
            createNotificationOutPort = { _, _, _ -> log += "MUST_NOT_BE_CALLED " }
        ).createDeadlineTaskNotification(now)

        assertEquals("", log)
    }
}