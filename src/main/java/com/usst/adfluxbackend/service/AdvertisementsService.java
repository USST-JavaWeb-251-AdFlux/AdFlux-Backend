package com.usst.adfluxbackend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.dto.ad.AdvertisementQueryRequest;
import com.usst.adfluxbackend.model.dto.ad.CreateAdvertisementRequest;
import com.usst.adfluxbackend.model.dto.ad.UpdateAdvertisementRequest;
import com.usst.adfluxbackend.model.dto.ad.ToggleAdStatusRequest;
import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.Advertisements;
import com.usst.adfluxbackend.model.vo.AdvertisementVO;
import com.usst.adfluxbackend.model.vo.AdvertisementStatisticsVO;
import com.usst.adfluxbackend.model.vo.AdvertisementReviewVO;
import com.usst.adfluxbackend.model.vo.DataOverviewStatisticsVO;

/**
* @author 30637
* @description 针对表【advertisements(广告信息表)】的数据库操作Service
* @createDate 2025-12-14 10:41:14
*/
public interface AdvertisementsService extends IService<Advertisements> {

    /**
     * 根据条件分页查询当前广告主的广告列表
     *
     * @param queryRequest 查询条件
     * @return 广告分页列表
     */
    IPage<AdvertisementVO> listAdvertisementsByPage(AdvertisementQueryRequest queryRequest);

    /**
     * 创建新的广告
     *
     * @param createRequest 创建广告请求
     * @return 创建的广告信息
     */
    AdvertisementVO createAdvertisement(CreateAdvertisementRequest createRequest);

    /**
     * 获取广告详情
     *
     * @param adId 广告ID
     * @return 广告详情
     */
    AdvertisementVO getAdvertisement(Long adId);

    /**
     * 更新广告信息
     *
     * @param adId 广告ID
     * @param updateRequest 更新广告请求
     * @return 更新后的广告信息
     */
    AdvertisementVO updateAdvertisement(Long adId, UpdateAdvertisementRequest updateRequest);

    /**
     *物理删除广告
     *
     * @param adId 广告ID
     * @return 是否删除成功
     */
    Boolean deleteAdvertisement(Long adId);

    /**
     * 切换广告投放状态
     *
     * @param adId 广告ID
     * @param toggleRequest 切换状态请求
     * @return 更新后的广告信息
     */
    AdvertisementVO toggleAdStatus(Long adId, ToggleAdStatusRequest toggleRequest);

    /**
     * 获取广告主数据概览
     * 获取广告主层面的数据概览（返回结构与单广告统计一致，adId 字段为 null）
     * @param queryRequest 查询条件
     * @return 数据概览
     */
    DataOverviewStatisticsVO getDataOverview(DataOverviewQueryRequest queryRequest);

    /**
     * 获取广告统计数据
     *
     * @param adId 广告ID
     * @param queryRequest 查询条件
     * @return 广告统计数据
     */
    AdvertisementStatisticsVO getAdvertisementStatistics(Long adId, DataOverviewQueryRequest queryRequest);
    
    /**
     * 管理员分页查询广告列表（可按审核状态筛选）
     *
     * @param status 审核状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 广告分页列表
     */
    IPage<AdvertisementVO> listAdsForAdmin(Integer status, Integer page, Integer pageSize);
    
    /**
     * 管理员审核广告
     *
     * @param adId 广告ID
     * @param reviewStatus 审核状态：1-通过；2-拒绝
     * @param reason 拒绝原因
     * @return 更新后的广告信息
     */
    AdvertisementReviewVO reviewAdvertisement(Long adId, Integer reviewStatus, String reason);
}