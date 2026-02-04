package com.newy.task.task.port.out

import com.newy.task.task.domain.SearchResult
import com.newy.task.task.domain.SearchTask

fun interface SearchTaskOutPort {
    fun search(searchTask: SearchTask): SearchResult
}