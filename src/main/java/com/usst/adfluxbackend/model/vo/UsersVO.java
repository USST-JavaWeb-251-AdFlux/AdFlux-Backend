package com.usst.adfluxbackend.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息视图
 */
@Data
public class UsersVO {
    /**
     * id
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
}
