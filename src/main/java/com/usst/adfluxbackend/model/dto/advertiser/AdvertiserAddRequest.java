package com.usst.adfluxbackend.model.dto.advertiser;

import lombok.Data;

import java.io.Serializable;

/**
 * 广告主更新请求
 */
@Data
public class AdvertiserAddRequest implements Serializable {

    /**
     * 公司名称
     */
    private String companyName;

    private static final long serialVersionUID = 1L;
}