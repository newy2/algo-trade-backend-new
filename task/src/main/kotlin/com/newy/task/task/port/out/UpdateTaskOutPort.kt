package com.newy.task.task.port.out

import com.newy.task.task.domain.UpdateTask

fun interface UpdateTaskOutPort {
    fun update(updateTask: UpdateTask)
}