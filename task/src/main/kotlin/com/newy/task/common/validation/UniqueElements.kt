package com.newy.task.common.validation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UniqueElementsValidator::class])
annotation class UniqueElements(
    val message: String = "리스트에 중복된 값이 포함되어 있습니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)


class UniqueElementsValidator : ConstraintValidator<UniqueElements, List<*>?> {
    override fun isValid(value: List<*>?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return true
        }

        return value.size == value.distinct().size
    }
}