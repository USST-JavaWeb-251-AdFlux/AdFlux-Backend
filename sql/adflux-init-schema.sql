-- 1. users（用户基础表）
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

-- 2. advertisers（广告业主信息表）
create table if not exists advertisers
(
    advertiserId bigint                               not null comment '广告业主 ID' primary key,
    companyName  varchar(256)                         not null comment '公司名称',
    FOREIGN KEY (advertiserId) REFERENCES users(userId)
) comment '广告业主信息表' collate = utf8mb4_unicode_ci;

-- 3. advertiser_payments（广告业主付款信息）
create table if not exists advertiser_payments
(
    paymentId    bigint                               not null comment '支付信息 ID' primary key,
    advertiserId bigint                               not null comment '广告业主 ID',
    cardNumber   varchar(200)                         not null comment '银行卡号',
    bankName     varchar(100)                         null comment '银行名称',
    FOREIGN KEY (advertiserId) REFERENCES users(userId)
) comment '广告业主付款信息' collate = utf8mb4_unicode_ci;

-- 4. publishers（网站站长信息表）
create table if not exists publishers
(
    websiteId         bigint                               not null comment '网站 ID' primary key,
    publisherId       bigint                               not null comment '网站站长 ID',
    websiteName       varchar(100)                         not null comment '网站名称',
    domain            varchar(200)                         not null comment '网站地址',
    verificationToken varchar(200)                         null comment '验证代码',
    isVerified        tinyint       default 0              not null comment '是否已通过验证',
    createTime        datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    verifyTime        datetime      default NULL comment '验证时间',
    FOREIGN KEY (publisherId) REFERENCES users(userId)
) comment '网站站长信息表' collate = utf8mb4_unicode_ci;

-- 5. ad_categories（广告分类表）
create table if not exists ad_categories
(
    categoryId   bigint                not null comment '广告类别 ID' primary key,
    categoryName varchar(100)                          not null comment '类别名称',
    createTime   datetime      default CURRENT_TIMESTAMP not null comment '创建时间'
) comment '广告分类表' collate = utf8mb4_unicode_ci;

-- 6. advertisements（广告信息表）
create table if not exists advertisements
(
    adId         bigint                                not null comment '广告 ID' primary key,
    advertiserId bigint                                not null comment '广告业主 ID',
    adType       int                                   not null comment '广告类型（0-image; 1-video）',
    mediaUrl     varchar(255)                          not null comment '素材路径',
    title        varchar(200)                          not null comment '广告标题',
    landingPage  varchar(255)                          null comment '点击跳转地址',
    categoryId   bigint                                not null comment '广告类别 ID',
    weeklyBudget decimal(10,2)                         not null comment '周预算',
    reviewStatus int           default 0               not null comment '审核状态（0-待审核; 1-通过; 2-拒绝）',
    isActive     tinyint       default 0               not null comment '是否启用投放（0-否；1-是）',
    createTime   datetime      default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime      default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '编辑时间',
    FOREIGN KEY (advertiserId) REFERENCES advertisers(advertiserId),
    FOREIGN KEY (categoryId) REFERENCES ad_categories(categoryId)
) comment '广告信息表' collate = utf8mb4_unicode_ci;

-- content_visit_stats（访问内容行为统计）
create table if not exists content_visit_stats
(
    visitId     bigint                                not null comment '主键' primary key,
    websiteId   bigint                                not null comment '网站 ID',
    categoryId  bigint                                not null comment '广告类别 ID',
    trackId     char(36)                              not null comment '匿名用户标识',
    duration    int                                   not null comment '停留时长',
    timestamp   datetime      default CURRENT_TIMESTAMP not null comment '访问时间',
    FOREIGN KEY (websiteId) REFERENCES publishers(websiteId),
    FOREIGN KEY (categoryId) REFERENCES ad_categories(categoryId)
) comment '访问内容行为统计' collate = utf8mb4_unicode_ci;

-- ad_displays（广告展示表）
create table if not exists ad_displays
(
    displayId   bigint                                not null comment '主键' primary key,
    trackId     char(36)                              not null comment '匿名用户标识',
    adId        bigint                                not null comment '广告 ID',
    websiteId   bigint                                not null comment '网站 ID',
    duration    int           default 0               not null comment '停留时长',
    clicked     tinyint       default 0               not null comment '是否点击',
    displayTime datetime      default CURRENT_TIMESTAMP not null comment '展示时间',
    clickTime   datetime      default NULL comment '点击时间',
    FOREIGN KEY (adId) REFERENCES advertisements(adId)
) comment '广告展示表' collate = utf8mb4_unicode_ci;



