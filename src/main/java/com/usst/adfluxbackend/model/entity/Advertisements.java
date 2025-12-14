package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 广告信息表
 * @TableName advertisements
 */
@TableName(value ="advertisements")
@Data
public class Advertisements {
    /**
     * 广告 ID
     */
    @TableId
    private Long adId;

    /**
     * 广告业主 ID
     */
    private Long advertiserId;

    /**
     * 广告类型（0-image; 1-video）
     */
    private Integer adType;

    /**
     * 素材路径
     */
    private String mediaUrl;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 点击跳转地址
     */
    private String landingPage;

    /**
     * 广告类别 ID
     */
    private Long categoryId;

    /**
     * 周预算
     */
    private BigDecimal weeklyBudget;

    /**
     * 审核状态（0-待审核; 1-通过; 2-拒绝）
     */
    private Integer reviewStatus;

    /**
     * 是否启用投放（0-否；1-是）
     */
    private Integer isActive;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 广告版式（0-banner； 1-sidebar； 2-card）
     */
    private String adLayout;
}