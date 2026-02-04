package com.newy.task.task.port.`in`

import com.newy.task.task.port.`in`.model.CreateTaskCommand

fun interface CreateTaskInPort {
    fun create(command: CreateTaskCommand): Long
}