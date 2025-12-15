package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 网站信息视图
 */
@Data
public class PublisherSiteVO implements Serializable {

    /**
     * 网站 ID
     */
    private Long websiteId;

    /**
     * 网站名称
     */
    private String websiteName;

    /**
     * 网站地址 / 域名
     */
    private String domain;

    /**
     * 是否已通过验证（0-未通过；1-通过）
     */
    private Integer isVerified;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 验证时间，未验证则为 null
     */
    private Date verifyTime;

    /**
     * 验证代码（是否返回由后端安全策略决定）
     */
    private String verificationToken;

    private static final long serialVersionUID = 1L;
}