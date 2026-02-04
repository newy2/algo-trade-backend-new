package com.newy.task.task.domain

interface AssigneeIdsValidator {
    val assigneeIds: List<Long>

    fun validateAssigneeIds(savedUserIds: List<Long>) {
        val missingIds = assigneeIds - savedUserIds
        if (missingIds.isNotEmpty()) {
            throw IllegalArgumentException("존재하지 않는 사용자 ID가 포함되어 있습니다: $missingIds")
        }
    }
}