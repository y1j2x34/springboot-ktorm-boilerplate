package com.vgerbot.dynamictable.config

import com.vgerbot.dynamictable.api.DynamicTableManagementService
import com.vgerbot.dynamictable.api.DynamicTableRegistry
import com.vgerbot.dynamictable.controller.DynamicTableManagementController
import com.vgerbot.dynamictable.core.DynamicTableManager
import com.vgerbot.dynamictable.registry.DefaultDynamicTableRegistry
import com.vgerbot.dynamictable.service.DynamicTableManagementServiceImpl
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import javax.sql.DataSource

/**
 * Auto-configuration for Dynamic Table module
 * 
 * This configuration automatically sets up all components needed for
 * runtime table management when the module is included as a dependency.
 */
@AutoConfiguration
@ComponentScan(basePackages = ["com.vgerbot.dynamictable"])
class DynamicTableAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    fun dynamicTableManager(dataSource: DataSource): DynamicTableManager {
        return DynamicTableManager(dataSource)
    }
    
    @Bean
    @ConditionalOnMissingBean(DynamicTableRegistry::class)
    fun dynamicTableRegistry(): DynamicTableRegistry {
        return DefaultDynamicTableRegistry()
    }
    
    @Bean
    @ConditionalOnMissingBean(DynamicTableManagementService::class)
    fun dynamicTableManagementService(
        dynamicTableManager: DynamicTableManager,
        dynamicTableRegistry: DynamicTableRegistry
    ): DynamicTableManagementService {
        return DynamicTableManagementServiceImpl(dynamicTableManager, dynamicTableRegistry)
    }
    
    @Bean
    @ConditionalOnMissingBean
    fun dynamicTableManagementController(
        tableManagementService: DynamicTableManagementService
    ): DynamicTableManagementController {
        return DynamicTableManagementController(tableManagementService)
    }
}

