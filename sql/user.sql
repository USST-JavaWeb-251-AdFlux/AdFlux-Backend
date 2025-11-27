-- 创建数据库
create database if not exists adflux;

-- 使用数据库
use adflux;

-- 创建用户表
create table if not exists users
(
    userId           bigint auto_increment comment 'id' primary key,
    username  varchar(256)                           not null comment '用户名',
    userPassword varchar(512)                           not null comment '密码 Hash',
    userRole     varchar(50) default 'advertiser'       not null comment '用户角色：admin/advertiser/publisher',
    email        varchar(100)                         null comment '邮箱',
    phone        varchar(50)                          null comment '电话',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    UNIQUE KEY uk_username (username)
) comment '用户' collate = utf8mb4_unicode_ci;
