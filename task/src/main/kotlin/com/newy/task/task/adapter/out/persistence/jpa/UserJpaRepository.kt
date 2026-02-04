package com.newy.task.task.adapter.out.persistence.jpa

import com.newy.task.task.adapter.out.persistence.jpa.model.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    @Query("SELECT u.id FROM UserJpaEntity u WHERE u.id IN :ids")
    fun findAllIdsByIdIn(ids: List<Long>): List<Long>
}