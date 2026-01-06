package com.vgerbot.authorization.api

/**
 * 授权策略类型
 * 支持多种访问控制模型
 */
enum class PolicyType {
    /**
     * RBAC (Role-Based Access Control) - 基于角色的访问控制
     * 适用场景：传统的企业应用，权限通过角色分配
     */
    RBAC,
    
    /**
     * ACL (Access Control List) - 访问控制列表
     * 适用场景：需要精细化的用户-资源权限控制
     */
    ACL,
    
    /**
     * ABAC (Attribute-Based Access Control) - 基于属性的访问控制
     * 适用场景：需要复杂的条件判断，如时间、IP、用户属性等
     */
    ABAC,
    
    /**
     * RBAC with Domains (多租户RBAC)
     * 适用场景：多租户系统，每个租户有独立的角色权限体系
     */
    RBAC_WITH_DOMAINS
}

