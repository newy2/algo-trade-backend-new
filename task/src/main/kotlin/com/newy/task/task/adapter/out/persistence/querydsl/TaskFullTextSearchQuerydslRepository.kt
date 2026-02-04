package com.newy.task.task.adapter.out.persistence.querydsl

import com.newy.task.task.adapter.out.persistence.jpa.model.QTaskFullTextSearchJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.QTaskJpaEntity
import com.newy.task.task.adapter.out.persistence.jpa.model.TaskJpaEntity
import com.newy.task.task.adapter.out.persistence.querydsl.model.TaskSearchCondition
import com.newy.task.task.domain.TaskPriority
import com.newy.task.task.domain.TaskStatus
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
class TaskFullTextSearchQuerydslRepository(
    private val queryFactory: JPAQueryFactory,
    @Value("\${spring.datasource.driver-class-name}")
    private val driverClassName: String
) {
    private val task = QTaskJpaEntity.taskJpaEntity
    private val search = QTaskFullTextSearchJpaEntity.taskFullTextSearchJpaEntity

    fun searchTasks(condition: TaskSearchCondition, pageable: Pageable): Page<TaskJpaEntity> {
        val fixedPageable = PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.Direction.DESC, "id")

        val content = selectFromTaskWithSearch(condition)
            .select(task)
            .orderBy(task.id.desc())
            .offset(fixedPageable.offset)
            .limit(fixedPageable.pageSize.toLong())
            .fetch()

        val countQuery = selectFromTaskWithSearch(condition)
            .select(task.count())

        return PageableExecutionUtils.getPage(content, fixedPageable) {
            countQuery.fetchOne() ?: 0L
        }
    }

    private fun selectFromTaskWithSearch(condition: TaskSearchCondition) =
        queryFactory
            .from(task)
            .innerJoin(search).on(task.id.eq(search.taskId))
            .where(
                fullTextSearch(condition.keyword),
                statusEq(condition.status),
                priorityEq(condition.priority),
                startAtGoe(condition.startAt),
                endAtLoe(condition.endAt),
            )

    private fun fullTextSearch(keyword: String?): BooleanExpression? {
        if (keyword.isNullOrBlank()) {
            return null
        }

        return if (driverClassName.contains("mysql", ignoreCase = true)) {
            // TODO fix mysql (SQL 과 API 는 검색 결과가 나오는데, 통합 테스트에서 검색 결과가 나오지 않음)
            Expressions.numberTemplate(
                Double::class.javaObjectType,
                "function('match_against', {0}, {1})",
                search.searchContent,
                keyword
            ).gt(0)
        } else {
            search.searchContent.contains(keyword)
        }
    }

    private fun statusEq(status: TaskStatus?) = status?.let { task.status.eq(it) }
    private fun priorityEq(priority: TaskPriority?) = priority?.let { task.priority.eq(it) }
    private fun startAtGoe(startFrom: OffsetDateTime?) = startFrom?.let { task.startAt.goe(it) }
    private fun endAtLoe(endTo: OffsetDateTime?) = endTo?.let { task.endAt.loe(it) }
}