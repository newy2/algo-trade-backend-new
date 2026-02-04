package com.newy.task.notification.adapter.out.internal_system

import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationPayload
import com.newy.task.notification.port.out.SendNotificationOutPort
import org.springframework.stereotype.Component

@Component
class SendLogNotificationAdapter : SendNotificationOutPort {
    private var log: String = ""

    override fun supports(channelType: NotificationChannelType): Boolean =
        channelType == NotificationChannelType.LOG

    override fun send(payload: NotificationPayload) {
        log += "[${payload.destination}]: ${payload.message}"
    }

    fun getLog(): String = log
}