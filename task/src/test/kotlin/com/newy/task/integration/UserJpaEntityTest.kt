package com.newy.task.integration

import com.newy.task.integration.helper.DataJpaTestHelper
import jakarta.persistence.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import kotlin.test.assertEquals

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var nickname: String
)


@DataJpaTestHelper
class UserJpaEntityTest(
    @Autowired val em: TestEntityManager,
) {
    @Test
    fun testPersist() {
        val userId = em.persist(UserJpaEntity(nickname = "홍길동")).id
        em.flush()
        em.clear()

        val user = em.find(UserJpaEntity::class.java, userId)
        assertEquals("홍길동", user!!.nickname)
    }
}