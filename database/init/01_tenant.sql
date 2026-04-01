-- ================================================
-- Tenant 模块 - Tenant 表初始化脚本
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

-- ================================================
-- 初始化数据
-- ================================================

-- 插入示例租户
INSERT INTO `tenant` (`code`, `name`, `description`, `email_domains`, `status`) VALUES
    ('tenant_demo', '演示租户', '用于演示和测试的租户', 'demo.com,example.com', 1),
    ('tenant_test', '测试租户', '专用测试环境租户', 'test.{com,cn,net}', 1),
    ('tenant_company_a', 'A公司', 'A公司企业租户，支持主域名和所有子域名', 'company-a.{com,cn},*.company-a.com', 1),
    ('tenant_company_b', 'B公司', 'B公司企业租户，支持多个域名扩展', 'comp.{com,cn,org},company-b.com', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`), `email_domains` = VALUES(`email_domains`);

