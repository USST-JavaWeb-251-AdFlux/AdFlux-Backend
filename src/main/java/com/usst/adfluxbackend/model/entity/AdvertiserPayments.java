package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 广告业主付款信息
 * @TableName advertiser_payments
 */
@TableName(value ="advertiser_payments")
@Data
public class AdvertiserPayments {
    /**
     * 支付信息 ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long paymentId;

    /**
     * 广告业主 ID
     */
    private Long advertiserId;

    /**
     * 银行卡号
     */
    private String cardNumber;

    /**
     * 银行名称
     */
    private String bankName;
}