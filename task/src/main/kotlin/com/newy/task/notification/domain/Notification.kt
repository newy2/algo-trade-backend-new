package com.newy.task.notification.domain

import java.time.OffsetDateTime

data class Notification(
    val id: Long,
    val payload: NotificationPayload,
    val status: NotificationStatus,
    val channelType: NotificationChannelType,
    val retryCount: Int,
    val processedAt: OffsetDateTime? = null,
) {
    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    fun success(now: OffsetDateTime = OffsetDateTime.now()): Notification {
        return copy(status = NotificationStatus.SENT, processedAt = now)
    }

    fun fail(now: OffsetDateTime = OffsetDateTime.now()): Notification {
        if (retryCount >= MAX_RETRY_COUNT) {
            return copy(retryCount = retryCount + 1, status = NotificationStatus.FAIL, processedAt = now)
        }
        return copy(retryCount = retryCount + 1, processedAt = now)
    }
}
