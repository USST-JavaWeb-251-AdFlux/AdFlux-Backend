package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.publisher.CreateAdSlotRequest;
import com.usst.adfluxbackend.model.dto.publisher.UpdateAdSlotRequest;
import com.usst.adfluxbackend.model.dto.publisher.CreateSiteRequest;
import com.usst.adfluxbackend.model.entity.AdPlacements;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.vo.AdSlotVO;
import com.usst.adfluxbackend.model.vo.IntegrationCodeVO;
import com.usst.adfluxbackend.model.vo.PublisherSiteVO;
import com.usst.adfluxbackend.model.vo.PublisherStatisticsVO;
import com.usst.adfluxbackend.service.AdPlacementsService;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publishers")
@RequireRole("PUBLISHER")
public class PublishersController {
    
    @Resource
    private PublishersService publishersService;
    
    @Resource
    private AdPlacementsService adPlacementsService;
    
    /**
     * 获取我的网站列表
     *
     * @return 网站列表
     */
    @GetMapping("/sites")
    public BaseResponse<List<PublisherSiteVO>> listSites() {
        List<Publishers> sites = publishersService.listSites();
        List<PublisherSiteVO> siteVOS = sites.stream().map(site -> {
            PublisherSiteVO vo = new PublisherSiteVO();
            BeanUtils.copyProperties(site, vo);
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(siteVOS);
    }
    /**
     * 根据id 获取我的网站列表
     *
     * @return 网站列表
     */
    @GetMapping("/sites/{websiteId}")
    public BaseResponse<PublisherSiteVO> getSiteDetail(@PathVariable Long websiteId) {
        Publishers site = publishersService.getSiteById(websiteId);
        ThrowUtils.throwIf(site == null, ErrorCode.NOT_FOUND_ERROR, "站点不存在或无权限访问");
        PublisherSiteVO siteVO = new PublisherSiteVO();
        BeanUtils.copyProperties(site, siteVO);
        return ResultUtils.success(siteVO);
    }
    
    /**
     * 提交新网站信息
     *
     * @param createSiteRequest 创建网站请求
     * @return 新增网站信息
     */
    @PostMapping("/sites")
    public BaseResponse<PublisherSiteVO> createSite(@RequestBody CreateSiteRequest createSiteRequest) {
        Publishers site = publishersService.createSite(
                createSiteRequest.getWebsiteName(), 
                createSiteRequest.getDomain());
        
        PublisherSiteVO siteVO = new PublisherSiteVO();
        BeanUtils.copyProperties(site, siteVO);
        return ResultUtils.success(siteVO);
    }
    
    /**
     * 获取网站验证 Token
     *
     * @param siteId 网站 ID
     * @return 验证 Token
     */
    @GetMapping("/sites/{siteId}/verification-token")
    public BaseResponse<String> getVerificationToken(@PathVariable Long siteId) {
        String token = publishersService.getVerificationToken(siteId);
        return ResultUtils.success(token);
    }
    
    /**
     * 触发网站所有权验证
     *
     * @param siteId 网站 ID
     * @return 验证结果
     */
    @PostMapping("/sites/{siteId}/verification")
    public BaseResponse<Boolean> verifySiteOwnership(@PathVariable Long siteId) {
        boolean result = publishersService.verifySiteOwnership(siteId);
        return ResultUtils.success(result);
    }
    
    /**
     * 获取广告位列表
     *
     * @param websiteId 按网站筛选广告位
     * @return 广告位列表
     */
    @GetMapping("/ad-slots")
    public BaseResponse<List<AdSlotVO>> listAdSlots(@RequestParam(required = false) Long websiteId) {
        List<AdPlacements> adSlots = adPlacementsService.listAdSlots(websiteId);
        List<AdSlotVO> adSlotVOS = adSlots.stream().map(adSlot -> {
            AdSlotVO vo = new AdSlotVO();
            BeanUtils.copyProperties(adSlot, vo);
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(adSlotVOS);
    }

    /**
     * 创建广告位
     *
     * @param createAdSlotRequest 创建广告位请求
     * @return 新建广告位信息
     */
    @PostMapping("/ad-slots")
    public BaseResponse<AdSlotVO> createAdSlot(@RequestBody CreateAdSlotRequest createAdSlotRequest) {
        AdPlacements adSlot = adPlacementsService.createAdSlot(
                createAdSlotRequest.getWebsiteId(),
                createAdSlotRequest.getPlacementName(),
                createAdSlotRequest.getAdLayout());
        
        AdSlotVO adSlotVO = new AdSlotVO();
        BeanUtils.copyProperties(adSlot, adSlotVO);
        return ResultUtils.success(adSlotVO);
    }
    
    /**
     * 获取广告位详情
     *
     * @param adSlotId 广告位 ID
     * @return 广告位详情
     */
    @GetMapping("/ad-slots/{adSlotId}")
    public BaseResponse<AdSlotVO> getAdSlotDetail(@PathVariable Long adSlotId) {
        AdPlacements adSlot = adPlacementsService.getAdSlotDetail(adSlotId);
        
        if (adSlot == null) {
            // 抛异常
        }
        
        AdSlotVO adSlotVO = new AdSlotVO();
        BeanUtils.copyProperties(adSlot, adSlotVO);
        return ResultUtils.success(adSlotVO);
    }
    
    /**
     * 修改广告位名称
     *
     * @param adSlotId 广告位 ID
     * @param updateAdSlotRequest 更新广告位请求
     * @return 是否更新成功
     */
    @PutMapping("/ad-slots/{adSlotId}")
    public BaseResponse<Boolean> updateAdSlotName(@PathVariable Long adSlotId,
                                                 @RequestBody UpdateAdSlotRequest updateAdSlotRequest) {
        boolean result = adPlacementsService.updateAdSlotName(adSlotId, updateAdSlotRequest.getPlacementName());
        return ResultUtils.success(result);
    }
    
    /**
     * 获取广告位集成代码
     *
     * @param adSlotId 广告位 ID
     * @return 集成代码
     */
    @GetMapping("/ad-slots/{adSlotId}/integration-code")
    public BaseResponse<IntegrationCodeVO> getIntegrationCode(@PathVariable Long adSlotId) {
        String scriptTemplate = adPlacementsService.getIntegrationCode(adSlotId);
        
        if (scriptTemplate == null) {

//            return ResultUtils.error(404, "广告位不存在或无权限访问");
        }
        
        IntegrationCodeVO integrationCodeVO = new IntegrationCodeVO();
        integrationCodeVO.setPlacementId(adSlotId);
        integrationCodeVO.setScriptTemplate(scriptTemplate);
        return ResultUtils.success(integrationCodeVO);
    }
    
    /**
     * 收益统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 收益统计
     */
    @GetMapping("/statistics")
    public BaseResponse<PublisherStatisticsVO> getPublisherStatistics(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PublisherStatisticsVO statistics = adPlacementsService.getPublisherStatistics(startDate, endDate);
        return ResultUtils.success(statistics);
    }
}