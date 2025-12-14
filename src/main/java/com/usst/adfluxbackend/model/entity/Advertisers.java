package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 广告业主信息表
 * @TableName advertisers
 */
@TableName(value ="advertisers")
@Data
public class Advertisers {
    /**
     * 广告业主 ID
     */
    @TableId
    private Long advertiserId;

    /**
     * 公司名称
     */
    private String companyName;
}