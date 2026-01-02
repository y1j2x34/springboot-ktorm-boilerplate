create table `spring-boot-kt`.`user`
(
    id           int auto_increment primary key,
    username     varchar(32)                           not null comment '用户名（唯一）',
    email        varchar(128)                          not null comment '邮箱',
    phone_number varchar(20)                               null comment '手机号',
    password     char(60)                              not null comment '密码（=BCrypt(MD5(明文), salt)）',
    created_at   timestamp default current_timestamp()     null comment '创建时间',
    updated_at   timestamp null on update current_timestamp comment '更新时间',
    is_deleted   tinyint(1) default 0 not null comment '是否删除：0-否, 1-是',
    constraint user_pk unique (username),
    constraint user_pk3 unique (email),
    constraint user_pk_phone unique (phone_number),
    index idx_is_deleted (is_deleted)
) comment '用户表';

-- ================================================
-- 用户测试数据
-- ================================================
-- 注意：密码字段需要通过应用程序的加密逻辑生成，这里使用占位符
INSERT INTO `spring-boot-kt`.`user` (`username`, `email`, `phone_number`, `password`) VALUES
('admin', 'admin@example.com', '13800138000', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('john_doe', 'john.doe@example.com', '13800138001', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('jane_smith', 'jane.smith@example.com', '13800138002', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('mike_wilson', 'mike.wilson@example.com', '13800138003', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('sarah_jones', 'sarah.jones@example.com', NULL, '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('david_brown', 'david.brown@example.com', '13800138005', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('emily_davis', 'emily.davis@example.com', '13800138006', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('chris_miller', 'chris.miller@example.com', NULL, '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('lisa_taylor', 'lisa.taylor@example.com', '13800138008', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a'),
('robert_anderson', 'robert.anderson@example.com', '13800138009', '$2a$10$k83EOZ1Y8lud/6GOzOtGWO2O6y/dRmbC1Mo2LiKh695qcx1mZyd1a')
ON DUPLICATE KEY UPDATE `username` = VALUES(`username`);


-- ================================================
-- RBAC 模块数据库初始化脚本
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

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`role_permission` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `role_id` INT NOT NULL COMMENT '角色ID',
    `permission_id` INT NOT NULL COMMENT '权限ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_permission_id` (`permission_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
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

INSERT INTO `user_role` (`user_id`, `role_id`)
SELECT
    (SELECT id FROM `user` WHERE username = 'admin'),
    (SELECT id FROM `role` WHERE code = 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

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
    `description` VARCHAR(500) NULL COMMENT '租户描述',
    `email_domains` VARCHAR(500) NULL COMMENT '邮箱域名（支持匹配语法，例如：baidu.{com,cn} 表示支持 baidu.com, baidu.cn，多个用逗号分隔）',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `created_by` INT NULL COMMENT '创建人ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` INT NULL COMMENT '更新人ID',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    INDEX `idx_code` (`code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_created_by` (`created_by`),
    INDEX `idx_updated_by` (`updated_by`),
    CONSTRAINT `fk_tenant_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_tenant_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='租户表';

-- 用户租户关联表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_tenant` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `tenant_id` INT NOT NULL COMMENT '租户ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tenant` (`user_id`, `tenant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_user_tenant_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_tenant_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户租户关联表';

-- 用户权限关联表（ACL 模式，需要在 tenant 表之后创建）
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user_permission` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '用户ID',
    `permission_id` INT NOT NULL COMMENT '权限ID',
    `tenant_id` INT NULL COMMENT '租户ID（可选，支持多租户 ACL）',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_permission_tenant` (`user_id`, `permission_id`, `tenant_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_permission_id` (`permission_id`),
    INDEX `idx_tenant_id` (`tenant_id`),
    INDEX `idx_is_deleted` (`is_deleted`),
    CONSTRAINT `fk_user_permission_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_permission_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenant` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户权限关联表（ACL 模式）';

-- ================================================
-- Tenant 模块初始化数据
-- ================================================

-- 插入示例租户
INSERT INTO `tenant` (`code`, `name`, `description`, `email_domains`, `status`) VALUES
    ('tenant_demo', '演示租户', '用于演示和测试的租户', 'demo.com,example.com', 1),
    ('tenant_test', '测试租户', '专用测试环境租户', 'test.{com,cn,net}', 1),
    ('tenant_company_a', 'A公司', 'A公司企业租户，支持主域名和所有子域名', 'company-a.{com,cn},*.company-a.com', 1),
    ('tenant_company_b', 'B公司', 'B公司企业租户，支持多个域名扩展', 'comp.{com,cn,org},company-b.com', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`), `email_domains` = VALUES(`email_domains`);

-- 为已存在的用户分配租户（假设用户表已有数据）
-- 注意：这里使用 INSERT IGNORE 避免重复插入
-- 实际使用中，应根据具体业务逻辑调整
INSERT IGNORE INTO `user_tenant` (`user_id`, `tenant_id`)
SELECT u.id, t.id
FROM `user` u
JOIN `tenant` t ON t.code = 'tenant_demo'
WHERE u.username IN ('john_doe', 'jane_smith');
;


CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`dict_type` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典类型ID',
    dict_code VARCHAR(100) NOT NULL UNIQUE COMMENT '字典编码（唯一标识）',
    dict_name VARCHAR(200) NOT NULL COMMENT '字典名称',
    dict_category VARCHAR(100) COMMENT '字典分类',
    value_type VARCHAR(50) DEFAULT 'STRING' COMMENT '值类型：STRING-字符串, INTEGER-整数, DECIMAL-小数, DATE-日期, BOOLEAN-布尔',
    validation_rule VARCHAR(500) COMMENT '值校验规则（使用JSON格式存储，例如：{"type": "regex", "pattern": "^[a-zA-Z0-9]+$"}）',
    validation_message VARCHAR(500) COMMENT '校验失败提示信息',
    is_tree TINYINT(1) DEFAULT 0 COMMENT '是否树形结构：0-否, 1-是',
    description TEXT COMMENT '字典描述',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-停用, 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_by INT COMMENT '创建人',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by INT COMMENT '更新人',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    remark VARCHAR(500) COMMENT '备注',
    
    INDEX idx_dict_code (dict_code),
    INDEX idx_dict_category (dict_category),
    INDEX idx_status (status),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型定义表';

CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`dict_data` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典数据ID',
    dict_type_id BIGINT NOT NULL COMMENT '字典类型ID（外键）',
    dict_code VARCHAR(100) NOT NULL COMMENT '字典编码（来自字典类型表）',
    data_value VARCHAR(500) NOT NULL COMMENT '字典值（实际存储的值）',
    data_label VARCHAR(500) NOT NULL COMMENT '字典标签（显示文本）',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID（树形结构使用，0表示根节点）',
    level INT DEFAULT 1 COMMENT '层级深度（1表示根级别）',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认值：0-否, 1-是',
    status TINYINT(1) DEFAULT 1 COMMENT '状态：0-停用, 1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序顺序',
    created_by INT COMMENT '创建人',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by INT COMMENT '更新人',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    remark VARCHAR(500) COMMENT '备注',
    
    INDEX idx_dict_type_id (dict_type_id),
    INDEX idx_dict_code (dict_code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_data_value (data_value),
    INDEX idx_is_deleted (is_deleted),
    UNIQUE KEY uk_dict_code_value (dict_code, data_value),
    
    CONSTRAINT fk_dict_data_type FOREIGN KEY (dict_type_id) 
        REFERENCES dict_type(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- ================================================
-- 字典模块初始化数据
-- ================================================



-- 插入示例字典类型
INSERT INTO `dict_type` (`dict_code`, `dict_name`, `dict_category`, `value_type`, `is_tree`, `description`, `status`, `sort_order`, `created_at`, `updated_at`) VALUES
('password-strategy', '密码策略', 'security', 'JSON', 0, '密码策略定义', 1, 1, NOW(), NOW()),
('lock-strategy', '锁定策略', 'security', 'JSON', 0, '锁定策略定义', 1, 1, NOW(), NOW()),
('enable-lock-strategy', '是否启用锁定策略', 'security', 'BOOLEAN', 0, '是否启用锁定策略', 1, 1, NOW(), NOW()),
('email-blacklist', '邮箱黑名单', 'security', 'List<String>', 0, '邮箱黑名单定义', 1, 1, NOW(), NOW()),
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

INSERT INTO `dict_data` (`dict_type_id`, `dict_code`, `data_value`, `data_label`, `parent_id`, `level`, `is_default`, `status`, `sort_order`, `created_at`, `updated_at`) VALUES
((SELECT id FROM `dict_type` WHERE dict_code = 'password-strategy'), 'password-strategy', '{"minLength": 8, "maxLength": 32, "refuce": ["leaked","simple","user-info", "custom:xxxx"]}', '密码策略1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'lock-strategy'), 'lock-strategy', '{"maxAttempts": 5, "lockDuration": 300, "unlockBy": ["email", "phone", "username"]}', '锁定策略1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'enable-lock-strategy'), 'enable-lock-strategy', 'true', '启用', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'email-blacklist'), 'email-blacklist', 'example.com', '邮箱黑名单1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'phone-blacklist'), 'phone-blacklist', '13800138000', '电话黑名单1', 0, 1, 1, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `data_label` = VALUES(`data_label`);
