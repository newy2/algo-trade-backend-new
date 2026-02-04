package com.newy.task.spring.error

import com.newy.task.common.error.NotFoundResourceException
import com.newy.task.common.error.ResourceConflictException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import tools.jackson.databind.exc.MismatchedInputException
import tools.jackson.databind.exc.UnrecognizedPropertyException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException) =
        createInvalidErrorResponse(e.bindingResult.fieldErrors.map {
            FieldError(
                field = it.field,
                value = it.rejectedValue?.toString() ?: "",
                reason = it.defaultMessage ?: "Invalid value"
            )
        })

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException) =
        createInvalidErrorResponse(e.constraintViolations.map {
            FieldError(
                field = it.propertyPath.toString(),
                value = it.invalidValue?.toString() ?: "",
                reason = it.message ?: "Invalid value"
            )
        })

    private fun createInvalidErrorResponse(filedErrors: List<FieldError>) =
        ResponseEntity.badRequest().body(
            InvalidErrorResponse(
                message = "요청 값이 올바르지 않습니다.",
                errors = filedErrors
            )
        )

    // ✅ Body 없음 / JSON 파싱 실패
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<*> {
        val cause = e.cause

        return when (cause) {
            is UnrecognizedPropertyException -> {
                val fieldError = FieldError(
                    field = cause.propertyName,
                    value = "Unknown Key",
                    reason = "정의되지 않은 필드입니다. 허용된 필드: ${cause.knownPropertyIds}"
                )
                createInvalidErrorResponse(listOf(fieldError))
            }

            // InvalidFormatException의 부모인 MismatchedInputException으로 범위를 넓힘
            is MismatchedInputException -> {
                val field = cause.path.joinToString(".") { it.propertyName ?: "unknown" }
                val targetType = cause.targetType?.simpleName ?: "Unknown"

                val fieldError = FieldError(
                    field = field,
                    value = "Invalid Type", // 객체가 들어온 경우 toString()이 복잡할 수 있어 고정값 혹은 요약 사용
                    reason = "$targetType 타입이 필요합니다."
                )
                createInvalidErrorResponse(listOf(fieldError))
            }

            else -> {
                val errorMessage = if (e.message?.contains("Required request body is missing") == true) {
                    "요청 본문(Body)이 누락되었습니다."
                } else {
                    "요청 데이터를 읽을 수 없습니다. JSON 형식을 확인해주세요."
                }
                ResponseEntity.badRequest().body(ErrorResponse(errorMessage))
            }
        }
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(e: MethodArgumentTypeMismatchException): ResponseEntity<*> {
        val fieldError = FieldError(
            field = e.name,
            value = e.value?.toString() ?: "",
            reason = "${e.requiredType?.simpleName} 타입이 필요합니다."
        )

        return ResponseEntity.badRequest().body(
            InvalidErrorResponse(
                message = "요청 경로 파라미터 값이 올바르지 않습니다.",
                errors = listOf(fieldError)
            )
        )
    }

    @ExceptionHandler(NotFoundResourceException::class)
    fun handleNotFoundResourceException(e: NotFoundResourceException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(message = e.message ?: "리소스를 찾을 수 없습니다."))
    }

    @ExceptionHandler(ResourceConflictException::class)
    fun handleResourceConflictException(e: ResourceConflictException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(message = e.message ?: "데이터 충돌이 발생했습니다."))
    }

    @ExceptionHandler(Exception::class)
    private fun defaultClientErrorMessage(exception: Exception) =
        HttpStatus.BAD_REQUEST.let { httpStatus ->
            ResponseEntity
                .status(httpStatus)
                .body(
                    ErrorResponse(
                        message = exception.message ?: httpStatus.reasonPhrase,
                    )
                )
        }
}