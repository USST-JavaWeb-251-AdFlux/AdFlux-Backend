package com.usst.adfluxbackend.model.dto.ad;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建广告请求
 */
@Data
public class CreateAdvertisementRequest implements Serializable {

    /**
     * 广告标题
     */
    private String title;

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
     * 广告版式（0-video； 1-banner； 2-sidebar）
     */
    private Integer adLayout;

    /**
     * 周预算
     */
    private BigDecimal weeklyBudget;

    /**
     * 广告类型（0-图文； 1-视频）
     */
    private Integer adType;

    private static final long serialVersionUID = 1L;
}