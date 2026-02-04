package com.newy.task.notification.port.out

import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationPayload

interface SendNotificationOutPort {
    fun supports(channelType: NotificationChannelType): Boolean
    fun send(payload: NotificationPayload)
}