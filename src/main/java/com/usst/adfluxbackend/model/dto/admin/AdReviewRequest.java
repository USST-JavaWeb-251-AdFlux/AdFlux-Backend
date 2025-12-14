package com.usst.adfluxbackend.model.dto.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 广告审核请求
 */
@Data
public class AdReviewRequest implements Serializable {

    /**
     * 审核结果：1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 拒绝原因，审核通过时可为空
     */
    private String reason;

    private static final long serialVersionUID = 1L;
}