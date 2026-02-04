package com.newy.task.task.port.`in`

import com.newy.task.task.port.`in`.model.UpdateTaskCommand

fun interface UpdateTaskInPort {
    fun update(command: UpdateTaskCommand)
}