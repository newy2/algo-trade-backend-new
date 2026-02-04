package com.newy.task.unit.notification.domain

import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationPayload
import com.newy.task.notification.domain.NotificationStatus.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime


class NotificationTest {
    val notification = Notification(
        id = 1,
        payload = NotificationPayload(
            message = "알림 메세지"
        ),
        status = READY,
        channelType = NotificationChannelType.LOG,
        retryCount = 0,
    )
    val proceedAt = OffsetDateTime.parse("2025-01-02T00:00:00.000Z")

    @Test
    fun `알림 전송에 성공하면 SEND 상태로 변경된다`() {
        assertEquals(
            notification.copy(status = SENT, processedAt = proceedAt),
            notification.success(proceedAt)
        )
    }

    @Test
    fun `알림 전송에 실패하면 retryCount 가 증가한다`() {
        assertEquals(
            notification.copy(retryCount = 1, processedAt = proceedAt),
            notification.fail(proceedAt)
        )
    }

    @Test
    fun `최대 재전송 횟수를 초과한 경우 FAIL 상태로 변경된다`() {
        val maxRetryCount = Notification.MAX_RETRY_COUNT
        val notification = notification.copy(retryCount = maxRetryCount)

        assertEquals(3, maxRetryCount)
        assertEquals(
            notification.copy(retryCount = maxRetryCount + 1, status = FAIL, processedAt = proceedAt),
            notification.fail(proceedAt)
        )
    }
}