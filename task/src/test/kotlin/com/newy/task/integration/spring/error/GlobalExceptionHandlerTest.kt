package com.newy.task.integration.spring.error

import com.newy.task.common.error.NotFoundResourceException
import com.newy.task.common.error.ResourceConflictException
import com.newy.task.common.validation.SelfValidating
import com.newy.task.integration.helper.WebMvcTestHelper
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.bind.annotation.*

// 1. 테스트용 DTO 및 컨트롤러 (이전과 동일)
data class TestRequest(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
)

data class TestInPortModel(
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
) : SelfValidating() {
    init {
        validate()
    }
}

@RestController
class TestController {
    @PostMapping("/test/validation")
    fun testValidation(@Valid @RequestBody request: TestRequest) = "ok"

    @PostMapping("/test/validation2")
    fun testValidation2(@RequestBody request: TestRequest) {
        TestInPortModel(request.name) // Spring @Valid 가 아닌 SelfValidation 에러 테스트용
        "ok"
    }

    @GetMapping("/test/IllegalArgumentException")
    fun throwIllegalArgumentException() {
        throw IllegalArgumentException("잘못된 파라미터 입니다.")
    }

    @DeleteMapping("/test/{taskId}")
    fun delete(
        @PathVariable taskId: Long
    ) {
        "ok"
    }

    @GetMapping("/test/ResourceNotFoundException")
    fun throwResourceNotFoundException() {
        throw NotFoundResourceException("Task를 찾을 수 없습니다. ID: 4231")
    }

    @PutMapping("/test/ResourceConflictException")
    fun throwResourceConflictException() {
        // 어댑터에서 변환되어 던져질 커스텀 예외 시뮬레이션
        throw ResourceConflictException("해당 리소스가 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해주세요.")
    }
}


@WebMvcTestHelper
@DisplayName("전역 에러 핸들러 테스트")
class GlobalExceptionHandlerTest(
    @Autowired val mockMvc: MockMvc,
) {
    private val client = MockMvcWebTestClient.bindTo(mockMvc).build()

    @Test
    @DisplayName("Spring 의 Bean Validation 실패 시 400 에러와 필드 에러 상세를 반환한다")
    fun handleMethodArgumentNotValidException() {
        val invalidRequest = mapOf("name" to "") // 빈 값 전송

        client.post()
            .uri("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 값이 올바르지 않습니다.")
            .jsonPath("$.errors[0].field").isEqualTo("name")
            .jsonPath("$.errors[0].reason").isEqualTo("이름은 필수입니다.")
    }

    @Test
    @DisplayName("SelfValidating 의 Bean Validation 실패 시 400 에러와 필드 에러 상세를 반환한다")
    fun handleConstraintViolationException() {
        val invalidRequest = mapOf("name" to "") // 빈 값 전송

        client.post()
            .uri("/test/validation2")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 값이 올바르지 않습니다.")
            .jsonPath("$.errors[0].field").isEqualTo("name")
            .jsonPath("$.errors[0].reason").isEqualTo("이름은 필수입니다.")
    }

    @Test
    @DisplayName("JSON 형식이 잘못되었을 때(HttpMessageNotReadable) 에러 메시지를 반환한다")
    fun handleHttpMessageNotReadableException() {
        val invalidJson = "{ \"name\": " // 잘못된 JSON 형식

        client.post()
            .uri("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidJson)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 데이터를 읽을 수 없습니다. JSON 형식을 확인해주세요.")
    }

    @Test
    @DisplayName("Body가 누락되었을 때 명확한 에러 메시지를 반환한다")
    fun handleMissingBody() {
        client.post()
            .uri("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 본문(Body)이 누락되었습니다.")
    }

    @Test
    @DisplayName("DTO에 정의되지 않은 키(Unknown Property)가 전달되면 400 에러와 원인 필드를 반환한다")
    fun handleUnrecognizedPropertyException() {
        // DTO에는 'name'만 있는데 'unknownKey'를 보냄
        val invalidRequest = mapOf(
            "name" to "Gemini",
            "unknownKey" to "someValue"
        )

        client.post()
            .uri("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 값이 올바르지 않습니다.")
            .jsonPath("$.errors[0].field").isEqualTo("unknownKey")
            .jsonPath("$.errors[0].value").isEqualTo("Unknown Key")
            .jsonPath("$.errors[0].reason").value<String> {
                assert(it.contains("정의되지 않은 필드입니다"))
                assert(it.contains("name")) // 허용된 필드 목록이 포함되었는지 확인
            }
    }

    @Test
    @DisplayName("필드 타입이 맞지 않는 값(Type Mismatch)이 전달되면 400 에러와 상세 정보를 반환한다")
    fun handleInvalidFormatException() {
        val invalidRequest = """
        {
            "name": { "firstName": "John" }
        }
    """.trimIndent()

        client.post()
            .uri("/test/validation")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidRequest)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            // ✅ 이제 Handler에서 createInvalidErrorResponse를 리턴하므로 이 메시지가 맞습니다.
            .jsonPath("$.message").isEqualTo("요청 값이 올바르지 않습니다.")
            .jsonPath("$.errors[0].field").isEqualTo("name")
            .jsonPath("$.errors[0].reason").value<String> {
                assert(it.contains("String 타입이 필요합니다"))
            }
    }

    @Test
    @DisplayName("PathVariable의 타입이 맞지 않으면 400 에러와 상세 정보를 반환한다")
    fun handleMethodArgumentTypeMismatchException() {
        client.delete()
            .uri("/test/wrong-id") // Long이 필요한데 String 전달
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("요청 경로 파라미터 값이 올바르지 않습니다.")
            .jsonPath("$.errors[0].field").isEqualTo("taskId")
            .jsonPath("$.errors[0].value").isEqualTo("wrong-id")
            .jsonPath("$.errors[0].reason").isEqualTo("long 타입이 필요합니다.")
    }

    @Test
    @DisplayName("IllegalArgumentException 에러 메시지를 반환한다")
    fun handleIllegalArgumentException() {
        client.get()
            .uri("/test/IllegalArgumentException")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.message").isEqualTo("잘못된 파라미터 입니다.")
    }

    @Test
    @DisplayName("ResourceNotFoundException 에러 메시지를 반환한다")
    fun handleResourceNotFoundException() {
        client.get()
            .uri("/test/ResourceNotFoundException")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.message").isEqualTo("Task를 찾을 수 없습니다. ID: 4231")
    }

    @Test
    @DisplayName("ResourceConflictException 발생 시 409 Conflict 에러와 메시지를 반환한다")
    fun handleResourceConflictException() {
        client.put()
            .uri("/test/ResourceConflictException")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT) // 409
            .expectBody()
            .jsonPath("$.message").isEqualTo("해당 리소스가 이미 다른 사용자에 의해 수정되었습니다. 다시 시도해주세요.")
    }
}