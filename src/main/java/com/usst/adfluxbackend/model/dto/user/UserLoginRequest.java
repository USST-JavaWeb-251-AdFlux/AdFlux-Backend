package com.usst.adfluxbackend.model.dto.user;

import lombok.Data;

@Data
public class UserLoginRequest {
    private static final long serialVersionUID = 8735650154179439661L;

    /**
     * 用户名
     */
    private String username;

     /**
      * 密码
      */
    private String userPassword;

}
