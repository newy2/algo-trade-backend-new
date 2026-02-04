package com.newy.task.task.port.`in`

import com.newy.task.task.domain.SearchResult
import com.newy.task.task.port.`in`.model.SearchTaskQuery

fun interface SearchTaskInPort {
    fun search(searchTask: SearchTaskQuery): SearchResult
}