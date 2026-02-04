package com.newy.task.task.adapter.out.persistence

import com.newy.task.task.adapter.out.persistence.jpa.TaskFullTextSearchJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.TaskJpaRepository
import com.newy.task.task.adapter.out.persistence.jpa.model.TaskFullTextSearchJpaEntity
import com.newy.task.task.adapter.out.persistence.querydsl.TaskFullTextSearchQuerydslRepository
import com.newy.task.task.adapter.out.persistence.querydsl.model.TaskSearchCondition
import com.newy.task.task.domain.SearchResult
import com.newy.task.task.domain.SearchTask
import com.newy.task.task.port.out.IndexSearchTaskOutPort
import com.newy.task.task.port.out.ReIndexSearchTaskOutPort
import com.newy.task.task.port.out.SearchTaskOutPort
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class SearchTaskAdapter(
    private val taskJpaRepository: TaskJpaRepository,
    private val taskSearchJpaRepository: TaskFullTextSearchJpaRepository,
    private val taskSearchQuerydslRepository: TaskFullTextSearchQuerydslRepository,
) : IndexSearchTaskOutPort, ReIndexSearchTaskOutPort, SearchTaskOutPort {
    override fun index(taskId: Long) {
        val task = taskJpaRepository.findByIdOrNull(taskId)!!
        val searchEntity = TaskFullTextSearchJpaEntity(task)
        taskSearchJpaRepository.save(searchEntity)
    }

    override fun reindex(taskId: Long) {
        val task = taskJpaRepository.findByIdOrNull(taskId)!!
        val searchEntity = taskSearchJpaRepository.findByIdOrNull(taskId)!!
        searchEntity.update(task)
    }

    override fun search(searchTask: SearchTask): SearchResult {
        val result = taskSearchQuerydslRepository.searchTasks(
            condition = TaskSearchCondition.fromDomainModel(searchTask),
            pageable = PageRequest.of(searchTask.page, searchTask.size)
        )

        return SearchResult(
            content = result.content.map { it.toDomainModel() },
            pageNumber = result.number,
            totalPages = result.totalPages,
            totalElements = result.totalElements,
        )
    }
}