create table `spring-boot-kt`.`user`
(
    id         int auto_increment primary key,
    username   varchar(32)                           not null comment '用户名（唯一）',
    email      varchar(128)                          not null comment '邮箱',
    password   varchar(32)                           not null comment '密码（=BCrypt(MD5(明文), salt)）',
    created_at timestamp default current_timestamp() null comment '创建时间',
    constraint user_pk unique (username),
    constraint user_pk3 unique (email)
) comment '用户表';


-- ================================================
-- RBAC 模块数据库初始化脚本
-- ================================================

-- 角色表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`role` (
                                      `id` INT NOT NULL AUTO_INCREMENT,
                                      `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色代码',
    `description` VARCHAR(255) COMMENT '角色描述',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`permission` (
                                            `id` INT NOT NULL AUTO_INCREMENT,
                                            `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限代码',
    `resource` VARCHAR(50) NOT NULL COMMENT '资源',
    `action` VARCHAR(50) NOT NULL COMMENT '操作',
    `description` VARCHAR(255) COMMENT '权限描述',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_resource_action` (`resource`, `action`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_role` (
                                           `id` INT NOT NULL AUTO_INCREMENT,
                                           `user_id` INT NOT NULL COMMENT '用户ID',
                                           `role_id` INT NOT NULL COMMENT '角色ID',
                                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`role_permission` (
                                                 `id` INT NOT NULL AUTO_INCREMENT,
                                                 `role_id` INT NOT NULL COMMENT '角色ID',
                                                 `permission_id` INT NOT NULL COMMENT '权限ID',
                                                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
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

-- 插入默认角色
INSERT INTO `role` (`name`, `code`, `description`) VALUES
                                                       ('管理员', 'ROLE_ADMIN', '系统管理员，拥有所有权限'),
                                                       ('普通用户', 'ROLE_USER', '普通用户，拥有基础权限')
    ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

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

-- ================================================
-- Tenant 模块数据库初始化脚本
-- ================================================

-- 租户表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '租户代码（唯一标识）',
    `name` VARCHAR(100) NOT NULL COMMENT '租户名称',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

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
-- Tenant 模块初始化数据
-- ================================================

-- 插入示例租户
INSERT INTO `tenant` (`code`, `name`, `status`) VALUES
    ('tenant_demo', '演示租户', 1),
    ('tenant_test', '测试租户', 1),
    ('tenant_company_a', 'A公司', 1),
    ('tenant_company_b', 'B公司', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 为已存在的用户分配租户（假设用户表已有数据）
-- 注意：这里使用 INSERT IGNORE 避免重复插入
-- 实际使用中，应根据具体业务逻辑调整
INSERT IGNORE INTO `user_tenant` (`user_id`, `tenant_id`)
SELECT 
    u.id as user_id,
    (SELECT id FROM `tenant` WHERE code = 'tenant_demo') as tenant_id
FROM `user` u
WHERE u.id IN (SELECT MIN(id) FROM `user`)  -- 只为第一个用户分配演示租户
LIMIT 1;

