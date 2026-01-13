package com.vgerbot.scheduler.executor.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.scheduler.executor.TaskExecutionResult
import com.vgerbot.scheduler.executor.TaskExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.script.ScriptEngineManager

/**
 * JavaScript脚本执行器
 * 配置示例：
 * {
 *   "script": "console.log('Hello World'); return 'Success';"
 * }
 */
@Component
class SchedulingJavaScriptRunner(
    private val objectMapper: ObjectMapper
) : TaskExecutor {
    
    private val logger = LoggerFactory.getLogger(SchedulingJavaScriptRunner::class.java)
    
    override fun getExecutorType(): String = "SchedulingJavaScriptRunner"
    
    override fun execute(config: String?): TaskExecutionResult {
        try {
            if (config.isNullOrBlank()) {
                return TaskExecutionResult(
                    success = false,
                    message = "JavaScript配置不能为空"
                )
            }
            
            val configMap = objectMapper.readValue(config, Map::class.java) as Map<*, *>
            val script = configMap["script"] as? String
            
            if (script.isNullOrBlank()) {
                return TaskExecutionResult(
                    success = false,
                    message = "JavaScript脚本不能为空"
                )
            }
            
            // 使用ScriptEngineManager执行JavaScript脚本
            // 优先尝试使用GraalVM JavaScript引擎，如果不可用则使用其他引擎
            val engineManager = ScriptEngineManager()
            val engine = engineManager.getEngineByName("graal.js")
                ?: engineManager.getEngineByName("js")
                ?: engineManager.getEngineByName("javascript")
                ?: throw IllegalStateException("无法获取JavaScript引擎")
            
            // 执行脚本
            val result = engine.eval(script)
            
            val resultMessage = when (result) {
                null -> "脚本执行完成，无返回值"
                is String -> result
                else -> result.toString()
            }
            
            logger.info("JavaScript任务执行成功: {}", resultMessage)
            
            return TaskExecutionResult(
                success = true,
                message = resultMessage,
                data = resultMessage
            )
        } catch (e: Exception) {
            logger.error("JavaScript任务执行失败", e)
            return TaskExecutionResult(
                success = false,
                message = "执行失败: ${e.message}"
            )
        }
    }
}

