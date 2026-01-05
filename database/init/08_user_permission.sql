-- ================================================
-- Tenant 模块 - UserPermission 表初始化脚本（ACL 模式）
-- ================================================

-- 用户权限关联表（ACL 模式，需要在 tenant 表之后创建）
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_permission` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `permission_id` INT NOT NULL COMMENT '权限ID',
    `tenant_id` INT NULL COMMENT '租户ID（可选，支持多租户 ACL）',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission_tenant` (`user_id`, `permission_id`, `tenant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_permission_id` (`permission_id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    CONSTRAINT `fk_user_permission_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_permission_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限关联表（ACL 模式）';

