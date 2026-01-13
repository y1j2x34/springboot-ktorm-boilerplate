package com.vgerbot.scheduler.controller

import com.vgerbot.common.controller.*
import com.vgerbot.common.exception.NotFoundException
import com.vgerbot.scheduler.dto.CreateScheduledTaskDto
import com.vgerbot.scheduler.dto.ExecuteTaskDto
import com.vgerbot.scheduler.dto.ScheduledTaskDto
import com.vgerbot.scheduler.dto.UpdateScheduledTaskDto
import com.vgerbot.scheduler.service.ScheduledTaskService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 定时任务控制器
 * 提供定时任务的REST API
 */
@Tag(name = "Scheduled Task", description = "定时任务管理API")
@RestController
@RequestMapping("/scheduler/tasks")
class ScheduledTaskController(
    private val scheduledTaskService: ScheduledTaskService
) {
    
    @Operation(summary = "创建定时任务", description = "创建一个新的定时任务")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "任务创建成功"),
        ApiResponse(responseCode = "400", description = "请求参数错误"),
        ApiResponse(responseCode = "409", description = "任务名称已存在")
    )
    @PostMapping
    fun createTask(
        @Parameter(description = "任务创建数据", required = true)
        @Valid @RequestBody dto: CreateScheduledTaskDto
    ): ResponseEntity<Map<String, Any>> {
        val task = scheduledTaskService.createTask(dto)
            ?: throw NotFoundException("任务创建失败")
        return task.created("任务创建成功")
    }
    
    @Operation(summary = "更新定时任务", description = "更新一个已存在的定时任务")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "任务更新成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @PutMapping("/{id}")
    fun updateTask(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long,
        @Parameter(description = "任务更新数据", required = true)
        @RequestBody dto: UpdateScheduledTaskDto
    ): ResponseEntity<Map<String, Any>> {
        val updated = scheduledTaskService.updateTask(id, dto)
        if (!updated) {
            throw NotFoundException("任务不存在")
        }
        return ok("任务更新成功")
    }
    
    @Operation(summary = "删除定时任务", description = "删除一个定时任务")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "任务删除成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @DeleteMapping("/{id}")
    fun deleteTask(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val deleted = scheduledTaskService.deleteTask(id)
        if (!deleted) {
            throw NotFoundException("任务不存在")
        }
        return ok("任务删除成功")
    }
    
    @Operation(summary = "获取任务详情", description = "根据ID获取任务详情")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "获取成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @GetMapping("/{id}")
    fun getTaskById(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val task = scheduledTaskService.getTaskById(id)
            ?: throw NotFoundException("任务不存在")
        return task.ok()
    }
    
    @Operation(summary = "获取所有任务", description = "获取所有定时任务列表")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @GetMapping
    fun getAllTasks(): ResponseEntity<Map<String, Any>> {
        val tasks = scheduledTaskService.getAllTasks()
        return tasks.ok()
    }
    
    @Operation(summary = "启用任务", description = "启用一个定时任务")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "任务启用成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @PostMapping("/{id}/enable")
    fun enableTask(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val enabled = scheduledTaskService.enableTask(id)
        if (!enabled) {
            throw NotFoundException("任务不存在")
        }
        return ok("任务启用成功")
    }
    
    @Operation(summary = "停用任务", description = "停用一个定时任务")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "任务停用成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @PostMapping("/{id}/disable")
    fun disableTask(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val disabled = scheduledTaskService.disableTask(id)
        if (!disabled) {
            throw NotFoundException("任务不存在")
        }
        return ok("任务停用成功")
    }
    
    @Operation(summary = "立即执行任务", description = "立即执行一个定时任务（不等待调度）")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "任务执行成功"),
        ApiResponse(responseCode = "404", description = "任务不存在")
    )
    @PostMapping("/{id}/execute")
    fun executeTask(
        @Parameter(description = "任务ID", required = true)
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any>> {
        val executed = scheduledTaskService.executeTask(id)
        if (!executed) {
            throw NotFoundException("任务不存在或执行失败")
        }
        return ok("任务执行成功")
    }
    
    @Operation(summary = "重新加载所有任务", description = "重新加载所有任务到调度器")
    @ApiResponse(responseCode = "200", description = "重新加载成功")
    @PostMapping("/reload")
    fun reloadTasks(): ResponseEntity<Map<String, Any>> {
        scheduledTaskService.reloadTasks()
        return ok("任务重新加载成功")
    }
}

