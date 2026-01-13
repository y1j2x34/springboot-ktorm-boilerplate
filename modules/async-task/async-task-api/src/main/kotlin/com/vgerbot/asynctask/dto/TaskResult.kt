package com.vgerbot.asynctask.dto

/**
 * 任务执行结果
 * 
 * @param success 是否成功
 * @param data 结果数据（JSON格式的Map）
 * @param message 结果消息
 */
data class TaskResult(
    val success: Boolean,
    val data: Map<String, Any>? = null,
    val message: String? = null
) {
    companion object {
        /**
         * 创建成功结果
         */
        fun success(data: Map<String, Any>? = null, message: String? = null): TaskResult {
            return TaskResult(success = true, data = data, message = message)
        }
        
        /**
         * 创建失败结果
         */
        fun failure(message: String, data: Map<String, Any>? = null): TaskResult {
            return TaskResult(success = false, data = data, message = message)
        }
    }
}

