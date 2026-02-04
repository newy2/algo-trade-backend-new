package com.newy.task.spring.auth.repository

import jakarta.persistence.*

@Entity
@Table(name = "users")
class LoginUserJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var nickname: String,
)