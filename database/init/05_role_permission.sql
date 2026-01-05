-- ================================================
-- Authorization 模块 - RolePermission 表初始化脚本
-- ================================================

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`role_permission` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `role_id` INT NOT NULL COMMENT '角色ID',
    `permission_id` INT NOT NULL COMMENT '权限ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ================================================
-- 初始化数据
-- ================================================

-- 为管理员角色分配所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN'),
    p.id
FROM `permission` p
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 为普通用户角色分配基础权限
INSERT INTO `role_permission` (`role_id`, `permission_id`)
SELECT
    (SELECT id FROM `role` WHERE code = 'ROLE_USER'),
    p.id
FROM `permission` p
WHERE p.code IN ('user:read')
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

