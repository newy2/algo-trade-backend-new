package com.newy.task.spring.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoginUserJpaRepository : JpaRepository<LoginUserJpaEntity, Long>