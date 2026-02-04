package com.newy.task.unit.task.port.`in`.model

import com.newy.task.task.port.`in`.model.TaskId
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TaskIdTest {
    private val inPortValue = TaskId(value = 123)

    @Test
    fun `InPortValue 에서 값을 꺼낼 수 있어야 한다`() {
        assertEquals(123L, inPortValue.value)
    }


    @Test
    fun `value 는 1 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortValue.copy(value = -1) }
        assertThrows<ConstraintViolationException> { inPortValue.copy(value = 0) }
        assertDoesNotThrow { inPortValue.copy(value = 1) }
        assertDoesNotThrow { inPortValue.copy(value = 2) }
    }
}