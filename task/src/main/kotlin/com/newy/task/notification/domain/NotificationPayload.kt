package com.newy.task.notification.domain

data class NotificationPayload(
    val message: String,
    val destination: String? = null,
)