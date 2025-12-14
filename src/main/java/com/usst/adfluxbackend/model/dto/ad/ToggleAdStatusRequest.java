package com.usst.adfluxbackend.model.dto.ad;

import lombok.Data;

import java.io.Serializable;

/**
 * 切换广告状态请求
 */
@Data
public class ToggleAdStatusRequest implements Serializable {

    /**
     * 是否启用投放（0-否；1-是）
     */
    private Integer isActive;

    private static final long serialVersionUID = 1L;
}