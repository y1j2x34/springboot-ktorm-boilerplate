-- ================================================
-- Authorization 模块 - Permission 表初始化脚本
-- ================================================

-- 权限表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`permission` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
    `resource` VARCHAR(50) NOT NULL COMMENT '资源',
    `action` VARCHAR(50) NOT NULL COMMENT '操作',
    `description` VARCHAR(255) COMMENT '权限描述',
    `created_by` INT NULL COMMENT '创建人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` INT NULL COMMENT '更新人ID',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用, 0-禁用',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_resource_action` (`resource`, `action`),
    INDEX `idx_created_by` (`created_by`),
    INDEX `idx_updated_by` (`updated_by`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_permission_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_permission_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- ================================================
-- 初始化数据
-- ================================================

-- 插入默认权限
INSERT INTO `permission` (`name`, `code`, `resource`, `action`, `description`) VALUES
-- 用户相关权限
('查看用户', 'user:read', 'user', 'read', '查看用户信息'),
('创建用户', 'user:create', 'user', 'create', '创建新用户'),
('更新用户', 'user:update', 'user', 'update', '更新用户信息'),
('删除用户', 'user:delete', 'user', 'delete', '删除用户'),

-- 角色相关权限
('查看角色', 'role:read', 'role', 'read', '查看角色信息'),
('创建角色', 'role:create', 'role', 'create', '创建新角色'),
('更新角色', 'role:update', 'role', 'update', '更新角色信息'),
('删除角色', 'role:delete', 'role', 'delete', '删除角色'),

-- 权限相关权限
('查看权限', 'permission:read', 'permission', 'read', '查看权限信息'),
('创建权限', 'permission:create', 'permission', 'create', '创建新权限'),
('更新权限', 'permission:update', 'permission', 'update', '更新权限信息'),
('删除权限', 'permission:delete', 'permission', 'delete', '删除权限')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

