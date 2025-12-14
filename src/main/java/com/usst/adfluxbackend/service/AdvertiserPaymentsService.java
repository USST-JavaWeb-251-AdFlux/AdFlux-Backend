package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.dto.payment.PaymentMethodAddRequest;
import com.usst.adfluxbackend.model.entity.AdvertiserPayments;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 30637
* @description 针对表【advertiser_payments(广告业主付款信息)】的数据库操作Service
* @createDate 2025-12-14 10:54:50
*/
public interface AdvertiserPaymentsService extends IService<AdvertiserPayments> {

    /**
     * 获取当前广告主的支付方式列表
     *
     * @return 支付方式列表
     */
    List<AdvertiserPayments> getPaymentMethods();

    /**
     * 为当前广告主添加支付方式
     *
     * @param paymentMethodAddRequest 支付方式添加请求
     * @return 新增的支付方式
     */
    AdvertiserPayments addPaymentMethod(PaymentMethodAddRequest paymentMethodAddRequest);
}