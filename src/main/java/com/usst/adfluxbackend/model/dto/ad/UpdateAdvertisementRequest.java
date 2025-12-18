package com.usst.adfluxbackend.model.dto.ad;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 更新广告请求
 */
@Data
public class UpdateAdvertisementRequest implements Serializable {

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
     * 广告版式（0-banner； 1-sidebar； 2-card）
     */
    private String adLayout;

    /**
     * 周预算
     */
    private BigDecimal weeklyBudget;

    private static final long serialVersionUID = 1L;
}