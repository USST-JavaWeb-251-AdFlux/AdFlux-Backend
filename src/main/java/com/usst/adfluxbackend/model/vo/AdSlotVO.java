package com.usst.adfluxbackend.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 广告位响应VO
 */
@Data
public class AdSlotVO implements Serializable {
    /**
     * 展示数据：displayId（用于后续接口更新停留时长、点击情况等）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long displayId;

    /**
     * 媒体文件地址（图片/视频）
     */
    private String mediaUrl;

    /**
     * 广告版式（banner/sidebar 等）
     */
    private Integer adLayout;

    /**
     * 落地页链接
     */
    private String landingPage;

    private static final long serialVersionUID = 1L;
}