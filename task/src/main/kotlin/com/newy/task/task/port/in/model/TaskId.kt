package com.newy.task.task.port.`in`.model

import com.newy.task.common.validation.SelfValidating
import jakarta.validation.constraints.Min

data class TaskId(
    @field:Min(1) val value: Long,
) : SelfValidating() {
    init {
        validate()
    }
}