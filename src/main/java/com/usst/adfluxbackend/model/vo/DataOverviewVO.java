package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据概览视图
 */
@Data
public class DataOverviewVO implements Serializable {

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
     * 总花费
     */
    private Double totalSpend;

    private static final long serialVersionUID = 1L;
}