package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 每日统计数据视图
 */
@Data
public class DailyStatisticsVO implements Serializable {

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

    private static final long serialVersionUID = 1L;
}