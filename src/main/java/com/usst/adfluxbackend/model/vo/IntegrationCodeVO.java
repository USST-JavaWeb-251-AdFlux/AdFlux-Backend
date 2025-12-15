package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 集成代码视图
 */
@Data
public class IntegrationCodeVO implements Serializable {

    /**
     * 广告位 ID
     */
    private Long placementId;

    /**
     * 嵌入页面的 JS 代码字符串
     */
    private String scriptTemplate;

    private static final long serialVersionUID = 1L;
}