package com.newy.task.task.port.out

import com.newy.task.task.domain.CreateTask

fun interface CreateTaskOutPort {
    fun create(createTask: CreateTask): Long
}