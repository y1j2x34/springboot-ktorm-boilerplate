package com.vgerbot.tenant.dto

import java.time.Instant

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

data class TenantDto(
    val id: Int,
    val code: String,
    val name: String,
    val description: String? = null,
    val emailDomains: List<String> = emptyList(),
    val status: TenantStatus = TenantStatus.Available,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)

