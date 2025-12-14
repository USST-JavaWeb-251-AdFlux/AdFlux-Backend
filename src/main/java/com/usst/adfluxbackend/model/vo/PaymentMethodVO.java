package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付方式信息视图
 */
@Data
public class PaymentMethodVO implements Serializable {

    /**
     * 支付信息 ID
     */
    private Long paymentId;

    /**
     * 银行卡号（可做脱敏处理返回）
     */
    private String cardNumber;

    /**
     * 银行名称
     */
    private String bankName;

    private static final long serialVersionUID = 1L;
}