package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发布主收益统计（汇总所有站点或单站点）
 */
@Data
public class PublisherRevenueStatisticsVO implements Serializable {
    private Long totalImpressions;
    private Long totalClicks;
    private Double totalRevenue;
    private List<DailyRevenueVO> daily;

    private static final long serialVersionUID = 1L;
}
