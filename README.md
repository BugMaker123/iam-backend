# 宇宙中心统一认证中台后端服务 (IAM Backend)

> **Spring Boot 3 + OIDC 1.0 + JWT + Flink/Kafka/Drools 实时风控架构**  
> 企业级统一身份认证与访问控制（IAM）中台核心服务端，提供基于标准 OpenID Connect 协议的应用单点登录、多租户组织权限体系、图形验证码防刷以及多维实时审计风控。

---

## 核心特性

- 🛡️ **标准 OIDC 1.0 / OAuth 2.0 协议**：
  - 支持授权码模式（`authorization_code`）、刷新令牌（`refresh_token`）；
  - 标准元数据发现接口 `/.well-known/openid-configuration`；
  - JWKS 公钥分发接口 `/.well-known/jwks.json`；
  - 授权码颁发（`/oauth2/authorize`）与 Token 兑换（`/oauth2/token`）。
- 🔐 **安全防线与凭证机制**：
  - 图形验证码防爆破：基于 HuTool Captcha 动态渲染，验证码凭据单次消费防重放；
  - MD5 加盐加固认证；
  - 无状态 JWT（RFC 7519）鉴权与权限校验。
- 🏢 **多维组织与应用中台**：
  - 公司组织树与部门多层级管理；
  - 人员账号全生命周期管理（角色、状态启禁、风控评级）；
  - OIDC 客户端应用接入管理（密钥生成、回调地址白名单校验）。
- ⚡ **实时风控流引擎规划**：
  - 基于 Apache Flink (CEP) + Apache Kafka + Drools 规则引擎的异步流式风控模型；
  - 自动识别异地登录、高频爆破、异常 UA 自动化特征与不可能位移（Impossible Travel）。
- 🚀 **双模运行能力**：
  - **开发/生产环境**：MySQL 8.0 + Redis 7.0 强劲性能；
  - **本地极简演示模式**：内置零外部依赖的 H2 嵌入式内存数据库，即开即测。

---

## 技术选型

| 层次 | 核心技术 | 说明 |
| :--- | :--- | :--- |
| **基础框架** | Spring Boot 3.2.3 | 基于 Java 17 的现代企业级骨架 |
| **持久层** | MyBatis-Plus 3.5.5 | 高效 CRUD 与分页引擎 |
| **数据库** | MySQL 8.0 / H2 Database | 生产与本地双引擎支持 |
| **缓存层** | Spring Data Redis + Caffeine | 分布式/本地双模缓存降级 |
| **安全/令牌** | JJWT (io.jsonwebtoken) 0.12.5 | 标准 JWT 颁发与验签 |
| **验证码** | HuTool Captcha 5.8.26 | 内存高效动态验证码 |
| **API规范** | RESTful + JSON | 全局统一 Result\<T\> 响应结构 |

---

## 工程目录结构

```text
iam-backend/
├── doc/
│   └── sql/
│       ├── schema.sql           # DDL 完整建表语句 (数据库: gk_iam)
│       ├── init-data.sql        # 宇宙中心组织、应用与人员初始化数据
│       └── gk_iam_all.sql       # 一键建库建表与初始化合并脚本
├── pom.xml                      # Maven 依赖定义
├── settings.xml                 # 镜像源加速配置
└── src/
    └── main/
        ├── java/com/gk/iam/
        │   ├── IamApplication.java        # Spring Boot 启动类
        │   ├── common/                    # 统一 Result 响应、分页包装与全局异常捕获
        │   ├── config/                    # 跨域 CORS、双模缓存与 MyBatis-Plus 配置
        │   ├── controller/                # 认证端点、OIDC 端点、风控中心与业务 CRUD 接口
        │   ├── dto/                       # 登录请求/响应、OIDC Token、元数据传输对象
        │   ├── entity/                    # 数据库映射实体类
        │   ├── mapper/                    # 数据访问 Mapper 接口
        │   ├── service/                   # 业务逻辑与流式风控事件分发服务
        │   └── util/                      # MD5、JWT、Captcha、IP 与地理位置解析工具
        └── resources/
            ├── application.yml            # 基础公共配置
            ├── application-dev.yml        # MySQL + Redis 生产开发配置
            ├── application-local.yml      # H2 内存数据库本地零依赖运行配置
            ├── h2-schema.sql              # H2 结构定义
            └── h2-data.sql                # H2 演示初始数据
```

---

## 快速上手

### 1. 环境准备
- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+** & **Redis 6.0+**（或直接以本地 H2 模式运行，免去环境依赖）

### 2. 数据库导入 (MySQL 模式)
在本地 MySQL 执行初始化脚本：
```bash
mysql -u root -p < doc/sql/gk_iam_all.sql
```

### 3. 服务启动

#### 方式 A：免配置本地演示模式 (推荐首试)
无需配置 MySQL 与 Redis，使用内置 H2 内存库启动：
```bash
mvn clean spring-boot:run -Dspring-boot.run.profiles=local
```

#### 方式 B：标准开发模式 (MySQL + Redis)
1. 检查并按需修改 `src/main/resources/application-dev.yml` 中的数据库及 Redis 连接参数；
2. 启动服务：
```bash
mvn clean spring-boot:run
```

启动成功后，服务端监听端口为：`http://localhost:8088`。

---

## 核心接口说明

- **获取图形验证码**：`GET /api/auth/captcha`
- **统一账号登录**：`POST /api/auth/login`
- **解析当前登录者**：`GET /api/auth/me` (Header: `Authorization: Bearer <token>`)
- **OIDC 授权码请求**：`GET /oauth2/authorize`
- **OIDC 凭据换取 Token**：`POST /oauth2/token`
- **OIDC 发现文档**：`GET /.well-known/openid-configuration`
- **风控引擎监控指标**：`GET /api/risk/status`
