package com.newy.task.task.adapter.out.persistence.jpa

import com.newy.task.task.adapter.out.persistence.jpa.model.TaskAssignmentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskAssignmentJpaRepository : JpaRepository<TaskAssignmentJpaEntity, Long>