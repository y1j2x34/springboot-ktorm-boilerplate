package com.vgerbot.postgrest.config

import com.vgerbot.authorization.api.AuthorizationService
import com.vgerbot.postgrest.rls.RowLevelSecurityProvider
import org.ktorm.database.Database
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean

/**
 * PostgREST 查询自动配置
 */
@AutoConfiguration
@ConditionalOnClass(PostgrestQueryAutoConfiguration::class)
class PostgrestQueryAutoConfiguration {
    
    @Bean
    fun rowLevelSecurityProvider(
        authorizationService: AuthorizationService
    ): RowLevelSecurityProvider {
        return RowLevelSecurityProvider(authorizationService)
    }
}

