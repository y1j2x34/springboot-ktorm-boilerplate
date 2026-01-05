-- ================================================
-- User 模块数据库初始化脚本
-- ================================================

-- 用户表
CREATE TABLE IF NOT EXISTS `spring-boot-kt`.`user` (
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

