package com.newy.task.notification.domain

enum class NotificationType {
    TASK_ASSIGNED {
        override fun getMessage(taskId: Long) =
            "새로운 Task에 할당되었습니다. (Task ID: $taskId)"
    },
    TASK_DEADLINE_IMMINENT {
        override fun getMessage(taskId: Long) =
            "Task 마감일 1일 전입니다. (Task ID: ${taskId})"
    };

    abstract fun getMessage(taskId: Long): String
}