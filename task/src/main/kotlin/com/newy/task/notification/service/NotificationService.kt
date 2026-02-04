package com.newy.task.notification.service

import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationType
import com.newy.task.notification.port.`in`.CreateDeadlineTaskNotificationInPort
import com.newy.task.notification.port.`in`.SendNotificationInPort
import com.newy.task.notification.port.out.CreateNotificationOutPort
import com.newy.task.notification.port.out.GetDeadlineTasksOutPort
import com.newy.task.notification.port.out.GetPendingNotificationsOutPort
import com.newy.task.notification.port.out.SendNotificationOutPort
import com.newy.task.notification.port.out.UpdateNotificationOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@Service
class NotificationService(
    private val getPendingNotificationsOutPort: GetPendingNotificationsOutPort,
    private val updateNotificationOutPort: UpdateNotificationOutPort,
    private val getDeadlineTasksOutPort: GetDeadlineTasksOutPort,
    private val createNotificationOutPort: CreateNotificationOutPort,
    private val sendNotificationOutPorts: List<SendNotificationOutPort>,
) : SendNotificationInPort, CreateDeadlineTaskNotificationInPort {
    @Transactional
    override fun sendNotification(now: OffsetDateTime) {
        getPendingNotificationsOutPort.getPendingNotifications().forEach { notification ->
            try {
                val sender = sendNotificationOutPorts.findByNotification(notification)
                sender.send(notification.payload)
                updateNotificationOutPort.update(notification.success(now))
            } catch (_: Exception) {
                updateNotificationOutPort.update(notification.fail(now))
            }
        }
    }

    @Transactional
    override fun createDeadlineTaskNotification(now: OffsetDateTime) {
        val deadline = now.plusDays(1)
        getDeadlineTasksOutPort.getDeadlineTasks(deadline).forEach { task ->
            val assigneeIds = task.assignees.map { it.id }
            if (assigneeIds.isNotEmpty()) {
                createNotificationOutPort.create(
                    taskId = task.id,
                    assigneeIds = assigneeIds,
                    type = NotificationType.TASK_DEADLINE_IMMINENT,
                )
            }
        }
    }

    fun List<SendNotificationOutPort>.findByNotification(notification: Notification): SendNotificationOutPort {
        return this.find { it.supports(notification.channelType) }
            ?: throw IllegalArgumentException("Not Found Notification Sender. (id: ${notification.id}, channelType: ${notification.channelType})")
    }
}