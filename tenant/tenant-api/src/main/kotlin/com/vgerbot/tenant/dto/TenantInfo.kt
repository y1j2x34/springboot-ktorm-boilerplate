package com.vgerbot.tenant.com.vgerbot.tenant.dto

import com.vgerbot.common.utils.EmailDomainMatcher
import com.vgerbot.tenant.com.vgerbot.tenant.model.Tenant
import com.vgerbot.tenant.com.vgerbot.tenant.utils.TenantUtils

enum class TenantStatus {
    Available,
    Disabled;
    companion object {
        fun from(status: Int) = when (status) {
            1 -> Available
            0 -> Disabled
            else -> throw IllegalArgumentException("Invalid Tenant status $status")
        }
    }
}

data class TenantInfo(
    val id: Int,
    val code: String,
    val name: String,
    val description: String,
    val emailDomains: List<String>,
    val status: TenantStatus,
) {
    companion object {
        fun from(model: Tenant) = TenantInfo(
            model.id,
            model.code,
            model.name,
            model.description ?: "",
            EmailDomainMatcher.expandPattern(model.emailDomains ?: "*"),
            TenantStatus.from(model.status)
        )
    }
}
