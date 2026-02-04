package com.newy.task.task.port.out

fun interface IndexSearchTaskOutPort {
    fun index(taskId: Long)
}