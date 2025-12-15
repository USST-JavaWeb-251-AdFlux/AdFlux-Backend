package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 广告位信息视图
 */
@Data
public class AdSlotVO implements Serializable {

    /**
     * 广告位 ID
     */
    private Long placementId;

    /**
     * 网站 ID
     */
    private Long websiteId;

    /**
     * 广告位名称
     */
    private String placementName;

    /**
     * 广告版式（banner/sidebar 等）
     */
    private String adLayout;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}