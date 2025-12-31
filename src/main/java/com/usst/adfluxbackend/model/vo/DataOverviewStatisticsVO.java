package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 广告主数据总览统计
 */
@Data
public class DataOverviewStatisticsVO implements Serializable {

    /**
     * 总展示数
     */
    private Long totalImpressions;

    /**
     * 总点击数
     */
    private Long totalClicks;

    /**
     * 点击率
     */
    private Double ctr;

    /**
     * 每日统计数据
     */
    private List<DailyStatisticsVO> daily;

    private static final long serialVersionUID = 1L;
}
