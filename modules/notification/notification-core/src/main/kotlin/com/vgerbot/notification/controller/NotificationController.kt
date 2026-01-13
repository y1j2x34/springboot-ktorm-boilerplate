package com.vgerbot.notification.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.common.controller.*
import com.vgerbot.notification.dto.SendNotificationDto
import com.vgerbot.notification.provider.NotificationRequest
import com.vgerbot.notification.service.NotificationService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/notification")
class NotificationController(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    
    /**
     * 发送通知
     */
    @PostMapping("/send")
    fun sendNotification(
        @Valid @RequestBody dto: SendNotificationDto
    ): ResponseEntity<Map<String, Any>> {
        // 转换DTO为Request
        val request = dto.toNotificationRequest(objectMapper)
        
        // 发送通知
        val result = notificationService.send(request)
        
        return result.ok()
    }
    
    /**
     * 批量发送通知
     */
    @PostMapping("/send/batch")
    fun sendBatchNotification(
        @Valid @RequestBody dtos: List<SendNotificationDto>
    ): ResponseEntity<Map<String, Any>> {
        val requests = dtos.map { it.toNotificationRequest(objectMapper) }
        val results = notificationService.sendBatch(requests)
        
        return results.ok()
    }
    
    /**
     * 发送邮件（便捷接口）
     */
    @PostMapping("/email")
    fun sendEmail(
        @RequestParam to: String,
        @RequestParam subject: String,
        @RequestParam content: String
    ): ResponseEntity<Map<String, Any>> {
        val result = notificationService.sendEmail(to, subject, content)
        return result.ok()
    }
    
    /**
     * 发送短信（便捷接口）
     */
    @PostMapping("/sms")
    fun sendSms(
        @RequestParam to: String,
        @RequestParam content: String,
        @RequestParam(required = false) templateId: String? = null
    ): ResponseEntity<Map<String, Any>> {
        val result = notificationService.sendSms(to, content, templateId)
        return result.ok()
    }
    
    /**
     * 检查通知类型是否可用
     */
    @GetMapping("/type/{type}/available")
    fun checkTypeAvailable(
        @PathVariable type: String
    ): ResponseEntity<Map<String, Any>> {
        val notificationType = try {
            com.vgerbot.notification.NotificationType.valueOf(type.uppercase())
        } catch (e: IllegalArgumentException) {
            return mapOf("available" to false, "error" to "无效的通知类型").ok()
        }
        
        val available = notificationService.isTypeAvailable(notificationType)
        return mapOf("available" to available).ok()
    }
}

/**
 * 扩展函数：将DTO转换为NotificationRequest
 */
private fun SendNotificationDto.toNotificationRequest(
    objectMapper: ObjectMapper
): NotificationRequest {
    val templateParams = templateParams?.let {
        try {
            objectMapper.readValue(it, Map::class.java) as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
    
    val extraParams = extraParams?.let {
        try {
            objectMapper.readValue(it, Map::class.java) as? Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
    
    return NotificationRequest(
        type = type,
        recipient = recipient,
        subject = subject,
        content = content,
        templateId = templateId,
        templateParams = templateParams,
        extraParams = extraParams,
        priority = priority,
        scheduledTime = scheduledTime
    )
}

