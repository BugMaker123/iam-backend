package com.gk.iam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 宇宙中心 统一身份认证登录系统 (GK Unified IAM)
 * 启动引导类
 */
@SpringBootApplication
@EnableAsync
@MapperScan("com.gk.iam.mapper")
public class IamApplication {

    public static void main(String[] args) {
        SpringApplication.run(IamApplication.class, args);
        System.out.println("=================================================");
        System.out.println("   宇宙中心统一身份认证中心 (GK IAM) 启动成功!");
        System.out.println("   服务地址: http://localhost:8088");
        System.out.println("   OIDC Discovery: http://localhost:8088/.well-known/openid-configuration");
        System.out.println("   默认管理员账号/密码: admin / admin");
        System.out.println("=================================================");
    }
}

