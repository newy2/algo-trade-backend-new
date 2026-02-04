package com.newy.task.task.adapter.out.persistence.jpa

import com.newy.task.task.adapter.out.persistence.jpa.model.TaskJpaEntity
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskJpaRepository : JpaRepository<TaskJpaEntity, Long> {
    @EntityGraph(attributePaths = ["assignments"])
    fun findWithAssignmentsById(id: Long): TaskJpaEntity?
}