create table web_ai.user
(
    id         tinyint(11) auto_increment primary key,
    username   varchar(32)                           not null comment '用户名（唯一）',
    email      varchar(128)                          not null comment '邮箱',
    password   varchar(32)                           not null comment '密码（=BCrypt(MD5(明文), salt)）',
    created_at timestamp default current_timestamp() null comment '创建时间',
    constraint user_pk unique (username),
    constraint user_pk3 unique (email)
) comment '用户表';

