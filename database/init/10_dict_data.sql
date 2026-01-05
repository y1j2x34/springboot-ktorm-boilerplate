-- ================================================
-- Dict 模块 - DictData 表初始化脚本
-- ================================================

-- 字典数据表
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
-- 初始化数据
-- ================================================

-- 插入示例字典数据
INSERT INTO `dict_data` (`dict_type_id`, `dict_code`, `data_value`, `data_label`, `parent_id`, `level`, `is_default`, `status`, `sort_order`, `created_at`, `updated_at`) VALUES
((SELECT id FROM `dict_type` WHERE dict_code = 'password-strategy'), 'password-strategy', '{"minLength": 8, "maxLength": 32, "refuce": ["leaked","simple","user-info", "custom:xxxx"]}', '密码策略1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'lock-strategy'), 'lock-strategy', '{"maxAttempts": 5, "lockDuration": 300, "unlockBy": ["email", "phone", "username"]}', '锁定策略1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'enable-lock-strategy'), 'enable-lock-strategy', 'true', '启用', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'email-blacklist'), 'email-blacklist', 'example.com', '邮箱黑名单1', 0, 1, 1, 1, 1, NOW(), NOW()),
((SELECT id FROM `dict_type` WHERE dict_code = 'phone-blacklist'), 'phone-blacklist', '13800138000', '电话黑名单1', 0, 1, 1, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `data_label` = VALUES(`data_label`);

