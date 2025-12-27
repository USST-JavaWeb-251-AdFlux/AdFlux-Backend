package com.usst.adfluxbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.usst.adfluxbackend.model.entity.AdPlacements;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.vo.PublisherStatisticsVO;

import java.util.List;

/**
* @author 30637
* @description 针对表【ad_placements(广告位信息表)】的数据库操作Service
* @createDate 2025-12-15
*/
public interface AdPlacementsService extends IService<AdPlacements> {

    /**
     * 获取当前站长名下所有网站的广告位列表
     *
     * @param websiteId 按网站筛选广告位
     * @return 广告位列表
     */
    List<AdPlacements> listAdSlots(Long websiteId);

    /**
     * 创建广告位
     *
     * @param websiteId 网站 ID
     * @param placementName 广告位名称
     * @param adLayout 广告版式
     * @return 创建的广告位
     */
    AdPlacements createAdSlot(Long websiteId, String placementName, Integer adLayout);

    /**
     * 获取广告位详情
     *
     * @param adSlotId 广告位 ID
     * @return 广告位详情
     */
    AdPlacements getAdSlotDetail(Long adSlotId);

    /**
     * 更新广告位名称
     *
     * @param adSlotId 广告位 ID
     * @param placementName 新名称
     * @return 是否更新成功
     */
    boolean updateAdSlotName(Long adSlotId, String placementName);

    /**
     * 获取广告位集成代码
     *
     * @param adSlotId 广告位 ID
     * @return 集成代码
     */
    String getIntegrationCode(Long adSlotId);
    
    /**
     * 获取站长收益统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收益统计
     */
    PublisherStatisticsVO getPublisherStatistics(String startDate, String endDate);
}