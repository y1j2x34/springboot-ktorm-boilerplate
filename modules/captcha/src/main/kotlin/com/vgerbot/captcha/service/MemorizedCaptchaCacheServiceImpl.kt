package com.vgerbot.captcha.service

import com.anji.captcha.service.CaptchaCacheService
import com.vgerbot.captcha.vo.CaptchaEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MemorizedCaptchaCacheServiceImpl : CaptchaCacheService {
    private val cache: MutableMap<String, CaptchaEntity> = ConcurrentHashMap<String, CaptchaEntity>()
    override fun set(key: String, value: String, expiresInSeconds: Long) {
        cache[key] = CaptchaEntity(Date().time, expiresInSeconds, value)
    }

    override fun exists(key: String): Boolean {
        val entity: CaptchaEntity = cache[key] ?: return false
        return entity.isNotExpired()
    }

    override fun delete(key: String) {
        cache.remove(key)
    }

    override fun get(key: String): String? {
        val entity: CaptchaEntity? = cache[key]
        return if (entity != null && entity.isNotExpired()) {
            entity.value
        } else null
    }

    override fun type() = "local"
}
