package com.vgerbot.captcha.vo

data class CaptchaEntity(var createdAt: Long = 0,
                         var expiresInSeconds: Long = 0,
                         var value: String? = null) {
    open fun isNotExpired() = this.createdAt + this.expiresInSeconds * 1000 < java.util.Date().time;
}