package com.usst.adfluxbackend.controller;
import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.publisher.CreateSiteRequest;
import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.vo.PublisherRevenueStatisticsVO;
import com.usst.adfluxbackend.model.vo.PublisherSiteVO;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publishers")
@RequireRole("publisher")
public class PublishersController {

    @Resource
    private PublishersService publishersService;

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
     * 获取当前发布主所有网站的收益统计（按时间范围）
     */
    @GetMapping("/statistics/summary")
    public BaseResponse<PublisherRevenueStatisticsVO> getPublisherStatistics(DataOverviewQueryRequest queryRequest) {
        PublisherRevenueStatisticsVO stats = publishersService.getPublisherStatistics(queryRequest);
        return ResultUtils.success(stats);
    }

    /**
     * 获取单个网站的收益统计（按时间范围）
     */
    @GetMapping("/sites/{websiteId}/statistics")
    public BaseResponse<PublisherRevenueStatisticsVO> getPublisherSiteStatistics(
            @PathVariable Long websiteId, DataOverviewQueryRequest queryRequest) {
        PublisherRevenueStatisticsVO stats = publishersService.getPublisherSiteStatistics(websiteId, queryRequest);
        return ResultUtils.success(stats);
    }


    /**
     * 触发网站所有权验证：
     * 1. 固定使用 HTTPS 访问站点首页并跟随重定向
     * 2. 解析 meta[name=adflux-verification] 的 content
     * 3. 返回该 content 与数据库内验证码的匹配结果
     *
     * @param siteId 网站 ID
     * @return 是否匹配验证码
     */
    @PostMapping("/sites/{siteId}/verification")
    public BaseResponse<Boolean> verifySiteOwnership(@PathVariable Long siteId) {
        // Delegate verification logic to service
        boolean result = publishersService.verifySiteOwnership(siteId);
        return ResultUtils.success(result);
    }
}
