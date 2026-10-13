-- ========================================================
-- 宇宙中心统一身份认证与访问控制系统 (GK IAM) - 初始化数据
-- Database: gk_iam
-- ========================================================

USE `gk_iam`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 初始化公司数据 (宇宙中心板块)
-- ----------------------------
TRUNCATE TABLE `sys_company`;
INSERT INTO `sys_company` (`id`, `company_code`, `company_name`, `leader`, `phone`, `email`, `status`, `sort_order`, `remark`) VALUES
(1, 'GK_CORP', '宇宙中心股份有限公司', '集团分管领导', '021-23018888', 'contact@sinopharm.com', 1, 1, '宇宙中心总部大厦'),
(2, 'GK_TECH', '宇宙中心数字科技有限公司', '信息总监', '021-23019999', 'tech@sinopharm.com', 1, 2, '数字化研发与IT基础设施中心');

-- ----------------------------
-- 2. 初始化部门数据
-- ----------------------------
TRUNCATE TABLE `sys_department`;
INSERT INTO `sys_department` (`id`, `company_id`, `parent_id`, `dept_code`, `dept_name`, `leader`, `phone`, `status`, `sort_order`) VALUES
(1, 2, 0, 'TECH_CENTER', '数字化研发与技术中心', '张总工', '021-9001', 1, 1),
(2, 2, 1, 'SEC_ARCH', '安全架构与IAM认证组', '赵架构', '021-9002', 1, 1),
(3, 2, 1, 'BIGDATA_RISK', '医药大数据与实时风控部', '孙专家', '021-9003', 1, 2),
(4, 2, 1, 'PLATFORM_DEV', '医药商业中台开发部', '周主管', '021-9004', 1, 3),
(5, 1, 0, 'LOGISTICS_OPS', '全国内药物流运营中心', '钱总监', '021-8001', 1, 2);

-- ----------------------------
-- 3. 初始化人员用户数据
-- 默认管理员账号: admin, 默认密码: admin, 采用MD5加密值为 21232f297a57a5a743894a0e4a801fc3
-- ----------------------------
TRUNCATE TABLE `sys_user`;
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `email`, `phone`, `company_id`, `dept_id`, `role`, `status`, `risk_level`) VALUES
(1, 'admin', '21232f297a57a5a743894a0e4a801fc3', '宇宙中心超级管理员', 'admin@sinopharm.com', '13800000000', 2, 2, 'ADMIN', 1, 'LOW'),
(2, 'zhangsan', '21232f297a57a5a743894a0e4a801fc3', '张三', 'zhangsan@sinopharm.com', '13800000001', 2, 2, 'USER', 1, 'LOW'),
(3, 'lisi', '21232f297a57a5a743894a0e4a801fc3', '李四', 'lisi@sinopharm.com', '13800000002', 2, 3, 'USER', 1, 'LOW'),
(4, 'risk_user', '21232f297a57a5a743894a0e4a801fc3', '风控可疑账户', 'risk_alert@sinopharm.com', '13800000003', 2, 3, 'USER', 1, 'HIGH');

-- ----------------------------
-- 4. 初始化接入应用数据 (OIDC Client Apps)
-- ----------------------------
TRUNCATE TABLE `iam_client_app`;
INSERT INTO `iam_client_app` (`id`, `client_id`, `client_secret`, `app_name`, `app_icon`, `redirect_uris`, `grant_types`, `token_lifetime`, `status`, `description`) VALUES
(1, 'gk-portal-web', 'gk_portal_secret_998877', '宇宙中心统一综合工作台门户', 'https://gw.alipayobjects.com/zos/rmsportal/KDpgvguMpGfqaHPjicRK.svg', 'http://localhost:5173/oidc-callback\nhttp://localhost:8088/callback', 'authorization_code,refresh_token', 7200, 1, '宇宙中心全集团统一协同办公门户，默认启用OIDC授权码登录'),
(2, 'gk-erp-system', 'gk_erp_secret_112233', '宇宙中心现代医药分销管理系统(ERP)', 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png', 'http://erp.sinopharm-internal.com/login/oauth2/code/gk-iam\nhttp://localhost:9001/login/callback', 'authorization_code,refresh_token', 7200, 1, '医药商业流通核心ERP，对接统一认证中心'),
(3, 'gk-wms-cloud', 'gk_wms_secret_445566', '宇宙中心智能医药物流WMS云平台', 'https://gw.alipayobjects.com/zos/rmsportal/WdGqmHpayyMjiPEhcDgq.svg', 'http://wms.sinopharm-internal.com/auth/oidc/callback', 'authorization_code,refresh_token', 14400, 1, '医药物流冷链与智能仓储系统，支持单点长会话');

-- ----------------------------
-- 5. 初始化登录审计日志数据
-- ----------------------------
TRUNCATE TABLE `iam_login_log`;
INSERT INTO `iam_login_log` (`username`, `user_id`, `ip_address`, `location`, `browser`, `os`, `user_agent`, `client_id`, `login_status`, `error_message`, `risk_score`, `risk_tags`, `login_time`) VALUES
('admin', 1, '127.0.0.1', '中国·上海 (宇宙中心内网专线)', 'Chrome 122', 'Windows 11', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36', 'gk-portal-web', 1, '登录成功', 0, '安全信任', DATE_SUB(NOW(), INTERVAL 15 MINUTE)),
('admin', 1, '127.0.0.1', '中国·上海 (宇宙中心内网专线)', 'Chrome 122', 'Windows 11', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36', NULL, 1, '登录成功', 0, '安全信任', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('admin', 1, '116.228.111.2', '中国·上海 (电信专线)', 'Edge 121', 'Windows 10', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Edg/121.0.0.0', 'gk-portal-web', 0, '图形验证码输入错误', 25, '验证码试错', DATE_SUB(NOW(), INTERVAL 4 HOUR)),
('zhangsan', 2, '218.242.124.10', '中国·上海 (移动专线)', 'Chrome 120', 'macOS 14', 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', 'gk-erp-system', 1, '登录成功', 5, '常规终端', DATE_SUB(NOW(), INTERVAL 6 HOUR)),
('risk_user', 4, '198.51.100.45', '美国·加利福尼亚·洛杉矶 (境外IP)', 'Unknown', 'Linux', 'curl/7.88.1 Automated-Script', 'gk-portal-web', 0, '密码校验失败', 85, '境外IP/爆破攻击特征/异常UA', DATE_SUB(NOW(), INTERVAL 12 HOUR)),
('risk_user', 4, '198.51.100.45', '美国·加利福尼亚·洛杉矶 (境外IP)', 'Unknown', 'Linux', 'curl/7.88.1 Automated-Script', 'gk-portal-web', 0, '密码校验失败', 90, '境外IP/高频攻击', DATE_SUB(NOW(), INTERVAL 12 HOUR));

SET FOREIGN_KEY_CHECKS = 1;
