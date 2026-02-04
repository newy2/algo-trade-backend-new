package com.newy.task.notification.port.`in`

import java.time.OffsetDateTime

fun interface CreateDeadlineTaskNotificationInPort {
    fun createDeadlineTaskNotification(now: OffsetDateTime)
}