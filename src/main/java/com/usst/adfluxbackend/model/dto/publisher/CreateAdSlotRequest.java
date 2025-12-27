package com.usst.adfluxbackend.model.dto.publisher;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建广告位请求
 */
@Data
public class CreateAdSlotRequest implements Serializable {

    /**
     * 网站 ID，必须是当前站长名下的网站
     */
    private Long websiteId;

    /**
     * 广告位名称，如"首页顶部横幅"
     */
    private String placementName;

    /**
     * 广告版式（0-video； 1-banner； 2-sidebar）
     */
    private Integer adLayout;

    private static final long serialVersionUID = 1L;
}