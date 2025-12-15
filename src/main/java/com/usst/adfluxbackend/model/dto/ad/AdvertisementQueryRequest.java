package com.usst.adfluxbackend.model.dto.ad;

import com.usst.adfluxbackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 广告查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdvertisementQueryRequest extends PageRequest implements Serializable {

    /**
     * 审核状态：0-待审核；1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 是否启用：0-否；1-是
     */
    private Integer isActive;

    private static final long serialVersionUID = 1L;
}