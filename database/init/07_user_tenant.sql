-- ================================================
-- Tenant 模块 - UserTenant 表初始化脚本
-- ================================================

-- 用户租户关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `tenant_id` INT NOT NULL COMMENT '租户ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tenant` (`user_id`, `tenant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    CONSTRAINT `fk_user_tenant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_tenant_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户租户关联表';

-- ================================================
-- 初始化数据
-- ================================================

-- 为已存在的用户分配租户（假设用户表已有数据）
-- 注意：这里使用 INSERT IGNORE 避免重复插入
-- 实际使用中，应根据具体业务逻辑调整
INSERT IGNORE INTO `user_tenant` (`user_id`, `tenant_id`)
SELECT u.id, t.id
FROM `user` u
JOIN `tenant` t ON t.code = 'tenant_demo'
WHERE u.username IN ('john_doe', 'jane_smith');

