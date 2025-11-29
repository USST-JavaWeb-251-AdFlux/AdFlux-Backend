package com.usst.adfluxbackend.model.dto.user;

import lombok.Data;

/**
 * 用户注册请求
 */
@Data
public class UserRegisterRequest {

    private static final long serialVersionUID = 8735650154179439661L;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 角色
     */
    private String role;
    /**
     * 手机号
     */
    private String phone;

     /**
      * 邮箱
      */
    private String email;
}