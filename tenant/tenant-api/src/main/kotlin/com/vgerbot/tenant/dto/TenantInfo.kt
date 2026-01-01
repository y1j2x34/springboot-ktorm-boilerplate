package com.vgerbot.tenant.com.vgerbot.tenant.dto

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
)
