package com.newy.task.task.port.out

fun interface ExistsTaskOutPort {
    fun exists(taskId: Long): Boolean
}