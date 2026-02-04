package com.newy.task.notification.port.out

import com.newy.task.notification.domain.Notification

fun interface UpdateNotificationOutPort {
    fun update(notification: Notification)
}