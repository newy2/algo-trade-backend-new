package com.newy.task.task.adapter.out.persistence.jpa.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var nickname: String
)
