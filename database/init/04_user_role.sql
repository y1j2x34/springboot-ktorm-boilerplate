-- ================================================
-- Authorization 模块 - UserRole 表初始化脚本
-- ================================================

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_role` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `role_id` INT NOT NULL COMMENT '角色ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ================================================
-- 初始化数据
-- ================================================

-- 为 admin 用户分配管理员角色
INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT
    (SELECT id FROM `user` WHERE username = 'admin'),
    (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

