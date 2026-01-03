package com.usst.adfluxbackend.model.dto.tracker;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新广告展示状态请求 DTO
 *
 * 用于前端或埋点系统在广告展示过程中，
 * 上报广告的展示时长和点击状态。
 *
 * 对应接口：
 * PUT /track/ad-slot/{displayId}
 */
@Data
public class UpdateAdDisplayRequest implements Serializable {

    /**
     * 广告已展示的累计时长（单位：秒）
     * 只允许递增，不能回退
     */
    private Integer duration;

    /**
     * 是否发生点击
     * 0：未点击
     * 1：已点击
     *
     * 一旦为 1，不允许回退为 0
     */
    private Integer clicked;

    private static final long serialVersionUID = 1L;
}