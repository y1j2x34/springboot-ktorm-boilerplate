-- ================================================
-- Async Task 模块数据库初始化脚本
-- ================================================

-- 异步任务表
CREATE TABLE IF NOT EXISTS async_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_type VARCHAR(100) NOT NULL COMMENT '任务类型（由业务模块定义）',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILURE-失败, RETRYING-重试中（注意：代码中使用 taskStatus 字段访问）',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    payload TEXT NOT NULL COMMENT '任务数据（JSON格式）',
    result TEXT NULL COMMENT '执行结果（JSON格式，可选）',
    error_message TEXT NULL COMMENT '错误信息',
    retry_count INT NOT NULL DEFAULT 0 COMMENT '当前重试次数',
    max_retry_count INT NOT NULL DEFAULT 0 COMMENT '最大重试次数',
    retry_strategy VARCHAR(20) NULL COMMENT '重试策略：FIXED-固定间隔, EXPONENTIAL-指数退避',
    retry_interval INT NULL COMMENT '重试间隔（秒）',
    processor_class VARCHAR(255) NULL COMMENT '处理器类名（用于动态加载）',
    started_at TIMESTAMP NULL COMMENT '开始处理时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    tenant_id INT NULL COMMENT '租户ID（支持多租户）',
    created_by INT NULL COMMENT '创建人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by INT NULL COMMENT '更新人ID',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status_flag INT NOT NULL DEFAULT 1 COMMENT '状态标志：1-正常，0-已取消',
    INDEX idx_task_type (task_type),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_created_at (created_at),
    INDEX idx_status_priority_created (status, priority DESC, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='异步任务表';

