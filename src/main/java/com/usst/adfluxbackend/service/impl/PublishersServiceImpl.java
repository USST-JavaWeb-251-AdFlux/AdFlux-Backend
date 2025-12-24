package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.mapper.PublishersMapper;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @author 30637
* @description 针对表【publishers(网站站长信息表)】的数据库操作Service实现
* @createDate 2025-12-14 10:53:24
*/
@Service
public class PublishersServiceImpl extends ServiceImpl<PublishersMapper, Publishers> implements PublishersService{

    /**
     * 获取当前站长名下的网站列表
     *
     * @return 网站列表
     */
    @Override
    public List<Publishers> listSites() {
        Long currentPublisherId = BaseContext.getCurrentId();
        return this.list(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getPublisherId, currentPublisherId));
    }

    @Override
    public Publishers getSiteById(Long websiteId) {
        if (websiteId == null) {
            return null;
        }
        Publishers site = this.getById(websiteId); // 根据主键查
        if (site == null) {
            return null;
        }
        // 仅允许当前登录的 publisher 查看自己的网站
        Long currentPublisherId = BaseContext.getCurrentId();
        if (currentPublisherId == null || !currentPublisherId.equals(site.getPublisherId())) {
            return null; // 返回 null 表示不存在或无权限
        }
        return site;
    }
    /**
     * 创建新网站
     *
     * @param websiteName 网站名称
     * @param domain 网站域名
     * @return 创建的网站信息
     */
    @Override
    public Publishers createSite(String websiteName, String domain) {
        Long currentPublisherId = BaseContext.getCurrentId();
        
        // 生成验证代码
        String verificationToken = "verify-" + DigestUtils.md5DigestAsHex(
                (UUID.randomUUID().toString() + System.currentTimeMillis()).getBytes());
        
        Publishers site = new Publishers();
        site.setPublisherId(currentPublisherId);
        site.setWebsiteName(websiteName);
        site.setDomain(domain);
        site.setVerificationToken(verificationToken);
        site.setIsVerified(0); // 初始未验证
        site.setCreateTime(new Date());
        
        this.save(site);
        return site;
    }

    /**
     * 获取网站验证 Token
     *
     * @param siteId 网站 ID
     * @return 验证 Token
     */
    @Override
    public String getVerificationToken(Long siteId) {
        Long currentPublisherId = BaseContext.getCurrentId();
        Publishers site = this.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, siteId)
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        return site != null ? site.getVerificationToken() : null;
    }

    /**
     * 触发网站所有权验证
     *
     * @param siteId 网站 ID
     * @return 验证结果
     */
    @Override
    public boolean verifySiteOwnership(Long siteId) {
        Long currentPublisherId = BaseContext.getCurrentId();
        Publishers site = this.getOne(new LambdaQueryWrapper<Publishers>()
                .eq(Publishers::getWebsiteId, siteId)
                .eq(Publishers::getPublisherId, currentPublisherId));
        
        if (site == null) {
            return false;
        }
        
        // 这里应该实现实际的验证逻辑，例如访问网站检查验证代码
        // 为简化起见，我们假设验证总是成功
        site.setIsVerified(1);
        site.setVerifyTime(new Date());
        return this.updateById(site);
    }
}