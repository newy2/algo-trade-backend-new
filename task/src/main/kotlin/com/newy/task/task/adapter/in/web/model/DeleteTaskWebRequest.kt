package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.port.`in`.model.TaskId

data class DeleteTaskWebRequest(
    val taskId: Long,
) {
    fun toInPortModel() = TaskId(value = taskId)
}