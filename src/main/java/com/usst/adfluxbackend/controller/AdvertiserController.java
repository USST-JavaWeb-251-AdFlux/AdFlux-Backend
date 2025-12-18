package com.usst.adfluxbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.model.dto.ad.*;
import com.usst.adfluxbackend.model.dto.advertiser.AdvertiserAddRequest;
import com.usst.adfluxbackend.model.dto.payment.PaymentMethodAddRequest;
import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.model.entity.AdvertiserPayments;
import com.usst.adfluxbackend.model.vo.*;
import com.usst.adfluxbackend.service.AdCategoriesService;
import com.usst.adfluxbackend.service.AdvertisersService;
import com.usst.adfluxbackend.service.AdvertiserPaymentsService;
import com.usst.adfluxbackend.service.AdvertisementsService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/advertisers")
public class AdvertiserController {

    @Resource
    private AdvertisersService advertisersService;
    
    @Resource
    private AdvertiserPaymentsService advertiserPaymentsService;
    
    @Resource
    private AdvertisementsService advertisementsService;

    @Resource
    private AdCategoriesService adCategoriesService;

    /**
     * 获取广告主公司信息
     *
     * @return 广告主公司信息
     */
    @GetMapping("/profile")
    public BaseResponse<AdvertiserProfileVO> getProfile() {
        AdvertiserProfileVO profile = advertisersService.getAdvertiserProfile();
        return ResultUtils.success(profile);
    }

    /**
     * 更新广告主公司名称
     *
     * @param addRequest 更新请求
     * @return 是否添加成功
     */
    @PutMapping("/company-name")
    public BaseResponse<Boolean> addCompanyName(@RequestBody AdvertiserAddRequest addRequest) {
        Boolean result = advertisersService.addCompanyName(addRequest);
        return ResultUtils.success(result);
    }
    
