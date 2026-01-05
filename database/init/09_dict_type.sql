-- ================================================
-- Dict 模块 - DictType 表初始化脚本
-- ================================================

-- 字典类型定义表
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

-- ================================================
-- 初始化数据
-- ================================================

-- 插入示例字典类型
INSERT INTO `dict_type` (`dict_code`, `dict_name`, `dict_category`, `value_type`, `is_tree`, `description`, `status`, `sort_order`, `created_at`, `updated_at`) VALUES
('password-strategy', '密码策略', 'security', 'JSON', 0, '密码策略定义', 1, 1, NOW(), NOW()),
('lock-strategy', '锁定策略', 'security', 'JSON', 0, '锁定策略定义', 1, 1, NOW(), NOW()),
('enable-lock-strategy', '是否启用锁定策略', 'security', 'BOOLEAN', 0, '是否启用锁定策略', 1, 1, NOW(), NOW()),
('email-blacklist', '邮箱黑名单', 'security', 'List<String>', 0, '邮箱黑名单定义', 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `dict_name` = VALUES(`dict_name`);

