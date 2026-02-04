package com.newy.task.notification.port.`in`

import java.time.OffsetDateTime

fun interface SendNotificationInPort {
    fun sendNotification(now: OffsetDateTime)
}