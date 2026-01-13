package com.vgerbot.scheduler.executor.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.vgerbot.scheduler.executor.TaskExecutionResult
import com.vgerbot.scheduler.executor.TaskExecutor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.lang.management.ManagementFactory
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.file.FileStore
import java.nio.file.FileSystems
import java.nio.file.Files

/**
 * 健康检查执行器
 * 配置示例：
 * {
 *   "checkServices": [
 *     {"host": "localhost", "port": 8080, "name": "Web服务"},
 *     {"host": "localhost", "port": 3306, "name": "数据库"}
 *   ],
 *   "checkDisk": true,
 *   "diskThreshold": 80,
 *   "alertEmail": "admin@example.com"
 * }
 */
@Component
class SchedulingHealthChecker(
    private val objectMapper: ObjectMapper
) : TaskExecutor {
    
    private val logger = LoggerFactory.getLogger(SchedulingHealthChecker::class.java)
    
    override fun getExecutorType(): String = "SchedulingHealthChecker"
    
    override fun execute(config: String?): TaskExecutionResult {
        try {
            val configMap = if (config.isNullOrBlank()) {
                emptyMap<String, Any>()
            } else {
                objectMapper.readValue(config, Map::class.java) as Map<*, *>
            }
            
            val results = mutableListOf<String>()
            val alerts = mutableListOf<String>()
            
            // 检查服务状态
            @Suppress("UNCHECKED_CAST")
            val checkServices = configMap["checkServices"] as? List<Map<String, Any>>
            if (!checkServices.isNullOrEmpty()) {
                checkServices.forEach { service ->
                    val host = service["host"] as? String ?: "localhost"
                    val port = (service["port"] as? Number)?.toInt() ?: 80
                    val name = service["name"] as? String ?: "$host:$port"
                    
                    val isHealthy = checkService(host, port)
                    if (isHealthy) {
                        results.add("服务 $name ($host:$port) 正常")
                    } else {
                        val alert = "服务 $name ($host:$port) 不可用"
                        results.add(alert)
                        alerts.add(alert)
                    }
                }
            }
            
            // 检查磁盘使用率
            val checkDisk = configMap["checkDisk"] as? Boolean ?: false
            if (checkDisk) {
                val diskThreshold = (configMap["diskThreshold"] as? Number)?.toInt() ?: 80
                val diskInfo = checkDiskUsage(diskThreshold)
                results.addAll(diskInfo.messages)
                alerts.addAll(diskInfo.alerts)
            }
            
            // 检查内存使用率
            val checkMemory = configMap["checkMemory"] as? Boolean ?: false
            if (checkMemory) {
                val memoryThreshold = (configMap["memoryThreshold"] as? Number)?.toInt() ?: 80
                val memoryInfo = checkMemoryUsage(memoryThreshold)
                results.addAll(memoryInfo.messages)
                alerts.addAll(memoryInfo.alerts)
            }
            
            val message = if (results.isEmpty()) {
                "健康检查完成，未配置检查项"
            } else {
                results.joinToString("\n")
            }
            
            val success = alerts.isEmpty()
            
            // 如果有告警，记录日志
            if (!success) {
                logger.warn("健康检查发现告警:\n{}", alerts.joinToString("\n"))
            }
            
            return TaskExecutionResult(
                success = success,
                message = message,
                data = mapOf(
                    "alerts" to alerts,
                    "results" to results
                )
            )
        } catch (e: Exception) {
            logger.error("健康检查执行失败", e)
            return TaskExecutionResult(
                success = false,
                message = "执行失败: ${e.message}"
            )
        }
    }
    
    /**
     * 检查服务是否可用
     */
    private fun checkService(host: String, port: Int, timeout: Int = 3000): Boolean {
        return try {
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), timeout)
            socket.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查磁盘使用率
     */
    private fun checkDiskUsage(threshold: Int): CheckResult {
        val messages = mutableListOf<String>()
        val alerts = mutableListOf<String>()
        
        try {
            FileSystems.getDefault().fileStores.forEach { store: FileStore ->
                val totalSpace = store.totalSpace
                val usableSpace = store.usableSpace
                val usedSpace = totalSpace - usableSpace
                val usagePercent = (usedSpace * 100.0 / totalSpace).toInt()
                
                val path = store.name()
                val totalGB = totalSpace / (1024.0 * 1024.0 * 1024.0)
                val usedGB = usedSpace / (1024.0 * 1024.0 * 1024.0)
                val usableGB = usableSpace / (1024.0 * 1024.0 * 1024.0)
                
                val message = "磁盘 $path: 已使用 ${usagePercent}% (${String.format("%.2f", usedGB)}GB / ${String.format("%.2f", totalGB)}GB), 可用 ${String.format("%.2f", usableGB)}GB"
                messages.add(message)
                
                if (usagePercent >= threshold) {
                    alerts.add("警告: 磁盘 $path 使用率 ${usagePercent}% 超过阈值 ${threshold}%")
                }
            }
        } catch (e: Exception) {
            messages.add("磁盘检查失败: ${e.message}")
        }
        
        return CheckResult(messages, alerts)
    }
    
    /**
     * 检查内存使用率
     */
    private fun checkMemoryUsage(threshold: Int): CheckResult {
        val messages = mutableListOf<String>()
        val alerts = mutableListOf<String>()
        
        try {
            val memoryMXBean = ManagementFactory.getMemoryMXBean()
            val heapMemoryUsage = memoryMXBean.heapMemoryUsage
            
            val used = heapMemoryUsage.used
            val max = heapMemoryUsage.max
            val usagePercent = if (max > 0) {
                (used * 100.0 / max).toInt()
            } else {
                0
            }
            
            val usedMB = used / (1024.0 * 1024.0)
            val maxMB = max / (1024.0 * 1024.0)
            
            val message = "堆内存使用率: ${usagePercent}% (${String.format("%.2f", usedMB)}MB / ${String.format("%.2f", maxMB)}MB)"
            messages.add(message)
            
            if (usagePercent >= threshold) {
                alerts.add("警告: 堆内存使用率 ${usagePercent}% 超过阈值 ${threshold}%")
            }
        } catch (e: Exception) {
            messages.add("内存检查失败: ${e.message}")
        }
        
        return CheckResult(messages, alerts)
    }
    
    private data class CheckResult(
        val messages: List<String>,
        val alerts: List<String>
    )
}

