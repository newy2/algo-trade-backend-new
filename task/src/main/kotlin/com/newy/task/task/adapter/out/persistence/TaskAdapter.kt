package com.newy.task.task.adapter.out.persistence

import com.newy.task.common.error.ResourceConflictException
import com.newy.task.task.adapter.out.persistence.jpa.TaskJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.TaskAssignmentJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.TaskJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import com.newy.task.task.domain.CreateTask
import com.newy.task.task.domain.UpdateTask
import com.newy.task.task.port.out.*
import jakarta.persistence.EntityManager
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component

@Component
class TaskAdapter(
    private val em: EntityManager,
    private val taskJpaRepository: TaskJpaRepository,
) : CreateTaskOutPort, DeleteTaskOutPort, ExistsTaskOutPort, GetTaskOutPort, UpdateTaskOutPort {
    override fun create(createTask: CreateTask): Long {
        val creatorRef = em.getReference(UserJpaEntity::class.java, createTask.createdBy)
        val updaterRef = em.getReference(UserJpaEntity::class.java, createTask.updatedBy)

        val task = TaskJpaEntity(createTask = createTask, creator = creatorRef, updater = updaterRef)
        task.addAssignments(createTask.assigneeIds.map { assigneeId ->
            TaskAssignmentJpaEntity(
                task = task,
                user = em.getReference(UserJpaEntity::class.java, assigneeId),
                creator = creatorRef,
                createdAt = createTask.createdAt,
            )
        })

        return taskJpaRepository.save(task).id
    }

    override fun update(updateTask: UpdateTask) {
        try {
            val task = taskJpaRepository.findWithAssignmentsById(updateTask.taskId)!!
            val updaterRef = em.getReference(UserJpaEntity::class.java, updateTask.updatedBy)

            task.update(updateTask, updater = updaterRef)
            task.removeAssignments(updateTask)
            task.addAssignments(updateTask.addedAssigneeIds.map { assigneeId ->
                TaskAssignmentJpaEntity(
                    task = task,
                    user = em.getReference(UserJpaEntity::class.java, assigneeId),
                    creator = updaterRef,
                    createdAt = updateTask.updatedAt,
                )
            })
        } catch (_: ObjectOptimisticLockingFailureException) {
            throw ResourceConflictException("Task 가 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해주세요.")
        }
    }

    override fun delete(taskId: Long) =
        taskJpaRepository.deleteById(taskId)

    override fun exists(taskId: Long) =
        taskJpaRepository.existsById(taskId)

    override fun get(taskId: Long) =
        taskJpaRepository.findWithAssignmentsById(taskId)?.toDomainModel()
}