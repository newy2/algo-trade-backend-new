package com.newy.task.integration.spring.auth

import com.newy.task.spring.auth.annotation.CurrentUser
import com.newy.task.spring.auth.annotation.LoginRequired
import com.newy.task.integration.helper.WebMvcTestHelper
import com.newy.task.spring.auth.model.LoginUser
import com.newy.task.spring.auth.repository.LoginUserJpaEntity
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

// 클래스 전체에 @LoginRequired가 붙은 경우
@RestController
@LoginRequired
class AllPrivateApiController {
    @GetMapping("/test/controller/private-api")
    fun getAdminInfo(@CurrentUser user: LoginUser) = "Hello User ${user.id}"
}

// 메서드별로 권한이 다른 경우
@RestController
class HalfPrivateApiController {

    @LoginRequired
    @GetMapping("/test/handler/private-api")
    fun privateApi(@CurrentUser user: LoginUser) = "Private Data for ${user.id}"

    @GetMapping("/test/handler/public-api")
    fun publicApi(@CurrentUser user: LoginUser?) = "Public Data for ${user?.id ?: "Guest"}"
}

@Transactional
@WebMvcTestHelper
@DisplayName("HTTP Request header 를 사용한 가짜 로그인 기능 테스트")
class AuthenticationTest(
    @Autowired val em: EntityManager,
    @Autowired val mockMvc: MockMvc
) {
    private val client = MockMvcWebTestClient.bindTo(mockMvc).build()

    @Nested
    inner class AllPrivateApiControllerTest {
        @Test
        fun `Private API 는 X-User-Id 헤더가 있어야 한다`() {
            client.get().uri("/test/controller/private-api")
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `Private API는 X-User-Id 헤더 값이 DB의 User Id 인 경우 호출에 성공한다`() {
            val user = LoginUserJpaEntity(nickname = "홍길동")
            em.persist(user)

            client.get().uri("/test/controller/private-api")
                .header("X-User-Id", user.id.toString())
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo("Hello User ${user.id}")
        }

        @Test
        fun `Private API는 X-User-Id 헤더 값이 DB의 User Id 가 아닌 경우 호출에 실패한다`() {
            val notUserId = 1234
            client.get().uri("/test/controller/private-api")
                .header("X-User-Id", notUserId.toString())
                .exchange()
                .expectStatus().isUnauthorized
        }
    }


    @Nested
    inner class HalfPrivateApiControllerTest {
        @Test
        fun `Public API 는 X-User-Id 헤더를 사용하지 않아도 된다`() {
            client.get().uri("/test/handler/public-api")
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo("Public Data for Guest")
        }

        @Test
        fun `Private API 는 Controller 메서드 단위로 선언할 수 있다`() {
            client.get().uri("/test/handler/private-api")
                .exchange()
                .expectStatus().isUnauthorized
        }

        @Test
        fun `Private API 호출 성공 테스트`() {
            val user = LoginUserJpaEntity(nickname = "홍길동")
            em.persist(user)

            client.get().uri("/test/handler/private-api")
                .header("X-User-Id", user.id.toString())
                .exchange()
                .expectStatus().isOk
                .expectBody(String::class.java).isEqualTo("Private Data for ${user.id}")
        }

        @Test
        fun `Private API 호출 실패 테스트`() {
            val notUserId = 1234
            client.get().uri("/test/handler/private-api")
                .header("X-User-Id", notUserId.toString())
                .exchange()
                .expectStatus().isUnauthorized
        }
    }
}