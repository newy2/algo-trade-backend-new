package com.newy.task.task.adapter.out.persistence.jpa.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "task_full_text_search")
class TaskFullTextSearchJpaEntity(
    @Id
    @Column(name = "task_id")
    val taskId: Long,

    var searchContent: String,

    var titleRaw: String,

    var descriptionRaw: String?,

    var assigneesRaw: String?,

    var updatedBy: Long,

    var updatedAt: OffsetDateTime
) {
    constructor(task: TaskJpaEntity) : this(
        taskId = task.id,
        searchContent = getSearchContent(task),
        titleRaw = task.title,
        descriptionRaw = task.description,
        assigneesRaw = getUserNickname(task),
        updatedBy = task.updater.id,
        updatedAt = task.updatedAt,
    )

    fun update(task: TaskJpaEntity) {
        this.searchContent = getSearchContent(task)
        this.titleRaw = task.title
        this.descriptionRaw = task.description
        this.assigneesRaw = getUserNickname(task)
        this.updatedBy = task.updater.id
        this.updatedAt = task.updatedAt
    }

    companion object {
        private fun getSearchContent(task: TaskJpaEntity): String =
            listOfNotNull(task.title, task.description, getUserNickname(task)).joinToString(" ")

        private fun getUserNickname(task: TaskJpaEntity): String? =
            task.assignments.takeIf { it.isNotEmpty() }?.joinToString(", ") { it.user.nickname }
    }
}