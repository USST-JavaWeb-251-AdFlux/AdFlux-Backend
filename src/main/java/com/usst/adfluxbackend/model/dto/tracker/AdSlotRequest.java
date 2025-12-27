package com.usst.adfluxbackend.model.dto.tracker;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.io.Serializable;

/**
 * 广告位请求DTO
 */
@Data
public class AdSlotRequest implements Serializable {
    /**
     * 由 tracker 生成并保存在前端的随机 UUID，用于标识匿名用户
     */
    private String trackId;

    /**
     * 用户当前访问的网站域名
     */
    private String domain;

    /**
     * 请求的广告类型
     */
    private Integer adType;

    /**
     * 广告的展现形式
     */
    private String adLayout;

    private static final long serialVersionUID = 1L;
}