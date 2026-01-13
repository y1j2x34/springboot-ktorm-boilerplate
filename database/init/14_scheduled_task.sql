-- 定时任务表
CREATE TABLE IF NOT EXISTS scheduled_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型：BUILTIN（内置类型）或 CLASS（Java类全限定名）',
    executor_type VARCHAR(100) NOT NULL COMMENT '执行器类型：内置类型名称（如SchedulingJavaScriptRunner）或Java类全限定名',
    cron_expression VARCHAR(100) NOT NULL COMMENT 'Cron表达式',
    task_config TEXT COMMENT '任务配置（JSON格式）',
    description VARCHAR(500) COMMENT '任务描述',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    last_run_time TIMESTAMP NULL COMMENT '最后执行时间',
    last_run_status VARCHAR(20) NULL COMMENT '最后执行状态：SUCCESS、FAILED、RUNNING',
    last_run_message TEXT NULL COMMENT '最后执行结果消息',
    next_run_time TIMESTAMP NULL COMMENT '下次执行时间',
    run_count BIGINT NOT NULL DEFAULT 0 COMMENT '执行次数',
    success_count BIGINT NOT NULL DEFAULT 0 COMMENT '成功次数',
    fail_count BIGINT NOT NULL DEFAULT 0 COMMENT '失败次数',
    created_by INT NULL COMMENT '创建人ID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_by INT NULL COMMENT '更新人ID',
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-停用',
    INDEX idx_task_type (task_type),
    INDEX idx_enabled (enabled),
    INDEX idx_status (status),
    INDEX idx_next_run_time (next_run_time),
    UNIQUE KEY uk_task_name (task_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='定时任务表';

