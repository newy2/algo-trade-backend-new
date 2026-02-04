package com.newy.task.unit.notification.adapter.`in`.internal_system

import com.newy.task.notification.adapter.`in`.internal_system.SendNotificationScheduler
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.OffsetDateTime
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertEquals

class CreateTaskControllerTest {
    @Test
    fun `Scheduler 는 현재 시간을 SendNotificationInPort 에 전달한다`() {
        val now = OffsetDateTime.now()
        var captured: OffsetDateTime? = null
        SendNotificationScheduler(
            sendNotificationInPort = { captured = it },
            createDeadlineTaskNotificationInPort = {},
        ).sendNotification()

        val diffMillis = Duration.between(now, captured).abs().toMillis()
        assertTrue(diffMillis < 100)
    }

    @Test
    fun `Scheduler 는 현재 시간을 CreateDeadlineTaskNotificationInPort 에 전달한다`() {
        val now = OffsetDateTime.now()
        var captured: OffsetDateTime? = null
        SendNotificationScheduler(
            sendNotificationInPort = {},
            createDeadlineTaskNotificationInPort = { captured = it },
        ).createDeadlineTaskNotification()

        val diffMillis = Duration.between(now, captured).abs().toMillis()
        assertTrue(diffMillis < 100)
    }
}

class SendNotificationSchedulerAnnotationTest {
    @Test
    fun `Scheduler 클래스는 아래 애너테이션을 사용해야 한다`() {
        assertTrue(SendNotificationScheduler::class.hasAnnotation<Component>())
    }

    @Test
    fun `Scheduler 클래스의 메서드는 아래 애너테이션을 사용해야 한다`() {
        SendNotificationScheduler::sendNotification.let {
            assertEquals(10 * 1000, it.findAnnotation<Scheduled>()?.fixedDelay)
        }

        SendNotificationScheduler::createDeadlineTaskNotification.let {
            assertEquals("0 */5 * * * *", it.findAnnotation<Scheduled>()?.cron)
        }
    }
}