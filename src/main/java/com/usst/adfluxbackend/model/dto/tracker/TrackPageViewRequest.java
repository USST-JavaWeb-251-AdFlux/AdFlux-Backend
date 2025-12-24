package com.usst.adfluxbackend.model.dto.tracker;

import lombok.Data;
import java.io.Serializable;

/**
 * 页面访问记录请求对象
 */
@Data
public class TrackPageViewRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 网站域名
     */
    private String domain;

    /**
     * 广告类别名称
     */
    private String categoryName;

    /**
     * 匿名用户标识
     */
    private String trackId;
}
