package com.newy.task.task.port.out

fun interface ReIndexSearchTaskOutPort {
    fun reindex(taskId: Long)
}