package com.vgerbot.wechat

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

/**
 * 微信认证模块配置
 * 
 * 启用此模块需要配置 wechat.enabled=true
 */
@Configuration
@ConditionalOnProperty(prefix = "wechat", name = ["enabled"], havingValue = "true", matchIfMissing = false)
@ComponentScan(basePackages = ["com.vgerbot.wechat"])
class WechatConfiguration

