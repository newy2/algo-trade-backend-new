package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.domain.SearchResult

data class SearchTaskWebResponse(
    val content: List<GetTaskWebResponse>,
    val pageNumber: Int,
    val totalElements: Long,
    val totalPages: Int,
) {
    companion object {
        fun fromDomainModel(domainModel: SearchResult) =
            SearchTaskWebResponse(
                content = domainModel.content.map { GetTaskWebResponse.fromDomainModel(it) },
                pageNumber = domainModel.pageNumber,
                totalPages = domainModel.totalPages,
                totalElements = domainModel.totalElements,
            )
    }
}