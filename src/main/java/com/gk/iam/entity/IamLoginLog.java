package com.gk.iam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("iam_login_log")
public class IamLoginLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private Long userId;

    private String ipAddress;

    private String location;

    private String browser;

    private String os;

    private String userAgent;

    private String clientId;

    private Integer loginStatus; // 1-成功, 0-失败

    private String errorMessage;

    private Integer riskScore; // 0-100

    private String riskTags;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;
}

