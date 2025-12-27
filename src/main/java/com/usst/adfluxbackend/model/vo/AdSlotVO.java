package com.usst.adfluxbackend.model.vo;

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
    private Long displayId;

    /**
     * 媒体文件地址（图片/视频）
     */
    private String mediaUrl;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 落地页链接
     */
    private String landingPage;

    private static final long serialVersionUID = 1L;
}