package com.newy.task.notification.adapter.`in`.internal_system

import com.newy.task.notification.port.`in`.CreateDeadlineTaskNotificationInPort
import com.newy.task.notification.port.`in`.SendNotificationInPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class SendNotificationScheduler(
    val sendNotificationInPort: SendNotificationInPort,
    val createDeadlineTaskNotificationInPort: CreateDeadlineTaskNotificationInPort,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Scheduled(fixedDelay = 10 * 1000)
    fun sendNotification() {
        val now = OffsetDateTime.now()
        log.info("Start sendNotification")
        sendNotificationInPort.sendNotification(now)
    }

    @Scheduled(cron = "0 */5 * * * *")
    fun createDeadlineTaskNotification() {
        val now = OffsetDateTime.now()
        log.info("Start createDeadlineTaskNotification")
        createDeadlineTaskNotificationInPort.createDeadlineTaskNotification(now)
    }
}