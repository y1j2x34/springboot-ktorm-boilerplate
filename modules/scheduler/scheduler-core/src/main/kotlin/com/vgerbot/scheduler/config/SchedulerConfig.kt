package com.vgerbot.scheduler.config

import com.vgerbot.scheduler.manager.TaskSchedulerManager
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

/**
 * 调度器配置类
 */
@Configuration
class SchedulerConfig {
    
    private val logger = LoggerFactory.getLogger(SchedulerConfig::class.java)
    
    /**
     * 配置任务调度器
     */
    @Bean
    fun taskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = 10
        scheduler.setThreadNamePrefix("scheduled-task-")
        scheduler.setWaitForTasksToCompleteOnShutdown(true)
        scheduler.setAwaitTerminationSeconds(60)
        scheduler.initialize()
        return scheduler
    }
    
    /**
     * 应用启动时加载所有启用的任务
     */
    @Bean
    fun schedulerInitializer(taskSchedulerManager: TaskSchedulerManager): ApplicationRunner {
        return ApplicationRunner {
            logger.info("初始化定时任务调度器...")
            taskSchedulerManager.reloadAllTasks()
            logger.info("定时任务调度器初始化完成")
        }
    }
}

