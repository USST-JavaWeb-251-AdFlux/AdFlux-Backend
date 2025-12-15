package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 站长收益统计视图
 */
@Data
public class PublisherStatisticsVO implements Serializable {

    /**
     * 总展示数
     */
    private Long totalImpressions;

    /**
     * 总点击数
     */
    private Long totalClicks;

    /**
     * 预估收益
     */
    private Double estimatedRevenue;

    private static final long serialVersionUID = 1L;
}