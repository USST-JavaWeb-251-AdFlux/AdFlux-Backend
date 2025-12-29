package com.usst.adfluxbackend.controller;
import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.publisher.CreateSiteRequest;
import com.usst.adfluxbackend.model.entity.Publishers;
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
}