-- ========================================================
-- 宇宙中心 统一身份认证与访问控制系统 (GK IAM) - 完整建表与初始化脚本
-- Database: gk_iam
-- ========================================================

CREATE DATABASE IF NOT EXISTS `gk_iam` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `gk_iam`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 公司组织表: sys_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `company_code` VARCHAR(64) NOT NULL COMMENT '公司编码',
    `company_name` VARCHAR(128) NOT NULL COMMENT '公司名称',
    `leader` VARCHAR(64) DEFAULT NULL COMMENT '负责人',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '联系邮箱',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_company_code` (`company_code`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司信息表';

-- ----------------------------
-- 2. 部门组织表: sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `company_id` BIGINT NOT NULL COMMENT '所属公司ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID (0表示顶级部门)',
    `dept_code` VARCHAR(64) NOT NULL COMMENT '部门编码',
    `dept_name` VARCHAR(128) NOT NULL COMMENT '部门名称',
    `leader` VARCHAR(64) DEFAULT NULL COMMENT '部门负责人',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '部门电话',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-启用, 0-停用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_company_id` (`company_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';

-- ----------------------------
-- 3. 人员用户表: sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '登录账号',
    `password` VARCHAR(128) NOT NULL COMMENT '密码 (默认MD5)',
    `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '电子邮箱',
    `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号码',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `company_id` BIGINT DEFAULT NULL COMMENT '所属公司ID',
    `dept_id` BIGINT DEFAULT NULL COMMENT '所属部门ID',
    `role` VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN-系统管理员, USER-普通员工, AUDITOR-审计员',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常, 0-禁用, 2-锁定',
    `risk_level` VARCHAR(32) NOT NULL DEFAULT 'LOW' COMMENT '账户风控评级: LOW-低风险, MEDIUM-中风险, HIGH-高风险',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(64) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `deleted`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_company_id` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='人员用户表';

-- ----------------------------
-- 4. OIDC应用接入表: iam_client_app
-- ----------------------------
DROP TABLE IF EXISTS `iam_client_app`;
CREATE TABLE `iam_client_app` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `client_id` VARCHAR(64) NOT NULL COMMENT '客户端ID (唯一身份标示)',
    `client_secret` VARCHAR(128) NOT NULL COMMENT '客户端密钥',
    `app_name` VARCHAR(128) NOT NULL COMMENT '接入应用名称',
    `app_icon` VARCHAR(255) DEFAULT NULL COMMENT '应用图标',
    `redirect_uris` TEXT NOT NULL COMMENT '授权回调地址列表(换行或逗号分隔)',
    `grant_types` VARCHAR(128) NOT NULL DEFAULT 'authorization_code,refresh_token' COMMENT '允许的授权模式',
    `token_lifetime` INT NOT NULL DEFAULT 7200 COMMENT 'Token有效时长(秒)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1-正常接入, 0-已下线',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '应用描述与接入说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_client_id` (`client_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OIDC/OAuth2接入应用配置表';

-- ----------------------------
-- 5. 登录审计日志表: iam_login_log
-- ----------------------------
DROP TABLE IF EXISTS `iam_login_log`;
CREATE TABLE `iam_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(64) NOT NULL COMMENT '登录账户',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `ip_address` VARCHAR(64) NOT NULL COMMENT '登录IP地址',
    `location` VARCHAR(128) DEFAULT NULL COMMENT '登录地理位置',
    `browser` VARCHAR(64) DEFAULT NULL COMMENT '浏览器名称及版本',
    `os` VARCHAR(64) DEFAULT NULL COMMENT '操作系统',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '原始UserAgent',
    `client_id` VARCHAR(64) DEFAULT NULL COMMENT '发起的OIDC应用ID',
    `login_status` TINYINT NOT NULL COMMENT '登录结果: 1-成功, 0-失败',
    `error_message` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
    `risk_score` INT NOT NULL DEFAULT 0 COMMENT '实时风控评分 (0-100)',
    `risk_tags` VARCHAR(255) DEFAULT NULL COMMENT '风控预警标签 (如: 频繁试错, 异地登录, 代理IP)',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_username_time` (`username`, `login_time`),
    KEY `idx_ip_time` (`ip_address`, `login_time`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录审计日志与风控数据源表';

-- ----------------------------
-- 6. OIDC授权码存储表: iam_auth_code
-- ----------------------------
DROP TABLE IF EXISTS `iam_auth_code`;
CREATE TABLE `iam_auth_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `auth_code` VARCHAR(128) NOT NULL COMMENT '生成的临时授权码',
    `client_id` VARCHAR(64) NOT NULL COMMENT '客户端ID',
    `user_id` BIGINT NOT NULL COMMENT '授权用户ID',
    `username` VARCHAR(64) NOT NULL COMMENT '授权用户名',
    `redirect_uri` VARCHAR(500) NOT NULL COMMENT '回调地址',
    `scope` VARCHAR(128) DEFAULT 'openid profile email' COMMENT '授权范围',
    `state` VARCHAR(128) DEFAULT NULL COMMENT '防CSRF状态码',
    `nonce` VARCHAR(128) DEFAULT NULL COMMENT 'OIDC随机值',
    `expires_at` DATETIME NOT NULL COMMENT '过期时间',
    `used` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已使用: 0-未使用, 1-已使用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_auth_code` (`auth_code`),
    KEY `idx_client_user` (`client_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OIDC授权码凭证表';

-- ========================================================
-- 数据初始化部分 (宇宙中心)
-- ========================================================

-- 初始化公司
INSERT INTO `sys_company` (`id`, `company_code`, `company_name`, `leader`, `phone`, `email`, `status`, `sort_order`, `remark`) VALUES
(1, 'GK_CORP', '宇宙中心股份有限公司', '集团分管领导', '021-23018888', 'contact@sinopharm.com', 1, 1, '宇宙中心总部大厦'),
(2, 'GK_TECH', '宇宙中心数字科技有限公司', '信息总监', '021-23019999', 'tech@sinopharm.com', 1, 2, '数字化研发与IT基础设施中心')
ON DUPLICATE KEY UPDATE `company_name` = VALUES(`company_name`);

-- 初始化部门
INSERT INTO `sys_department` (`id`, `company_id`, `parent_id`, `dept_code`, `dept_name`, `leader`, `phone`, `status`, `sort_order`) VALUES
(1, 2, 0, 'TECH_CENTER', '数字化研发与技术中心', '张总工', '021-9001', 1, 1),
(2, 2, 1, 'SEC_ARCH', '安全架构与IAM认证组', '赵架构', '021-9002', 1, 1),
(3, 2, 1, 'BIGDATA_RISK', '医药大数据与实时风控部', '孙专家', '021-9003', 1, 2),
(4, 2, 1, 'PLATFORM_DEV', '医药商业中台开发部', '周主管', '021-9004', 1, 3),
(5, 1, 0, 'LOGISTICS_OPS', '全国内药物流运营中心', '钱总监', '021-8001', 1, 2)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`);

-- 初始化人员 (admin / admin, MD5: 21232f297a57a5a743894a0e4a801fc3)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `company_id`, `dept_id`, `role`, `status`, `risk_level`) VALUES
(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '宇宙中心超级管理员', 'admin@sinopharm.com', '13800000000', 2, 2, 'ADMIN', 1, 'LOW'),
(2, 'zhangsan', '21232f297a57a5a743894a0e4a801fc3', '张三', 'zhangsan@sinopharm.com', '13800000001', 2, 2, 'USER', 1, 'LOW'),
(3, 'lisi', '21232f297a57a5a743894a0e4a801fc3', '李四', 'lisi@sinopharm.com', '13800000002', 2, 3, 'USER', 1, 'LOW'),
(4, 'risk_user', '21232f297a57a5a743894a0e4a801fc3', '风控可疑账户', 'risk_alert@sinopharm.com', '13800000003', 2, 3, 'USER', 1, 'HIGH')
ON DUPLICATE KEY UPDATE `real_name` = VALUES(`real_name`);

-- 初始化OIDC接入应用
INSERT INTO `iam_client_app` (`id`, `client_id`, `client_secret`, `app_name`, `app_icon`, `redirect_uris`, `grant_types`, `token_lifetime`, `status`, `description`) VALUES
(1, 'gk-portal-web', 'gk_portal_secret_998877', '宇宙中心统一综合工作台门户', 'https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg', 'http://localhost:5173/oidc-callback\nhttp://localhost:8088/callback', 'authorization_code,refresh_token', 7200, 1, '宇宙中心全集团统一协同办公门户，默认启用OIDC授权码登录'),
(2, 'gk-erp-system', 'gk_erp_secret_112233', '宇宙中心现代医药分销管理系统(ERP)', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', 'http://erp.sinopharm-internal.com/login/oauth2/code/gk-iam\nhttp://localhost:9001/login/callback', 'authorization_code,refresh_token', 7200, 1, '医药商业流通核心ERP，对接统一认证中心'),
(3, 'gk-wms-cloud', 'gk_wms_secret_445566', '宇宙中心智能医药物流WMS云平台', 'https://gw.alipayobjects.com/zos/rmsportal/WdGqmHpayyMjiPEhcDgq.svg', 'http://wms.sinopharm-internal.com/auth/oidc/callback', 'authorization_code,refresh_token', 14400, 1, '医药物流冷链与智能仓储系统，支持单点长会话')
ON DUPLICATE KEY UPDATE `app_name` = VALUES(`app_name`);

-- 初始化登录审计日志
INSERT INTO `iam_login_log` (`username`, `user_id`, `ip_address`, `location`, `browser`, `os`, `user_agent`, `client_id`, `login_status`, `error_message`, `risk_score`, `risk_tags`, `login_time`) VALUES
('admin', 1, '127.0.0.1', '中国·上海 (宇宙中心内网专线)', 'Chrome 122', 'Windows 11', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36', 'gk-portal-web', 1, '登录成功', 0, '安全信任', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('admin', 1, '127.0.0.1', '中国·上海 (宇宙中心内网专线)', 'Chrome 122', 'Windows 11', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36', NULL, 1, '登录成功', 0, '安全信任', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('admin', 1, '116.228.111.2', '中国·上海 (电信专线)', 'Edge 121', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edg/121.0.0.0', 'gk-portal-web', 0, '图形验证码输入错误', 25, '验证码试错', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('zhangsan', 2, '218.242.124.10', '中国·上海 (移动专线)', 'Chrome 120', 'macOS 14', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'gk-erp-system', 1, '登录成功', 5, '常规终端', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('risk_user', 4, '198.51.100.45', '美国·加利福尼亚·洛杉矶 (境外IP)', 'Unknown', 'Linux', 'curl/7.88.1 Automated-Script', 'gk-portal-web', 0, '密码校验失败', 85, '境外IP/爆破攻击特征/异常UA', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('risk_user', 4, '198.51.100.45', '美国·加利福尼亚·洛杉矶 (境外IP)', 'Unknown', 'Linux', 'curl/7.88.1 Automated-Script', 'gk-portal-web', 0, '密码校验失败', 90, '境外IP/高频攻击', DATE_SUB(NOW(), INTERVAL 12 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
