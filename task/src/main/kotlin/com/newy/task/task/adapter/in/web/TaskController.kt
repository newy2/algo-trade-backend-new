package com.newy.task.task.adapter.`in`.web

import com.newy.task.spring.auth.annotation.CurrentUser
import com.newy.task.spring.auth.annotation.LoginRequired
import com.newy.task.spring.auth.model.LoginUser
import com.newy.task.task.adapter.`in`.web.model.CreateTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.CreateTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.DeleteTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.GetTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.GetTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.SearchTaskWebRequest
import com.newy.task.task.adapter.`in`.web.model.SearchTaskWebResponse
import com.newy.task.task.adapter.`in`.web.model.UpdateTaskWebRequest
import com.newy.task.task.port.`in`.CreateTaskInPort
import com.newy.task.task.port.`in`.DeleteTaskInPort
import com.newy.task.task.port.`in`.GetTaskInPort
import com.newy.task.task.port.`in`.SearchTaskInPort
import com.newy.task.task.port.`in`.UpdateTaskInPort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@LoginRequired
@RestController
@RequestMapping("/api/v1")
class TaskController(
    private val createTaskInPort: CreateTaskInPort,
    private val deleteTaskInPort: DeleteTaskInPort,
    private val getTaskInPort: GetTaskInPort,
    private val updateTaskInPort: UpdateTaskInPort,
    private val searchTaskInPort: SearchTaskInPort,
) {
    @PostMapping("/tasks")
    fun create(
        @CurrentUser currentUser: LoginUser,
        @RequestBody requestBody: CreateTaskWebRequest
    ): ResponseEntity<CreateTaskWebResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            CreateTaskWebResponse(
                id = createTaskInPort.create(requestBody.toInPortModel(currentUser.id)),
                message = "Task가 생성되었습니다."
            )
        )
    }

    @DeleteMapping("/tasks/{taskId}")
    fun delete(
        @PathVariable taskId: Long,
    ): ResponseEntity<Void> {
        deleteTaskInPort.delete(DeleteTaskWebRequest(taskId).toInPortModel())
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/tasks/{taskId}")
    fun get(
        @PathVariable taskId: Long,
    ): ResponseEntity<GetTaskWebResponse> =
        ResponseEntity.ok(
            GetTaskWebResponse.fromDomainModel(getTaskInPort.get(GetTaskWebRequest(taskId).toInPortModel()))
        )

    @PatchMapping("/tasks/{taskId}")
    fun update(
        @CurrentUser currentUser: LoginUser,
        @PathVariable taskId: Long,
        @RequestBody requestBody: UpdateTaskWebRequest,
    ): ResponseEntity<Void> {
        updateTaskInPort.update(requestBody.toInPortModel(currentUserId = currentUser.id, taskId = taskId))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/tasks")
    fun search(
        request: SearchTaskWebRequest,
    ): ResponseEntity<SearchTaskWebResponse> =
        ResponseEntity.ok(
            SearchTaskWebResponse.fromDomainModel(searchTaskInPort.search(request.toInPortModel()))
        )
}