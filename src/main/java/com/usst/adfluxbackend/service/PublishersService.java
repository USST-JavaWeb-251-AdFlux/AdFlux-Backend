package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.vo.PublisherRevenueStatisticsVO;

import java.util.List;

/**
* @author 30637
* @description 针对表【publishers(网站站长信息表)】的数据库操作Service
* @createDate 2025-12-14 10:53:24
*/
public interface PublishersService extends IService<Publishers> {

    /**
     * 获取当前站长名下的网站列表
     *
     * @return 网站列表
     */
    List<Publishers> listSites();

    /**
     * 根据 websiteId 查询网站（只返回当前登录站长自己的记录）
     *
     * @param websiteId 网站 ID
     * @return Publishers 或 null（不存在或无权限）
     */
    Publishers getSiteById(Long websiteId);

    /**
     * 创建新网站
     *
     * @param websiteName 网站名称
     * @param domain 网站域名
     * @return 创建的网站信息
     */
    Publishers createSite(String websiteName, String domain);

    /**
     * 获取网站验证 Token
     *
     * @param siteId 网站 ID
     * @return 验证 Token
     */
    String getVerificationToken(Long siteId);

    /**
     * 触发网站所有权验证
     *
     * @param siteId 网站 ID
     * @return 验证结果
     */
    boolean verifySiteOwnership(Long siteId);

    /**
     * 获取发布主所有网站的收益统计
     */
    PublisherRevenueStatisticsVO getPublisherStatistics(DataOverviewQueryRequest queryRequest);

    /**
     * 获取单个网站的收益统计
     */
    PublisherRevenueStatisticsVO getPublisherSiteStatistics(Long websiteId, DataOverviewQueryRequest queryRequest);
}