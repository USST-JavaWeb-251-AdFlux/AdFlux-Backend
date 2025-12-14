package com.usst.adfluxbackend.model.dto.statistic;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据概览查询请求
 */
@Data
public class DataOverviewQueryRequest implements Serializable {

    /**
     * 开始日期 yyyy-MM-dd
     */
    private String startDate;

    /**
     * 结束日期 yyyy-MM-dd
     */
    private String endDate;

    private static final long serialVersionUID = 1L;
}