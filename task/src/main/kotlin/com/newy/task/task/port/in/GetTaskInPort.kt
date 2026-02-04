package com.newy.task.task.port.`in`

import com.newy.task.task.domain.Task
import com.newy.task.task.port.`in`.model.TaskId

fun interface GetTaskInPort {
    fun get(taskId: TaskId): Task
}