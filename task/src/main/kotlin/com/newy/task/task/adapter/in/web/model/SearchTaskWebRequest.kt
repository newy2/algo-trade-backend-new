package com.newy.task.task.adapter.`in`.web.model

import com.newy.task.task.port.`in`.model.SearchTaskQuery

data class SearchTaskWebRequest(
    val keyword: String?,
    val status: String?,
    val priority: String?,
    val startAt: String?,
    val endAt: String?,
    val page: Int = 0,
    val size: Int = 20,
) {
    fun toInPortModel() =
        SearchTaskQuery(
            keyword = keyword,
            status = status,
            priority = priority,
            startAt = startAt,
            endAt = endAt,
            page = page,
            size = size,
        )
}