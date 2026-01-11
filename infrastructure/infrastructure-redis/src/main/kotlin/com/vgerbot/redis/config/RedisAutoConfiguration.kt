package com.vgerbot.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.vgerbot.redis.service.RedisCacheService
import com.vgerbot.redis.service.RedisLockService
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 自动配置类
 * 
 * 自动配置 Redis 相关组件，包括：
 * - RedisTemplate 配置
 * - 分布式锁服务
 * - 缓存服务
 */
@AutoConfiguration
@ConditionalOnClass(RedisConnectionFactory::class)
@EnableConfigurationProperties(RedisProperties::class)
@ComponentScan(basePackages = ["com.vgerbot.redis"])
class RedisAutoConfiguration {
    
    /**
     * 配置 RedisTemplate
     * 使用 JSON 序列化器，支持 Kotlin 对象
     */
    @Bean
    @ConditionalOnMissingBean(name = ["redisTemplate"])
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val objectMapper = ObjectMapper().apply {
            registerKotlinModule()
        }
        
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = connectionFactory
        
        // Key 使用 String 序列化
        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        
        // Value 使用 JSON 序列化
        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)
        template.valueSerializer = jsonSerializer
        template.hashValueSerializer = jsonSerializer
        
        template.afterPropertiesSet()
        return template
    }
    
    /**
     * 配置分布式锁服务
     */
    @Bean
    @ConditionalOnMissingBean
    fun redisLockService(
        redisTemplate: RedisTemplate<String, Any>,
        redisProperties: RedisProperties
    ): RedisLockService {
        return RedisLockService(redisTemplate, redisProperties)
    }
    
    /**
     * 配置缓存服务
     */
    @Bean
    @ConditionalOnMissingBean
    fun redisCacheService(
        redisTemplate: RedisTemplate<String, Any>,
        redisProperties: RedisProperties
    ): RedisCacheService {
        return RedisCacheService(redisTemplate, redisProperties)
    }
}

