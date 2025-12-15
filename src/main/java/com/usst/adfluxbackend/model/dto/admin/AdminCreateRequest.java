package com.usst.adfluxbackend.model.dto.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员创建请求
 */
@Data
public class AdminCreateRequest implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    private static final long serialVersionUID = 1L;
}