package com.newy.task.task.port.out

fun interface FindUserIdsOutPort {
    fun findUserIds(userIds: List<Long>): List<Long>
}