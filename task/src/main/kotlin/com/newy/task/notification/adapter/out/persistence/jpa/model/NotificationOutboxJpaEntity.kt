package com.newy.task.notification.adapter.out.persistence.jpa.model

import com.newy.task.notification.domain.Notification
import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.notification.domain.NotificationPayload
import com.newy.task.notification.domain.NotificationStatus
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "notification_outbox")
class NotificationOutboxJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserJpaEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    val channelType: NotificationChannelType,

    var destination: String? = null,

    @Column(columnDefinition = "TEXT")
    val message: String,

    @Column(name = "event_key")
    val eventKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: NotificationStatus = NotificationStatus.READY,

    @Column(nullable = false)
    var retryCount: Int = 0,

    @Column(nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    var processedAt: OffsetDateTime? = null
) {
    fun toDomainModel() =
        Notification(
            id = this.id,
            payload = NotificationPayload(
                destination = this.destination,
                message = this.message,
            ),
            status = this.status,
            channelType = this.channelType,
            retryCount = this.retryCount,
            processedAt = this.processedAt,
        )
}