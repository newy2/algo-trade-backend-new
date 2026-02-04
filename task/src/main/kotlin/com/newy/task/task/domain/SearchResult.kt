package com.newy.task.task.domain

data class SearchResult(
    val content: List<Task> = emptyList(),
    val pageNumber: Int = Int.MIN_VALUE,
    val totalPages: Int = Int.MIN_VALUE,
    val totalElements: Long = Long.MIN_VALUE,
)