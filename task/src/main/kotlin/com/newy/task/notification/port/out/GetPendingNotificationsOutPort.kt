package com.newy.task.notification.port.out

import com.newy.task.notification.domain.Notification

fun interface GetPendingNotificationsOutPort {
    fun getPendingNotifications(): List<Notification> = getPendingNotifications(DEFAULT_PAGE_SIZE)
    fun getPendingNotifications(size: Int): List<Notification>

    companion object {
        const val DEFAULT_PAGE_SIZE = 50
    }
}