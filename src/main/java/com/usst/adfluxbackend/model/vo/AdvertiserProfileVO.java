package com.usst.adfluxbackend.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * 广告主公司信息视图
 */
@Data
public class AdvertiserProfileVO implements Serializable {

    /**
     * 广告业主 ID，对应 advertiserId
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long advertiserId;

    /**
     * 对应 users.userId
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * 邮箱（来自 users.email）
     */
    private String email;

    /**
     * 电话（来自 users.phone）
     */
    private String phone;

    private static final long serialVersionUID = 1L;
}