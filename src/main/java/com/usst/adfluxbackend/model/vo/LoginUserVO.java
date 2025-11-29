package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 已登录用户视图（脱敏）
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色：admin/advertiser/publisher
     */
    private String userRole;

    /**
     * token
     */
    private String token;

    private static final long serialVersionUID = 1L;
}