-- 广告系统数据库初始化脚本

-- 删除已存在的表
DROP TABLE IF EXISTS ad_displays;
DROP TABLE IF EXISTS content_visit_stats;
DROP TABLE IF EXISTS ad_placements;
DROP TABLE IF EXISTS advertisements;
DROP TABLE IF EXISTS advertiser_payments;
DROP TABLE IF EXISTS advertisers;
DROP TABLE IF EXISTS publishers;
DROP TABLE IF EXISTS ad_categories;
DROP TABLE IF EXISTS users;

-- 用户表
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(255) NOT NULL UNIQUE COMMENT '用户名',
    user_password VARCHAR(255) NOT NULL COMMENT '密码Hash',
    user_role ENUM('admin', 'advertiser', 'publisher') NOT NULL COMMENT '用户角色：admin/advertiser/publisher',
    email VARCHAR(255) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_user_role (user_role)
) COMMENT '用户表';

-- 广告业主信息表
CREATE TABLE advertisers (
    advertiser_id BIGINT PRIMARY KEY COMMENT '广告业主ID',
    company_name VARCHAR(255) NOT NULL COMMENT '公司名称',
    FOREIGN KEY (advertiser_id) REFERENCES users(user_id)
) COMMENT '广告业主信息表';

-- 网站站长信息表
CREATE TABLE publishers (
    website_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '网站ID',
    publisher_id BIGINT NOT NULL COMMENT '网站站长ID',
    website_name VARCHAR(255) NOT NULL COMMENT '网站名称',
    domain VARCHAR(255) NOT NULL COMMENT '网站地址',
    verification_token VARCHAR(255) COMMENT '验证代码',
    is_verified TINYINT DEFAULT 0 COMMENT '是否已通过验证（0-未通过；1-通过）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    verify_time DATETIME COMMENT '验证时间',
    FOREIGN KEY (publisher_id) REFERENCES users(user_id),
    INDEX idx_publisher_id (publisher_id),
    INDEX idx_domain (domain)
) COMMENT '网站站长信息表';

-- 广告分类表
CREATE TABLE ad_categories (
    category_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '广告类别ID',
    category_name VARCHAR(100) NOT NULL UNIQUE COMMENT '类别名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '广告分类表';

-- 广告信息表
CREATE TABLE advertisements (
    ad_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '广告ID',
    advertiser_id BIGINT NOT NULL COMMENT '广告业主ID',
    ad_type TINYINT NOT NULL COMMENT '广告类型（0-image; 1-video）',
    media_url VARCHAR(512) NOT NULL COMMENT '素材路径',
    title VARCHAR(255) NOT NULL COMMENT '广告标题',
    landing_page VARCHAR(512) COMMENT '点击跳转地址',
    category_id BIGINT COMMENT '广告类别ID',
    weekly_budget DECIMAL(10,2) COMMENT '周预算',
    review_status TINYINT DEFAULT 0 COMMENT '审核状态（0-待审核; 1-通过; 2-拒绝）',
    is_active TINYINT DEFAULT 0 COMMENT '是否启用投放（0-否；1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    edit_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '编辑时间',
    ad_layout VARCHAR(50) COMMENT '广告版式（banner/sidebar/card等）',
    reject_reason TEXT COMMENT '拒绝原因',
    FOREIGN KEY (advertiser_id) REFERENCES advertisers(advertiser_id),
    FOREIGN KEY (category_id) REFERENCES ad_categories(category_id),
    INDEX idx_advertiser_id (advertiser_id),
    INDEX idx_category_id (category_id),
    INDEX idx_review_status (review_status),
    INDEX idx_is_active (is_active)
) COMMENT '广告信息表';

-- 广告业主付款信息
CREATE TABLE advertiser_payments (
    payment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '支付信息ID',
    advertiser_id BIGINT NOT NULL COMMENT '广告业主ID',
    card_number VARCHAR(255) NOT NULL COMMENT '银行卡号',
    bank_name VARCHAR(100) COMMENT '银行名称',
    FOREIGN KEY (advertiser_id) REFERENCES advertisers(advertiser_id),
    INDEX idx_advertiser_id (advertiser_id)
) COMMENT '广告业主付款信息';

-- 广告位信息表
CREATE TABLE ad_placements (
    placement_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '广告位ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    placement_name VARCHAR(255) NOT NULL COMMENT '广告位名称',
    ad_layout VARCHAR(50) NOT NULL COMMENT '广告版式（banner/sidebar/card等）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (website_id) REFERENCES publishers(website_id),
    INDEX idx_website_id (website_id)
) COMMENT '广告位信息表';

-- 广告展示表
CREATE TABLE ad_displays (
    display_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    track_id VARCHAR(255) NOT NULL COMMENT '匿名用户标识',
    ad_id BIGINT NOT NULL COMMENT '广告ID',
    website_id BIGINT NOT NULL COMMENT '网站ID',
    duration INT COMMENT '停留时长',
    clicked TINYINT DEFAULT 0 COMMENT '是否点击（0-否；1-是）',
    display_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '展示时间',
    click_time DATETIME COMMENT '点击时间',
    FOREIGN KEY (ad_id) REFERENCES advertisements(ad_id),
    FOREIGN KEY (website_id) REFERENCES publishers(website_id),
    INDEX idx_track_id (track_id),
    INDEX idx_ad_id (ad_id),
    INDEX idx_website_id (website_id),
    INDEX idx_display_time (display_time)
) COMMENT '广告展示表';

-- 内容访问统计表
CREATE TABLE content_visit_stats (
    stat_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '统计ID',
    track_id VARCHAR(255) NOT NULL COMMENT '匿名用户标识',
    content_url VARCHAR(512) NOT NULL COMMENT '内容页面URL',
    visit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
    stay_duration INT COMMENT '停留时长（秒）',
    INDEX idx_track_id (track_id),
    INDEX idx_content_url (content_url),
    INDEX idx_visit_time (visit_time)
) COMMENT '内容访问统计表';

-- 插入基础数据
INSERT INTO ad_categories (categoryId, categoryName, createTime) VALUES
(2, '科技', NOW()),
(3, '金融', NOW()),
(4, '教育', NOW()),
(5, '电商', NOW()),
(6, '旅游', NOW());

-- 创建管理员用户示例
-- 注意：实际密码需要经过BCrypt加密
INSERT INTO users (user_id, username, user_password, user_role, email, phone, create_time) VALUES 
(1, 'admin', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'admin', 'admin@example.com', '13800000000', NOW());

INSERT INTO advertisers (advertiser_id, company_name) VALUES 
(1, '示例广告公司');

-- 创建站长用户示例
INSERT INTO users (user_id, username, user_password, user_role, email, phone, create_time) VALUES 
(2, 'publisher', '$2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', 'publisher', 'publisher@example.com', '13800000001', NOW());

INSERT INTO publishers (publisher_id, website_name, domain, verification_token, is_verified, create_time, verify_time) VALUES 
(2, '示例网站', 'www.example.com', 'verify-xxxxxxxxxxxxxxxx', 1, NOW(), NOW());

COMMIT;