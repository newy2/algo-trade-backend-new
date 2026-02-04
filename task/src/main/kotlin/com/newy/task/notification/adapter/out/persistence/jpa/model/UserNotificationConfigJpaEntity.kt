package com.newy.task.notification.adapter.out.persistence.jpa.model

import com.newy.task.notification.domain.NotificationChannelType
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import jakarta.persistence.*

@Entity
@Table(name = "user_notification_config")
class UserNotificationConfigJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @field:ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: UserJpaEntity,

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type")
    val channelType: NotificationChannelType,

    var destination: String? = null,
)