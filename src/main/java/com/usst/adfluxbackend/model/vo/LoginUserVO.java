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
     * id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户角色：admin/advertiser/publisher
     */
    private String userRole;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 创建时间
     */
    private Date createTime;


    private static final long serialVersionUID = 1L;
}