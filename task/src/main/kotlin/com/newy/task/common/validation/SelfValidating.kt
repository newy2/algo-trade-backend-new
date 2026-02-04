package com.newy.task.common.validation

import jakarta.validation.ConstraintViolationException
import jakarta.validation.Validation

open class SelfValidating {
    companion object {
        private val VALIDATOR = Validation.buildDefaultValidatorFactory().validator
    }

    // TODO 자식 클래스 생성완료시, 자동으로 validate 메소드 호출하도록 수정
    protected fun validate() {
        val violations = VALIDATOR.validate(this)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }
}