package com.gk.iam.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("iam_client_app")
public class IamClientApp implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clientId;

    private String clientSecret;

    private String appName;

    private String appIcon;

    private String redirectUris;

    private String grantTypes;

    private Integer tokenLifetime; // seconds

    private Integer status; // 1-正常, 0-下线

    private String description;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

