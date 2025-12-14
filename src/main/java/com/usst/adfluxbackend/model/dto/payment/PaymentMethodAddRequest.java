package com.usst.adfluxbackend.model.dto.payment;

import lombok.Data;

import java.io.Serializable;

/**
 * 支付方式添加请求
 */
@Data
public class PaymentMethodAddRequest implements Serializable {

    /**
     * 银行卡号
     */
    private String cardNumber;

    /**
     * 银行名称
     */
    private String bankName;

    private static final long serialVersionUID = 1L;
}