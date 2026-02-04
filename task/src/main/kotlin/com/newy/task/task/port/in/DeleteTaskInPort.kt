package com.newy.task.task.port.`in`

import com.newy.task.task.port.`in`.model.TaskId

fun interface DeleteTaskInPort {
    fun delete(taskId: TaskId)
}