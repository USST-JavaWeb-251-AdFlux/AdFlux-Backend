package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 页面访问跟踪返回 VO
 */
@Data
public class TrackPageViewVO implements Serializable {

    /**
     * 访问记录 ID（字符串，避免前端精度丢失）
     */
    private String visitId;

    private static final long serialVersionUID = 1L;
}
