package com.vgerbot.logto

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.management.ManagementFactory

@RestController
class CheckController {
    @GetMapping("/check")
    fun check(): Map<String, Any> {
        val runtime = ManagementFactory.getRuntimeMXBean()
        val os = ManagementFactory.getOperatingSystemMXBean()
        val memory = Runtime.getRuntime()

        return mapOf(
            "os" to mapOf(
                "name" to System.getProperty("os.name"),
                "version" to System.getProperty("os.version"),
                "arch" to System.getProperty("os.arch"),
                "availableProcessors" to os.availableProcessors
            ),
            "java" to mapOf(
                "version" to System.getProperty("java.version"),
                "vendor" to System.getProperty("java.vendor"),
                "vmName" to runtime.vmName
            ),
            "runtime" to mapOf(
                "uptime" to runtime.uptime,
                "startTime" to runtime.startTime
            ),
            "memory" to mapOf(
                "total" to memory.totalMemory(),
                "free" to memory.freeMemory(),
                "max" to memory.maxMemory()
            )
        )
    }
}