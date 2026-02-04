package com.newy.task.task.port.out

import com.newy.task.task.domain.Task

fun interface GetTaskOutPort {
    fun get(taskId: Long): Task?
}