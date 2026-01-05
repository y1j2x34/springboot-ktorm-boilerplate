-- ================================================
-- Authorization 模块 - Role 表初始化脚本
-- ================================================

-- 角色表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`role` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    `description` VARCHAR(255) COMMENT '角色描述',
    `created_by` INT NULL COMMENT '创建人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` INT NULL COMMENT '更新人ID',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_created_by` (`created_by`),
    INDEX `idx_updated_by` (`updated_by`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_role_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_role_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- ================================================
-- 初始化数据
-- ================================================

-- 插入默认角色
INSERT INTO `role` (`name`, `code`, `description`) VALUES
    ('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
    ('普通用户', 'ROLE_USER', '普通用户，拥有基础权限')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

