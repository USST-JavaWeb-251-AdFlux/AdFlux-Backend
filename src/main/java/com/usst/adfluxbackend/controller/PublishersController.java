package com.usst.adfluxbackend.controller;
import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.publisher.CreateSiteRequest;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.vo.PublisherSiteVO;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/publishers")
@RequireRole("publisher")
public class PublishersController {

    private static final Pattern VERIFICATION_META_PATTERN = Pattern.compile(
            "<meta[^>]*name=[\"']adflux-verification[\"'][^>]*content=[\"']([^\"']+)[\"'][^>]*>",
            Pattern.CASE_INSENSITIVE);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    
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
     * 触发网站所有权验证：
     * 1. 固定使用 HTTPS 访问站点首页并跟随重定向
     * 2. 解析 meta[name=adflux-verification] 的 content
     * 3. 返回该 content 与数据库内验证码的匹配结果
     *
     * @param siteId 网站 ID
     * @return 是否匹配验证码
     */
    @GetMapping("/sites/{siteId}/verification-token")
    public BaseResponse<Boolean> getVerificationToken(@PathVariable Long siteId) {
        Publishers site = publishersService.getSiteById(siteId);
        ThrowUtils.throwIf(site == null, ErrorCode.NOT_FOUND_ERROR, "站点不存在或无权限访问");

        String domain = site.getDomain();
        ThrowUtils.throwIf(!StringUtils.hasText(domain), ErrorCode.PARAMS_ERROR, "站点域名未配置");

        String normalizedDomain = domain.trim().replaceFirst("^(?i)https?://", "");
        String targetUrl = "https://" + normalizedDomain;
        if (!targetUrl.endsWith("/")) {
            targetUrl = targetUrl + "/";
        }

        String html;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            html = response.body();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "访问站点失败");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "访问站点失败");
        }

        String expectedToken = site.getVerificationToken();
        String metaContent = html == null ? null : VERIFICATION_META_PATTERN.matcher(html).results()
                .findFirst()
                .map(matchResult -> matchResult.group(1))
                .orElse(null);

        boolean matched = expectedToken != null && expectedToken.equals(metaContent);
        if (matched && (site.getIsVerified() == null || site.getIsVerified() == 0)) {
            site.setIsVerified(1);
            site.setVerifyTime(new Date());
            publishersService.updateById(site);
        }
        return ResultUtils.success(matched);
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
