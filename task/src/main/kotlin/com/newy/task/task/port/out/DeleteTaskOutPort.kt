package com.newy.task.task.port.out

fun interface DeleteTaskOutPort {
    fun delete(taskId: Long)
}