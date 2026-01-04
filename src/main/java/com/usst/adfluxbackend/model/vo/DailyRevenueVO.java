package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 发布主每日收益明细
 */
@Data
public class DailyRevenueVO implements Serializable {
    /**
     * 日期
     */
    private String date;

    /**
     * 展示数
     */
    private Long impressions;

    /**
     * 点击数
     */
    private Long clicks;
    /**
     * 当日收益（发布主）
     */
    private Double revenue;

    private static final long serialVersionUID = 1L;
}
