package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员首页统计概览（无日期维度）
 * 返回若干 key metrics 供管理员 dashboard 展示
 */
@Data
public class AdminDashboardVO implements Serializable {

    /**
     * 系统中广告总数（所有广告，不限状态）
     */
    private Long totalAds;

    /**
     * 已通过审核并且启用的广告数（reviewStatus = 1 && isActive = 1）
     */
    private Long activeReviewedAds;

    /**
     * 待审核的广告数（reviewStatus = 0）
     */
    private Long pendingAds;

    /**
     * 系统中网站数量（publishers 表）
     */
    private Long totalWebsites;

    /**
     * 系统中用户数量（users 表）
     */
    private Long totalUsers;

    /**
     * 广告分类数量
     */
    private Long totalCategories;

    private static final long serialVersionUID = 1L;
}