package com.newy.task.notification.port.out

import com.newy.task.notification.domain.NotificationType

fun interface CreateNotificationOutPort {
    fun create(taskId: Long, assigneeIds: List<Long>, type: NotificationType)
}
