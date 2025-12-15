package com.usst.adfluxbackend.model.dto.publisher;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新广告位请求
 */
@Data
public class UpdateAdSlotRequest implements Serializable {

    /**
     * 新名称
     */
    private String placementName;

    private static final long serialVersionUID = 1L;
}