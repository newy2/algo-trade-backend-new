package com.newy.task.unit.task.port.`in`.model

import com.newy.task.task.domain.SearchTask
import com.newy.task.task.port.`in`.model.SearchTaskQuery
import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SearchTaskQueryTest {
    private val inPortModel = SearchTaskQuery(
        page = 0,
        size = 20,
        keyword = "검색어",
    )

    @Nested
    inner class ToDomainModelTest {
        private val domainModel = SearchTask(
            page = 0,
            size = 20,
            keyword = "검색어",
        )

        @Test
        fun `InPortModel 은 DomainModel 로 변환할 수 있어야 한다`() {
            assertEquals(domainModel, inPortModel.toDomainModel())
        }

        @Test
        fun `keyword 는 trim 처리 한다`() {
            assertEquals(domainModel, inPortModel.copy(keyword = "  검색어  ").toDomainModel())
        }
    }

    @Test
    fun `page 는 0 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(page = -2) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(page = -1) }
        assertDoesNotThrow { inPortModel.copy(page = 0) }
        assertDoesNotThrow { inPortModel.copy(page = 1) }
    }

    @Test
    fun `size 는 0 이상이어야 한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(size = -2) }
        assertThrows<ConstraintViolationException> { inPortModel.copy(size = -1) }
        assertDoesNotThrow { inPortModel.copy(size = 0) }
        assertDoesNotThrow { inPortModel.copy(size = 1) }
    }

    @Test
    fun `title 은 빈 문자열을 사용할 수 없다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(keyword = "") }
        assertDoesNotThrow { inPortModel.copy(keyword = null) }
        assertDoesNotThrow { inPortModel.copy(keyword = "a") }
    }

    @Test
    fun `status 는 WAITING, IN_PROGRESS, DONE 만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(status = "NOT_SUPPORT_VALUE") }
        assertDoesNotThrow { inPortModel.copy(priority = null) }
        assertDoesNotThrow { inPortModel.copy(status = "WAITING") }
        assertDoesNotThrow { inPortModel.copy(status = "IN_PROGRESS") }
        assertDoesNotThrow { inPortModel.copy(status = "DONE") }
    }

    @Test
    fun `priority 는 LOW, MEDIUM, HIGH 만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(priority = "NOT_SUPPORT_VALUE") }
        assertDoesNotThrow { inPortModel.copy(priority = null) }
        assertDoesNotThrow { inPortModel.copy(priority = "LOW") }
        assertDoesNotThrow { inPortModel.copy(priority = "MEDIUM") }
        assertDoesNotThrow { inPortModel.copy(priority = "HIGH") }
    }

    @Test
    fun `statedAt 은 UTC 형식만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(startAt = "2026-01-25T03:00:00+09:00") }
        assertDoesNotThrow { inPortModel.copy(startAt = null) }
        assertDoesNotThrow { inPortModel.copy(startAt = "2026-01-26T03:00:00Z") }
    }

    @Test
    fun `endedAt 은 UTC 형식만 지원한다`() {
        assertThrows<ConstraintViolationException> { inPortModel.copy(endAt = "2026-01-25T03:00:00+09:00") }
        assertDoesNotThrow { inPortModel.copy(endAt = null) }
        assertDoesNotThrow { inPortModel.copy(endAt = "2026-01-26T03:00:00Z") }
    }
}