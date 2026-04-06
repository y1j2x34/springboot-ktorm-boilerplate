-- ================================================
-- System 模块 - 菜单表初始化脚本
-- ================================================

CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id` BIGINT NULL DEFAULT 0 COMMENT '父菜单ID',
    `name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
    `path` VARCHAR(255) NULL COMMENT '路由地址',
    `component` VARCHAR(255) NULL COMMENT '组件路径',
    `permission` VARCHAR(100) NULL COMMENT '权限标识',
    `icon` VARCHAR(50) NULL COMMENT '菜单图标',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '显示排序',
    `type` INT NOT NULL DEFAULT 1 COMMENT '菜单类型（0:目录, 1:菜单, 2:按钮）',
    `visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '菜单状态（1显示 0隐藏）',
    `created_by` INT NULL COMMENT '创建人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` INT NULL COMMENT '更新人ID',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_permission` (`permission`),
    INDEX `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_sys_menu_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_sys_menu_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- ================================================
-- 初始化数据
-- ================================================
INSERT INTO `spring-boot-kt`.`sys_menu` (`id`, `parent_id`, `name`, `path`, `component`, `permission`, `icon`, `sort_order`, `type`, `visible`) VALUES
    (1, 0, '系统管理', '/system', 'Layout', NULL, 'system', 1, 0, 1),
    (2, 1, '用户管理', 'user', 'system/user/index', 'system:user:list', 'user', 1, 1, 1),
    (3, 1, '角色管理', 'role', 'system/role/index', 'system:role:list', 'peoples', 2, 1, 1),
    (4, 1, '菜单管理', 'menu', 'system/menu/index', 'system:menu:list', 'tree-table', 3, 1, 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);
