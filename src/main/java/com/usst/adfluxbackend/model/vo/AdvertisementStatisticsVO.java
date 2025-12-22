package com.usst.adfluxbackend.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 广告统计信息视图
 */
@Data
public class AdvertisementStatisticsVO implements Serializable {

    /**
     * 广告 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adId;

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