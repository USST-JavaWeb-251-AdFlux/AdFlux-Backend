package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 广告信息视图
 */
@Data
public class AdvertisementVO implements Serializable {

    /**
     * 广告 ID
     */
    private Long adId;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 广告类型（0-image; 1-video）
     */
    private Integer adType;

    /**
     * 素材路径
     */
    private String mediaUrl;

    /**
     * 点击跳转地址
     */
    private String landingPage;

    /**
     * 广告类别 ID
     */
    private Long categoryId;

    /**
     * 广告版式
     */
    private String adLayout;

    /**
     * 周预算
     */
    private BigDecimal weeklyBudget;

    /**
     * 审核状态（0-待审核;1-通过;2-拒绝）
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

    private static final long serialVersionUID = 1L;
}