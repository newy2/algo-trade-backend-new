package com.newy.task.common.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UtcOffsetDateTimeValidator::class])
annotation class ValidUtcDateTime(
    val message: String = "UTC 시간대(Z) 형식의 날짜여야 합니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class UtcOffsetDateTimeValidator : ConstraintValidator<ValidUtcDateTime, String> {
    override fun isValid(value: String?, context: ConstraintValidatorContext?): Boolean {
        if (value.isNullOrBlank()) {
            return true
        }

        return try {
            val parsed = OffsetDateTime.parse(value)
            parsed.offset == ZoneOffset.UTC && value.endsWith("Z")
        } catch (e: Exception) {
            false
        }
    }
}