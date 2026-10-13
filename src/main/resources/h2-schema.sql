CREATE TABLE IF NOT EXISTS `sys_company` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_code` VARCHAR(64) NOT NULL,
    `company_name` VARCHAR(128) NOT NULL,
    `leader` VARCHAR(64),
    `phone` VARCHAR(32),
    `email` VARCHAR(128),
    `status` INT DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `remark` VARCHAR(255),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `sys_department` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `dept_code` VARCHAR(64) NOT NULL,
    `dept_name` VARCHAR(128) NOT NULL,
    `leader` VARCHAR(64),
    `phone` VARCHAR(32),
    `status` INT DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64) NOT NULL,
    `password` VARCHAR(128) NOT NULL,
    `real_name` VARCHAR(64) NOT NULL,
    `email` VARCHAR(128),
    `phone` VARCHAR(32),
    `avatar` VARCHAR(255),
    `company_id` BIGINT,
    `dept_id` BIGINT,
    `role` VARCHAR(32) DEFAULT 'USER',
    `status` INT DEFAULT 1,
    `risk_level` VARCHAR(32) DEFAULT 'LOW',
    `last_login_time` TIMESTAMP,
    `last_login_ip` VARCHAR(64),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `iam_client_app` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `client_id` VARCHAR(64) NOT NULL,
    `client_secret` VARCHAR(128) NOT NULL,
    `app_name` VARCHAR(128) NOT NULL,
    `app_icon` VARCHAR(255),
    `redirect_uris` VARCHAR(2000) NOT NULL,
    `grant_types` VARCHAR(128) DEFAULT 'authorization_code,refresh_token',
    `token_lifetime` INT DEFAULT 7200,
    `status` INT DEFAULT 1,
    `description` VARCHAR(500),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `deleted` INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `iam_login_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(64) NOT NULL,
    `user_id` BIGINT,
    `ip_address` VARCHAR(64) NOT NULL,
    `location` VARCHAR(128),
    `browser` VARCHAR(64),
    `os` VARCHAR(64),
    `user_agent` VARCHAR(500),
    `client_id` VARCHAR(64),
    `login_status` INT NOT NULL,
    `error_message` VARCHAR(255),
    `risk_score` INT DEFAULT 0,
    `risk_tags` VARCHAR(255),
    `login_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS `iam_auth_code` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `auth_code` VARCHAR(128) NOT NULL,
    `client_id` VARCHAR(64) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `username` VARCHAR(64) NOT NULL,
    `redirect_uri` VARCHAR(500) NOT NULL,
    `scope` VARCHAR(128) DEFAULT 'openid profile email',
    `state` VARCHAR(128),
    `nonce` VARCHAR(128),
    `expires_at` TIMESTAMP NOT NULL,
    `used` INT DEFAULT 0,
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
