-- ================================================
-- OAuth2 Provider 模块数据库初始化脚本
-- ================================================

-- OAuth2 提供商配置表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`oauth2_provider` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `registration_id` VARCHAR(50) NOT NULL UNIQUE COMMENT '注册 ID（唯一标识，如 google、github）',
    `name` VARCHAR(100) NOT NULL COMMENT '提供商名称（显示用）',
    `client_id` VARCHAR(255) NOT NULL COMMENT '客户端 ID',
    `client_secret` VARCHAR(500) NOT NULL COMMENT '客户端密钥',
    `authorization_uri` VARCHAR(500) NULL COMMENT '授权端点 URI',
    `token_uri` VARCHAR(500) NULL COMMENT 'Token 端点 URI',
    `user_info_uri` VARCHAR(500) NULL COMMENT '用户信息端点 URI',
    `jwk_set_uri` VARCHAR(500) NULL COMMENT 'JWK Set URI（用于验证 ID Token）',
    `issuer_uri` VARCHAR(500) NULL COMMENT 'Issuer URI（OIDC 自动发现）',
    `redirect_uri` VARCHAR(500) NULL COMMENT '重定向 URI',
    `scopes` VARCHAR(500) NOT NULL DEFAULT 'openid,profile,email' COMMENT '请求的权限范围（逗号分隔）',
    `user_name_attribute_name` VARCHAR(100) NOT NULL DEFAULT 'sub' COMMENT '用户名属性名',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `description` VARCHAR(500) NULL COMMENT '描述',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT(1) DEFAULT 0 NOT NULL COMMENT '是否删除：0-否, 1-是',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_registration_id` (`registration_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='OAuth2 提供商配置表';

-- ================================================
-- 示例数据（可选，根据需要取消注释）
-- ================================================

-- Google OAuth2 示例
-- INSERT INTO `oauth2_provider` (`registration_id`, `name`, `client_id`, `client_secret`, `issuer_uri`, `scopes`, `user_name_attribute_name`, `status`, `sort_order`, `description`) VALUES
-- ('google', 'Google', 'your-google-client-id', 'your-google-client-secret', 'https://accounts.google.com', 'openid,profile,email', 'sub', 1, 1, 'Google OAuth2 登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- GitHub OAuth2 示例
-- INSERT INTO `oauth2_provider` (`registration_id`, `name`, `client_id`, `client_secret`, `authorization_uri`, `token_uri`, `user_info_uri`, `scopes`, `user_name_attribute_name`, `status`, `sort_order`, `description`) VALUES
-- ('github', 'GitHub', 'your-github-client-id', 'your-github-client-secret', 'https://github.com/login/oauth/authorize', 'https://github.com/login/oauth/access_token', 'https://api.github.com/user', 'read:user,user:email', 'login', 1, 2, 'GitHub OAuth2 登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- Keycloak OIDC 示例
-- INSERT INTO `oauth2_provider` (`registration_id`, `name`, `client_id`, `client_secret`, `issuer_uri`, `scopes`, `user_name_attribute_name`, `status`, `sort_order`, `description`) VALUES
-- ('keycloak', 'Keycloak', 'your-keycloak-client-id', 'your-keycloak-client-secret', 'https://your-keycloak-server.com/realms/your-realm', 'openid,profile,email', 'preferred_username', 1, 3, 'Keycloak OIDC 登录')
-- ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

