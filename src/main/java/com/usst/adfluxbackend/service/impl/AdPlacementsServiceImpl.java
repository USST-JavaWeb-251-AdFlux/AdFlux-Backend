package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.mapper.AdDisplaysMapper;
import com.usst.adfluxbackend.mapper.AdPlacementsMapper;
import com.usst.adfluxbackend.model.entity.AdDisplays;
import com.usst.adfluxbackend.model.entity.AdPlacements;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.vo.PublisherStatisticsVO;
import com.usst.adfluxbackend.service.AdPlacementsService;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @author 30637
* @description 针对表【ad_placements(广告位信息表)】的数据库操作Service实现
* @createDate 2025-12-15
*/
@Service
public class AdPlacementsServiceImpl extends ServiceImpl<AdPlacementsMapper, AdPlacements>
    implements AdPlacementsService {

    @Resource
    private PublishersService publishersService;
    
    @Resource
    private AdDisplaysMapper adDisplaysMapper;

    /**
     * 获取当前站长名下所有网站的广告位列表
     *
     * @param websiteId 按网站筛选广告位
     * @return 广告位列表
     */
    @Override
    public List<AdPlacements> listAdSlots(Long websiteId) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 构造查询条件
        LambdaQueryWrapper<AdPlacements> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了网站ID，则添加筛选条件
        if (websiteId != null) {
            // 验证该网站是否属于当前站长
            Publishers site = publishersService.getOne(new LambdaQueryWrapper<Publishers>()
                    .eq(Publishers::getWebsiteId, websiteId)
                    .eq(Publishers::getPublisherId, currentPublisherId));
            
            if (site != null) {
                queryWrapper.eq(AdPlacements::getWebsiteId, websiteId);
            }
        } else {
            // 如果没有指定网站ID，则查询当前站长所有网站的广告位
            List<Publishers> sites = publishersService.list(new LambdaQueryWrapper<Publishers>()
                    .eq(Publishers::getPublisherId, currentPublisherId));
            
            if (!sites.isEmpty()) {
                queryWrapper.in(AdPlacements::getWebsiteId, 
                        sites.stream().map(Publishers::getWebsiteId).toArray());
            } else {
                // 如果当前站长没有任何网站，则返回空列表
                return List.of();
            }
        }
        
        return this.list(queryWrapper);
    }

    /**
     * 创建广告位
     *
     * @param websiteId 网站 ID
     * @param placementName 广告位名称
     * @param adLayout 广告版式
     * @return 创建的广告位
     */
    @Override
    public AdPlacements createAdSlot(Long websiteId, String placementName, String adLayout) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 验证该网站是否属于当前站长
        Publishers site = publishersService.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, websiteId)
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        if (site == null) {
            throw new RuntimeException("网站不存在或不属于当前站长");
        }
        
        // 创建广告位
        AdPlacements adSlot = new AdPlacements();
        adSlot.setWebsiteId(websiteId);
        adSlot.setPlacementName(placementName);
        adSlot.setAdLayout(adLayout);
        adSlot.setCreateTime(new Date());
        
        this.save(adSlot);
        return adSlot;
    }

    /**
     * 获取广告位详情
     *
     * @param adSlotId 广告位 ID
     * @return 广告位详情
     */
    @Override
    public AdPlacements getAdSlotDetail(Long adSlotId) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 查询广告位详情
        AdPlacements adSlot = this.getById(adSlotId);
        
        if (adSlot == null) {
            return null;
        }
        
        // 验证该广告位所属的网站是否属于当前站长
        Publishers site = publishersService.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, adSlot.getWebsiteId())
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        return site != null ? adSlot : null;
    }

    /**
     * 更新广告位名称
     *
     * @param adSlotId 广告位 ID
     * @param placementName 新名称
     * @return 是否更新成功
     */
    @Override
    public boolean updateAdSlotName(Long adSlotId, String placementName) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 查询广告位详情
        AdPlacements adSlot = this.getById(adSlotId);
        
        if (adSlot == null) {
            return false;
        }
        
        // 验证该广告位所属的网站是否属于当前站长
        Publishers site = publishersService.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, adSlot.getWebsiteId())
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        if (site == null) {
            return false;
        }
        
        // 更新广告位名称
        adSlot.setPlacementName(placementName);
        return this.updateById(adSlot);
    }

    /**
     * 获取广告位集成代码
     *
     * @param adSlotId 广告位 ID
     * @return 集成代码
     */
    @Override
    public String getIntegrationCode(Long adSlotId) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 查询广告位详情
        AdPlacements adSlot = this.getById(adSlotId);
        
        if (adSlot == null) {
            return null;
        }
        
        // 验证该广告位所属的网站是否属于当前站长
        Publishers site = publishersService.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, adSlot.getWebsiteId())
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        if (site == null) {
            return null;
        }
        
        // 生成集成代码
        return String.format("<script src=\"/api/public/ads/serve?placementId=%d&cacheBuster=%s\"></script>", 
                adSlotId, UUID.randomUUID().toString());
    }
    
    /**
     * 获取站长收益统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收益统计
     */
    @Override
    public PublisherStatisticsVO getPublisherStatistics(String startDate, String endDate) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 获取当前站长的所有网站
        List<Publishers> sites = publishersService.list(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        if (sites.isEmpty()) {
            PublisherStatisticsVO statistics = new PublisherStatisticsVO();
            statistics.setTotalImpressions(0L);
            statistics.setTotalClicks(0L);
            statistics.setEstimatedRevenue(0.0);
            return statistics;
        }
        
        // 构造查询条件
        LambdaQueryWrapper<AdDisplays> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AdDisplays::getWebsiteId, 
                sites.stream().map(Publishers::getWebsiteId).toArray());
        
        // 添加日期筛选条件
        if (StringUtils.hasText(startDate)) {
            queryWrapper.ge(AdDisplays::getDisplayTime, startDate);
        }
        if (StringUtils.hasText(endDate)) {
            queryWrapper.le(AdDisplays::getDisplayTime, endDate);
        }
        
        // 查询展示数据
        List<AdDisplays> adDisplaysList = adDisplaysMapper.selectList(queryWrapper);
        
        // 计算统计数据
        long totalImpressions = adDisplaysList.size();
        long totalClicks = adDisplaysList.stream()
                .mapToLong(display -> display.getClicked() != null ? display.getClicked() : 0)
                .sum();
        
        // 简单计算预估收益（每次点击0.01元）
        double estimatedRevenue = totalClicks * 0.01;
        
        PublisherStatisticsVO statistics = new PublisherStatisticsVO();
        statistics.setTotalImpressions(totalImpressions);
        statistics.setTotalClicks(totalClicks);
        statistics.setEstimatedRevenue(estimatedRevenue);
        
        return statistics;
    }
}