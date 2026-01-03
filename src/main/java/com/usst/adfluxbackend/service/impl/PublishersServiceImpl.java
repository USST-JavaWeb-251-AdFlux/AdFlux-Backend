package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.mapper.PublishersMapper;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service implementation for Publishers (website owners).
 */
@Service
public class PublishersServiceImpl extends ServiceImpl<PublishersMapper, Publishers> implements PublishersService{

    private static final Pattern META_TAG_PATTERN = Pattern.compile("(?i)<meta[^>]*>");
    private static final Pattern META_NAME_PATTERN = Pattern.compile("(?i)\\bname=[\"']adflux-verification[\"']");
    private static final Pattern META_CONTENT_PATTERN = Pattern.compile("(?i)\\bcontent=[\"']([^\"']+)[\"']");
    private static final Pattern HEAD_END_PATTERN = Pattern.compile("(?i)</head>");

    private static final Duration VERIFY_TIMEOUT = Duration.ofSeconds(10);
    private static final int NOT_VERIFIED = 0;
    private static final int MAX_HTML_SCAN_LENGTH = 10000;

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(VERIFY_TIMEOUT)
            .build();

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
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "站点不存在或无权限访问");
        }
        
        String domain = site.getDomain();
        if (domain == null || domain.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "站点域名为空或无效");
        }

        URI targetUri;
        try {
            // 使用 https 且使用默认端口
            targetUri = new URI("https", null, domain.trim(), -1, "/", null, null);
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "站点域名无效");
        }

        String html;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(targetUri)
                    .timeout(VERIFY_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "访问站点失败，HTTP 状态码：" + response.statusCode());
            }
            html = response.body();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "访问站点失败：" + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "请求被中断");
        }

        String expectedToken = site.getVerificationToken();
        String metaContent = null;
        String scopedHtml = html;
        Matcher headMatcher = HEAD_END_PATTERN.matcher(html);
        if (headMatcher.find()) {
            scopedHtml = html.substring(0, headMatcher.start());
        } else if (html.length() > MAX_HTML_SCAN_LENGTH) {
            scopedHtml = html.substring(0, MAX_HTML_SCAN_LENGTH);
        }
        Matcher metaMatcher = META_TAG_PATTERN.matcher(scopedHtml);
        while (metaMatcher.find()) {
            String tag = metaMatcher.group();
            if (META_NAME_PATTERN.matcher(tag).find()) {
                Matcher contentMatcher = META_CONTENT_PATTERN.matcher(tag);
                if (contentMatcher.find()) {
                    metaContent = contentMatcher.group(1);
                    break;
                }
            }
        }

        boolean matched = expectedToken != null && expectedToken.equals(metaContent);
        if (!matched) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "站点未包含正确的验证标签");
        }

        Integer isVerified = site.getIsVerified();
        if (isVerified == null || Objects.equals(isVerified, NOT_VERIFIED)) {
            site.setIsVerified(1);
            site.setVerifyTime(new Date());
            this.updateById(site);
        }
        return true;
    }
}
