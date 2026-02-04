package com.newy.task.task.service

import com.newy.task.common.error.NotFoundResourceException
import com.newy.task.notification.domain.NotificationType
import com.newy.task.notification.port.out.CreateNotificationOutPort
import com.newy.task.task.domain.AssigneeIdsValidator
import com.newy.task.task.domain.SearchResult
import com.newy.task.task.port.`in`.model.CreateTaskCommand
import com.newy.task.task.port.`in`.model.SearchTaskQuery
import com.newy.task.task.port.`in`.model.TaskId
import com.newy.task.task.port.`in`.model.UpdateTaskCommand
import com.newy.task.task.port.`in`.CreateTaskInPort
import com.newy.task.task.port.`in`.DeleteTaskInPort
import com.newy.task.task.port.`in`.GetTaskInPort
import com.newy.task.task.port.`in`.SearchTaskInPort
import com.newy.task.task.port.`in`.UpdateTaskInPort
import com.newy.task.task.port.out.CreateTaskOutPort
import com.newy.task.task.port.out.DeleteTaskOutPort
import com.newy.task.task.port.out.ExistsTaskOutPort
import com.newy.task.task.port.out.FindUserIdsOutPort
import com.newy.task.task.port.out.GetTaskOutPort
import com.newy.task.task.port.out.IndexSearchTaskOutPort
import com.newy.task.task.port.out.ReIndexSearchTaskOutPort
import com.newy.task.task.port.out.SearchTaskOutPort
import com.newy.task.task.port.out.UpdateTaskOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TaskService(
    private val findUserIdsOutPort: FindUserIdsOutPort,
    private val existsTaskOutPort: ExistsTaskOutPort,
    private val createTaskOutPort: CreateTaskOutPort,
    private val deleteTaskOutPort: DeleteTaskOutPort,
    private val getTaskOutPort: GetTaskOutPort,
    private val updateTaskOutPort: UpdateTaskOutPort,
    private val searchTaskOutPort: SearchTaskOutPort,
    private val indexSearchTaskOutPort: IndexSearchTaskOutPort,
    private val reIndexSearchTaskOutPort: ReIndexSearchTaskOutPort,
    private val createNotificationOutPort: CreateNotificationOutPort,
) : CreateTaskInPort, DeleteTaskInPort, GetTaskInPort, UpdateTaskInPort, SearchTaskInPort {
    @Transactional
    override fun create(command: CreateTaskCommand): Long {
        val task = command.toDomainModel().also {
            it.validateAssigneesIfNeeded(findUserIdsOutPort)
        }
        val taskId = createTaskOutPort.create(task)
        indexSearchTaskOutPort.index(taskId)
        createNotifications(taskId = taskId, assigneeIds = task.assigneeIds)
        return taskId
    }

    @Transactional
    override fun delete(taskId: TaskId) {
        if (!existsTaskOutPort.exists(taskId.value)) {
            throw NotFoundResourceException("존재하지 않는 Task ID 입니다: ${taskId.value}")
        }
        deleteTaskOutPort.delete(taskId.value)
    }

    @Transactional(readOnly = true)
    override fun get(taskId: TaskId) =
        getTaskOutPort.get(taskId.value)
            ?: throw NotFoundResourceException("존재하지 않는 Task ID 입니다: ${taskId.value}")

    @Transactional
    override fun update(command: UpdateTaskCommand) {
        val savedTask = getTaskOutPort.get(command.taskId)
            ?: throw NotFoundResourceException("존재하지 않는 Task ID 입니다: ${command.taskId}")

        val updateTask = command.toDomainModel().merge(savedTask).also {
            it.validateAssigneesIfNeeded(findUserIdsOutPort)
        }

        updateTaskOutPort.update(updateTask)
        reIndexSearchTaskOutPort.reindex(updateTask.taskId)
        createNotifications(taskId = updateTask.taskId, assigneeIds = updateTask.addedAssigneeIds)
    }

    @Transactional(readOnly = true)
    override fun search(searchTask: SearchTaskQuery): SearchResult =
        searchTaskOutPort.search(searchTask.toDomainModel())

    private fun createNotifications(taskId: Long, assigneeIds: List<Long>) {
        if (assigneeIds.isEmpty()) {
            return
        }
        createNotificationOutPort.create(
            taskId = taskId,
            assigneeIds = assigneeIds,
            type = NotificationType.TASK_ASSIGNED,
        )
    }
}

fun AssigneeIdsValidator.validateAssigneesIfNeeded(findUserIdsOutPort: FindUserIdsOutPort) {
    if (assigneeIds.isNotEmpty()) {
        validateAssigneeIds(savedUserIds = findUserIdsOutPort.findUserIds(assigneeIds))
    }
}