package com.usst.adfluxbackend.model.dto.publisher;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建网站请求
 */
@Data
public class CreateSiteRequest implements Serializable {

    /**
     * 网站名称
     */
    private String websiteName;

    /**
     * 网站域名
     */
    private String domain;

    private static final long serialVersionUID = 1L;
}