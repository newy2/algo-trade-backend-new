package com.newy.task.notification.adapter.out.persistence

import com.newy.task.notification.adapter.out.persistence.jpa.NotificationOutboxJpaRepository
import com.newy.task.notification.adapter.out.persistence.jpa.UserNotificationConfigJpaRepository
import com.newy.task.notification.adapter.out.persistence.jpa.model.NotificationOutboxJpaEntity
import com.newy.task.notification.adapter.out.persistence.querydsl.TaskNotificationQuerydslRepository
import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationStatus
import com.newy.task.notification.domain.NotificationType
import com.newy.task.notification.port.out.CreateNotificationOutPort
import com.newy.task.notification.port.out.GetDeadlineTasksOutPort
import com.newy.task.notification.port.out.GetPendingNotificationsOutPort
import com.newy.task.notification.port.out.UpdateNotificationOutPort
import com.newy.task.task.domain.Task
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class NotificationAdapter(
    private val notificationOutboxRepository: NotificationOutboxJpaRepository,
    private val taskNotificationQuerydslRepository: TaskNotificationQuerydslRepository,
    private val notificationConfigRepository: UserNotificationConfigJpaRepository,
    private val outboxRepository: NotificationOutboxJpaRepository
) : GetPendingNotificationsOutPort, UpdateNotificationOutPort, GetDeadlineTasksOutPort, CreateNotificationOutPort {
    override fun getPendingNotifications(size: Int): List<Notification> {
        return notificationOutboxRepository.findByStatusOrderByCreatedAtAsc(
            NotificationStatus.READY,
            PageRequest.of(0, size, Sort.Direction.ASC, "id")
        ).map { it.toDomainModel() }
    }

    override fun update(notification: Notification) {
        notificationOutboxRepository.findByIdOrNull(notification.id)!!.also {
            it.retryCount = notification.retryCount
            it.status = notification.status
            it.processedAt = notification.processedAt
        }
    }

    override fun getDeadlineTasks(deadline: OffsetDateTime): List<Task> {
        return taskNotificationQuerydslRepository.findTasksNearingDeadline(deadline).map { it.toDomainModel() }
    }

    override fun create(taskId: Long, assigneeIds: List<Long>, type: NotificationType) {
        val notificationConfigs = notificationConfigRepository.findAllByUserIdIn(assigneeIds)
        val outboxes = notificationConfigs.mapNotNull { config ->
            val eventKey = "${type}_T${taskId}_U${config.user.id}_C${config.channelType}"
            if (notificationOutboxRepository.existsByEventKey(eventKey)) {
                return@mapNotNull null
            }

            NotificationOutboxJpaEntity(
                user = config.user,
                channelType = config.channelType,
                destination = config.destination,
                message = type.getMessage(taskId),
                eventKey = eventKey,
                status = NotificationStatus.READY
            )
        }
        outboxRepository.saveAll(outboxes)
    }
}