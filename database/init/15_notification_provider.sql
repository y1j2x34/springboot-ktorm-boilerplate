-- 通知提供商配置表
-- 用于存储不同通知提供商的配置信息，支持动态配置和启用/禁用

CREATE TABLE IF NOT EXISTS `notification_provider` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `provider_type` VARCHAR(50) NOT NULL COMMENT '提供商类型（SMTP、ALIYUN_SMS、TENCENT_SMS等）',
    `provider_name` VARCHAR(100) NOT NULL COMMENT '提供商名称',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `supported_types` VARCHAR(200) NOT NULL COMMENT '支持的通知类型，JSON数组格式，如：["EMAIL","SMS"]',
    `config` JSON NOT NULL COMMENT '提供商配置，JSON格式，包含鉴权信息等',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '提供商描述',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级，数字越大优先级越高',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `updated_by` VARCHAR(100) DEFAULT NULL COMMENT '更新人',
    `deleted_at` DATETIME DEFAULT NULL COMMENT '删除时间（软删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_provider_type` (`provider_type`, `deleted_at`),
    KEY `idx_enabled` (`enabled`),
    KEY `idx_deleted_at` (`deleted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知提供商配置表';

-- 通知发送记录表
-- 用于记录通知发送历史，便于追踪和排查问题

CREATE TABLE IF NOT EXISTS `notification_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `notification_type` VARCHAR(50) NOT NULL COMMENT '通知类型（EMAIL、SMS等）',
    `recipient` VARCHAR(200) NOT NULL COMMENT '接收者（邮箱地址、手机号等）',
    `subject` VARCHAR(500) DEFAULT NULL COMMENT '通知标题',
    `content` TEXT NOT NULL COMMENT '通知内容',
    `provider_type` VARCHAR(50) DEFAULT NULL COMMENT '使用的提供商类型',
    `provider_message_id` VARCHAR(200) DEFAULT NULL COMMENT '提供商返回的消息ID',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（PENDING、SENDING、SUCCESS、FAILED、CANCELLED）',
    `error_message` TEXT DEFAULT NULL COMMENT '错误消息（如果发送失败）',
    `error_code` VARCHAR(50) DEFAULT NULL COMMENT '错误码',
    `error_details` JSON DEFAULT NULL COMMENT '错误详情，JSON格式',
    `sent_at` DATETIME DEFAULT NULL COMMENT '发送时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `created_by` VARCHAR(100) DEFAULT NULL COMMENT '创建人',
    PRIMARY KEY (`id`),
    KEY `idx_notification_type` (`notification_type`),
    KEY `idx_recipient` (`recipient`),
    KEY `idx_status` (`status`),
    KEY `idx_provider_type` (`provider_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_sent_at` (`sent_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知发送记录表';

