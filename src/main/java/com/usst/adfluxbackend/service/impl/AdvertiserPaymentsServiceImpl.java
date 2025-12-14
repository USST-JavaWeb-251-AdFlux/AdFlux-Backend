package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.mapper.AdvertiserPaymentsMapper;
import com.usst.adfluxbackend.model.dto.payment.PaymentMethodAddRequest;
import com.usst.adfluxbackend.model.entity.AdvertiserPayments;
import com.usst.adfluxbackend.service.AdvertiserPaymentsService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author 30637
* @description 针对表【advertiser_payments(广告业主付款信息)】的数据库操作Service实现
* @createDate 2025-12-14 10:54:50
*/
@Service
public class AdvertiserPaymentsServiceImpl extends ServiceImpl<AdvertiserPaymentsMapper, AdvertiserPayments>
    implements AdvertiserPaymentsService{

    /**
     * 获取当前广告主的支付方式列表
     *
     * @return 支付方式列表
     */
    @Override
    public List<AdvertiserPayments> getPaymentMethods() {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        return this.lambdaQuery()
                .eq(AdvertiserPayments::getAdvertiserId, currentAdvertiserId)
                .list();
    }

    /**
     * 为当前广告主添加支付方式
     *
     * @param paymentMethodAddRequest 支付方式添加请求
     * @return 新增的支付方式
     */
    @Override
    public AdvertiserPayments addPaymentMethod(PaymentMethodAddRequest paymentMethodAddRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        AdvertiserPayments payment = new AdvertiserPayments();
        payment.setAdvertiserId(currentAdvertiserId);
        payment.setCardNumber(paymentMethodAddRequest.getCardNumber());
        payment.setBankName(paymentMethodAddRequest.getBankName());
        
        this.save(payment);
        return payment;
    }
}