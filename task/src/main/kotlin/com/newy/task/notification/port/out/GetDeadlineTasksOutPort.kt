package com.newy.task.notification.port.out

import com.newy.task.task.domain.Task
import java.time.OffsetDateTime

fun interface GetDeadlineTasksOutPort {
    fun getDeadlineTasks(deadline: OffsetDateTime): List<Task>
}