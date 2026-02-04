package com.newy.task.task.adapter.out.persistence.jpa

import com.newy.task.task.adapter.out.persistence.jpa.model.TaskFullTextSearchJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface TaskFullTextSearchJpaRepository : JpaRepository<TaskFullTextSearchJpaEntity, Long>