package com.usst.adfluxbackend.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 广告审核结果视图（仅用于审核接口）
 */
@Data
public class AdvertisementReviewVO implements Serializable {

    /**
     * 广告 ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adId;

    /**
     * 审核状态（0-待审核；1-通过；2-拒绝）
     */
    private Integer reviewStatus;

    /**
     * 拒绝原因（审核拒绝时才有）
     */
    private String rejectReason;

    /**
     * 编辑时间
     */
    private Date editTime;

    private static final long serialVersionUID = 1L;
}
