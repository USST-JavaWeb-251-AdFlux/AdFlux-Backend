package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.entity.Publishers;
import com.baomidou.mybatisplus.extension.service.IService;

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
}