    /**
     * 获取当前广告主的支付方式列表
     *
     * @return 支付方式列表
     */
    @GetMapping("/payment-methods")
    public BaseResponse<List<PaymentMethodVO>> getPaymentMethods() {
        List<AdvertiserPayments> paymentMethods = advertiserPaymentsService.getPaymentMethods();
        List<PaymentMethodVO> paymentMethodVOS = paymentMethods.stream().map(payment -> {
            PaymentMethodVO vo = new PaymentMethodVO();
            vo.setPaymentId(payment.getPaymentId());
            // 银行卡号脱敏处理，保留前4位和后4位
            String cardNumber = payment.getCardNumber();
            if (cardNumber != null && cardNumber.length() > 8) {
                String maskedCardNumber = cardNumber.substring(0, 4) + 
                    "*".repeat(cardNumber.length() - 8) + 
                    cardNumber.substring(cardNumber.length() - 4);
                vo.setCardNumber(maskedCardNumber);
            } else {
                vo.setCardNumber(cardNumber);
            }
            vo.setBankName(payment.getBankName());
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(paymentMethodVOS);
    }
    
    /**
     * 为当前广告主添加支付方式
     *
     * @param addRequest 支付方式添加请求
     * @return 添加的支付方式
     */
    @PostMapping("/payment-methods")
    public BaseResponse<PaymentMethodVO> addPaymentMethod(@RequestBody PaymentMethodAddRequest addRequest) {
        AdvertiserPayments payment = advertiserPaymentsService.addPaymentMethod(addRequest);
        PaymentMethodVO vo = new PaymentMethodVO();
        vo.setPaymentId(payment.getPaymentId());
        // 银行卡号脱敏处理，保留前4位和后4位
        String cardNumber = payment.getCardNumber();
        if (cardNumber != null && cardNumber.length() > 8) {
            String maskedCardNumber = cardNumber.substring(0, 4) + 
                "*".repeat(cardNumber.length() - 8) + 
                cardNumber.substring(cardNumber.length() - 4);
            vo.setCardNumber(maskedCardNumber);
        } else {
            vo.setCardNumber(cardNumber);
        }
        vo.setBankName(payment.getBankName());
        return ResultUtils.success(vo);
    }
    
    /**
     * 分页获取当前广告主的广告列表
     *
     * @param reviewStatus 审核状态：0-待审核；1-通过；2-拒绝
     * @param isActive 是否启用：0-否；1-是
     * @param page 页码，从 1 开始，默认 1
     * @param pageSize 每页数量，默认 10
     * @return 广告分页列表
     */
    @GetMapping("/ads")
    public BaseResponse<IPage<AdvertisementVO>> listAdvertisements(
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) Integer isActive,
            @RequestParam(required = false, defaultValue ="1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        
        AdvertisementQueryRequest queryRequest = new AdvertisementQueryRequest();
        queryRequest.setReviewStatus(reviewStatus);
        queryRequest.setIsActive(isActive);
        queryRequest.setCurrent(page);
        queryRequest.setPageSize(pageSize);
        
        IPage<AdvertisementVO> advertisementPage = advertisementsService.listAdvertisementsByPage(queryRequest);
        return ResultUtils.success(advertisementPage);
    }
    /**
     * 创建新的广告
     *
     * @param createRequest 创建广告请求
     * @return 创建的广告信息
     */
    @PostMapping("/ads")
    public BaseResponse<AdvertisementVO> createAdvertisement(@RequestBody CreateAdvertisementRequest createRequest) {
        AdvertisementVO advertisementVO = advertisementsService.createAdvertisement(createRequest);
        return ResultUtils.success(advertisementVO);
    }
    
    /**
     * 获取广告详情
     *
     * @param adId 广告ID
     * @return 广告详情
     */
    @GetMapping("/ads/{adId}")
    public BaseResponse<AdvertisementVO> getAdvertisement(@PathVariable Long adId) {
        AdvertisementVO advertisementVO = advertisementsService.getAdvertisement(adId);
        return ResultUtils.success(advertisementVO);
    }
    
    /**
     * 更新广告信息
     *
     * @param adId 广告ID
     * @param updateRequest 更新广告请求
     * @return 更新后的广告信息
     */
    @PutMapping("/ads/{adId}")
    public BaseResponse<AdvertisementVO> updateAdvertisement(@PathVariable Long adId, 
                                                             @RequestBody UpdateAdvertisementRequest updateRequest) {
        AdvertisementVO advertisementVO = advertisementsService.updateAdvertisement(adId, updateRequest);
        return ResultUtils.success(advertisementVO);
    }
    
    /**
     * 逻辑删除广告
     *
     * @param adId 广告ID
     * @return 是否删除成功
     */
    @DeleteMapping("/ads/{adId}")
    public BaseResponse<Boolean> deleteAdvertisement(@PathVariable Long adId) {
        Boolean result = advertisementsService.deleteAdvertisement(adId);
        return ResultUtils.success(result);
    }
    
    /**
     * 切换广告投放状态
     *
     * @param adId 广告ID
     * @param toggleRequest 切换状态请求
     * @return 更新后的广告信息
     */
    @PutMapping("/ads/{adId}/status")
    public BaseResponse<AdvertisementVO> toggleAdStatus(@PathVariable Long adId,
                                                        @RequestBody ToggleAdStatusRequest toggleRequest) {
        AdvertisementVO advertisementVO = advertisementsService.toggleAdStatus(adId, toggleRequest);
        return ResultUtils.success(advertisementVO);
    }
    
    /**
     * 获取广告主数据概览
     *
     * @param queryRequest 查询条件
     * @return 数据概览
     */
    @GetMapping("/statistics/summary")
    public BaseResponse<DataOverviewVO> getDataOverview(DataOverviewQueryRequest queryRequest) {
        DataOverviewVO dataOverviewVO = advertisementsService.getDataOverview(queryRequest);
        return ResultUtils.success(dataOverviewVO);
    }
    
    /**
     * 获取广告统计数据
     *
     * @param adId 广告ID
     * @param queryRequest 查询条件
     * @return广告统计数据
     */
    @GetMapping("/ads/{adId}/statistics")
    public BaseResponse<AdvertisementStatisticsVO> getAdvertisementStatistics(@PathVariable Long adId,
                                                                             DataOverviewQueryRequest queryRequest) {
        AdvertisementStatisticsVO statisticsVO = advertisementsService.getAdvertisementStatistics(adId, queryRequest);
        return ResultUtils.success(statisticsVO);
    }

    /**
     * 获取广告分类列表
     *
     * @return 广告分类列表
     */
    @GetMapping("/categories")
    public BaseResponse<List<CategoryVO>> listAllCategories() {
        List<AdCategories> categories = adCategoriesService.listAllCategories();
        List<CategoryVO> categoryVOS = categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(categoryVOS);
    }
}