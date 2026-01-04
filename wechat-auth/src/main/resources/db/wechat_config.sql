-- ================================================
-- 微信配置模块数据库初始化脚本
-- ================================================

-- 微信配置表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`wechat_config` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `config_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '配置标识（唯一）',
    `name` VARCHAR(100) NOT NULL COMMENT '配置名称（显示用）',
    `login_type` VARCHAR(20) NOT NULL COMMENT '登录类型：OPEN_PLATFORM-开放平台, MP-公众号, MINI_PROGRAM-小程序',
    `app_id` VARCHAR(100) NOT NULL COMMENT 'AppID',
    `app_secret` VARCHAR(200) NOT NULL COMMENT 'AppSecret',
    `token` VARCHAR(200) NULL COMMENT 'Token（公众号验证用）',
    `encoding_aes_key` VARCHAR(100) NULL COMMENT 'EncodingAESKey（消息加解密密钥）',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `description` VARCHAR(500) NULL COMMENT '描述',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_id` (`config_id`),
    INDEX `idx_login_type` (`login_type`),
    INDEX `idx_app_id` (`app_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信配置表';

-- ================================================
-- 示例数据（可选，根据需要取消注释并修改）
-- ================================================

-- 微信开放平台示例（PC 扫码登录）
-- INSERT INTO `wechat_config` (`config_id`, `name`, `login_type`, `app_id`, `app_secret`, `status`, `sort_order`, `description`) VALUES
-- ('wechat_open', '微信扫码登录', 'OPEN_PLATFORM', 'your-open-app-id', 'your-open-app-secret', 1, 1, '微信开放平台 PC 扫码登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 微信公众号示例（微信内 H5 登录）
-- INSERT INTO `wechat_config` (`config_id`, `name`, `login_type`, `app_id`, `app_secret`, `token`, `encoding_aes_key`, `status`, `sort_order`, `description`) VALUES
-- ('wechat_mp', '微信公众号登录', 'MP', 'your-mp-app-id', 'your-mp-app-secret', 'your-token', 'your-aes-key', 1, 2, '微信公众号网页授权登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 微信小程序示例
-- INSERT INTO `wechat_config` (`config_id`, `name`, `login_type`, `app_id`, `app_secret`, `status`, `sort_order`, `description`) VALUES
-- ('wechat_mini', '微信小程序登录', 'MINI_PROGRAM', 'your-mini-app-id', 'your-mini-app-secret', 1, 3, '微信小程序登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

