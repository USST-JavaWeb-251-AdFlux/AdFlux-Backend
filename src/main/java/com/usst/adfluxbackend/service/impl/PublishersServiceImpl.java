package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.constant.CostConstant;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.mapper.AdDisplaysMapper;
import com.usst.adfluxbackend.mapper.PublishersMapper;
import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.AdDisplays;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.vo.DailyRevenueVO;
import com.usst.adfluxbackend.model.vo.PublisherRevenueStatisticsVO;
import com.usst.adfluxbackend.service.PublishersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * Service implementation for Publishers (website owners).
 */
@Service
public class PublishersServiceImpl extends ServiceImpl<PublishersMapper, Publishers> implements PublishersService {

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
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    @Autowired
    private PublishersMapper publishersMapper;

    @Autowired
    private AdDisplaysMapper adDisplaysMapper;

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
     * @param domain      网站域名
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
     * 获取当前发布主所有网站的收益统计（按时间范围）
     */
    @Override
    public PublisherRevenueStatisticsVO getPublisherStatistics(DataOverviewQueryRequest queryRequest) {
        Long currentPublisherId = BaseContext.getCurrentId();

        // 1. 获取当前发布主名下所有网站 ID（避免 null）
        List<Publishers> sites = publishersMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Publishers>()
                        .eq(Publishers::getPublisherId, currentPublisherId)
        );
        List<Long> siteIds = (sites == null) ? Collections.emptyList()
                : sites.stream()
                .map(Publishers::getWebsiteId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        PublisherRevenueStatisticsVO empty = new PublisherRevenueStatisticsVO();
        empty.setTotalImpressions(0L);
        empty.setTotalClicks(0L);
        empty.setTotalRevenue(0.0);
        empty.setDaily(Collections.emptyList());
        if (siteIds.isEmpty()) return empty;

        // 2. 解析时间范围（前端格式 yyyy-MM-dd），转为 java.util.Date 边界：[startDate, endDateExclusive)
        Date startDate = null;
        Date endDateExclusive = null;
        try {
            if (queryRequest != null && StringUtils.hasText(queryRequest.getStartDate())) {
                LocalDate startLocal = LocalDate.parse(queryRequest.getStartDate(), DATE_FMT);
                startDate = Date.from(startLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (queryRequest != null && StringUtils.hasText(queryRequest.getEndDate())) {
                LocalDate endLocal = LocalDate.parse(queryRequest.getEndDate(), DATE_FMT);
                LocalDate endExclusiveLocal = endLocal.plusDays(1);
                endDateExclusive = Date.from(endExclusiveLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式错误，应为 yyyy-MM-dd");
        }

        // 3. 查询符合条件的 AdDisplays
        LambdaQueryWrapper<AdDisplays> q = new LambdaQueryWrapper<>();
        q.in(AdDisplays::getWebsiteId, siteIds);
        if (startDate != null) q.ge(AdDisplays::getDisplayTime, startDate);
        if (endDateExclusive != null) q.lt(AdDisplays::getDisplayTime, endDateExclusive);

        List<AdDisplays> displays = adDisplaysMapper.selectList(q);
        if (displays == null) displays = Collections.emptyList();

        long totalImpressions = displays.size();
        long totalClicks = displays.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();

        // 4. 计算发布主收益（使用常量，不用魔法数字）
        // publisherShare = advertiserCost * (1 - PLATFORM_COMMISSION_RATE)
        BigDecimal totalRevenueBD = BigDecimal.ZERO;
        Map<String, DailyRevenueVO> dailyMap = new LinkedHashMap<>();

        for (AdDisplays d : displays) {
            // 使用 displayTime 的 LocalDate 作为 key（yyyy-MM-dd）
            LocalDate ld = d.getDisplayTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String key = ld.format(DATE_FMT);

            DailyRevenueVO dv = dailyMap.computeIfAbsent(key, k -> {
                DailyRevenueVO tmp = new DailyRevenueVO();
                tmp.setDate(k);
                tmp.setImpressions(0L);
                tmp.setClicks(0L);
                tmp.setRevenue(0.0);
                return tmp;
            });

            dv.setImpressions(dv.getImpressions() + 1);

            BigDecimal advertiserCost = (d.getClicked() != null && d.getClicked() == 1)
                    ? CostConstant.PRICE_PER_CLICK
                    : CostConstant.PRICE_PER_DISPLAY;

            BigDecimal publisherShare = advertiserCost.multiply(
                    BigDecimal.ONE.subtract(CostConstant.PLATFORM_COMMISSION_RATE)
            );

            // 累加
            totalRevenueBD = totalRevenueBD.add(publisherShare);
            dv.setRevenue(roundDouble(dv.getRevenue() + publisherShare.setScale(6, RoundingMode.HALF_UP).doubleValue()));

            if (d.getClicked() != null && d.getClicked() == 1) {
                dv.setClicks(dv.getClicks() + 1);
            }
        }

        // 5. 填充连续日期（如果同时提供 startDate/endDate），确保每天都有数据（便于前端画图）
        List<DailyRevenueVO> dailyList;
        if (startDate != null && endDateExclusive != null) {
            LocalDate startLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endInclusiveLocal = endDateExclusive.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);

            List<DailyRevenueVO> tmpList = new ArrayList<>();
            LocalDate cur = startLocal;
            while (!cur.isAfter(endInclusiveLocal)) {
                String key = cur.format(DATE_FMT);
                DailyRevenueVO dv = dailyMap.getOrDefault(key, new DailyRevenueVO());
                if (dv.getDate() == null) dv.setDate(key);
                if (dv.getImpressions() == null) dv.setImpressions(0L);
                if (dv.getClicks() == null) dv.setClicks(0L);
                if (dv.getRevenue() == null) dv.setRevenue(0.0);
                tmpList.add(dv);
                cur = cur.plusDays(1);
            }
            dailyList = tmpList;
        } else {
            dailyList = new ArrayList<>(dailyMap.values());
        }

        PublisherRevenueStatisticsVO result = new PublisherRevenueStatisticsVO();
        result.setTotalImpressions(totalImpressions);
        result.setTotalClicks(totalClicks);
        result.setTotalRevenue(totalRevenueBD.setScale(6, RoundingMode.HALF_UP).doubleValue());
        result.setDaily(dailyList);
        return result;
    }

    /**
     * 获取单个网站的收益统计（按时间范围）
     */
    @Override
    public PublisherRevenueStatisticsVO getPublisherSiteStatistics(Long websiteId, DataOverviewQueryRequest queryRequest) {
        Long currentPublisherId = BaseContext.getCurrentId();
        Publishers site = publishersMapper.selectById(websiteId);
        if (site == null || !Objects.equals(site.getPublisherId(), currentPublisherId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "网站不存在或无权限访问");
        }

        // 1. 解析时间范围（同上）
        Date startDate = null;
        Date endDateExclusive = null;
        try {
            if (queryRequest != null && StringUtils.hasText(queryRequest.getStartDate())) {
                LocalDate startLocal = LocalDate.parse(queryRequest.getStartDate(), DATE_FMT);
                startDate = Date.from(startLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            if (queryRequest != null && StringUtils.hasText(queryRequest.getEndDate())) {
                LocalDate endLocal = LocalDate.parse(queryRequest.getEndDate(), DATE_FMT);
                LocalDate endExclusiveLocal = endLocal.plusDays(1);
                endDateExclusive = Date.from(endExclusiveLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式错误，应为 yyyy-MM-dd");
        }

        LambdaQueryWrapper<AdDisplays> q = new LambdaQueryWrapper<>();
        q.eq(AdDisplays::getWebsiteId, websiteId);
        if (startDate != null) q.ge(AdDisplays::getDisplayTime, startDate);
        if (endDateExclusive != null) q.lt(AdDisplays::getDisplayTime, endDateExclusive);

        List<AdDisplays> displays = adDisplaysMapper.selectList(q);
        if (displays == null) displays = Collections.emptyList();

        long totalImpressions = displays.size();
        long totalClicks = displays.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();

        BigDecimal totalRevenueBD = BigDecimal.ZERO;
        Map<String, DailyRevenueVO> dailyMap = new LinkedHashMap<>();
        for (AdDisplays d : displays) {
            LocalDate ld = d.getDisplayTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            String key = ld.format(DATE_FMT);

            DailyRevenueVO dv = dailyMap.computeIfAbsent(key, k -> {
                DailyRevenueVO tmp = new DailyRevenueVO();
                tmp.setDate(k);
                tmp.setImpressions(0L);
                tmp.setClicks(0L);
                tmp.setRevenue(0.0);
                return tmp;
            });

            dv.setImpressions(dv.getImpressions() + 1);

            BigDecimal advertiserCost = (d.getClicked() != null && d.getClicked() == 1)
                    ? CostConstant.PRICE_PER_CLICK
                    : CostConstant.PRICE_PER_DISPLAY;

            BigDecimal publisherShare = advertiserCost.multiply(
                    BigDecimal.ONE.subtract(CostConstant.PLATFORM_COMMISSION_RATE)
            );

            totalRevenueBD = totalRevenueBD.add(publisherShare);
            dv.setRevenue(roundDouble(dv.getRevenue() + publisherShare.setScale(6, RoundingMode.HALF_UP).doubleValue()));

            if (d.getClicked() != null && d.getClicked() == 1) {
                dv.setClicks(dv.getClicks() + 1);
            }
        }

        List<DailyRevenueVO> dailyList;
        if (startDate != null && endDateExclusive != null) {
            LocalDate startLocal = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate endInclusiveLocal = endDateExclusive.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().minusDays(1);

            List<DailyRevenueVO> tmp = new ArrayList<>();
            LocalDate cur = startLocal;
            while (!cur.isAfter(endInclusiveLocal)) {
                String key = cur.format(DATE_FMT);
                DailyRevenueVO dv = dailyMap.getOrDefault(key, new DailyRevenueVO());
                if (dv.getDate() == null) dv.setDate(key);
                if (dv.getImpressions() == null) dv.setImpressions(0L);
                if (dv.getClicks() == null) dv.setClicks(0L);
                if (dv.getRevenue() == null) dv.setRevenue(0.0);
                tmp.add(dv);
                cur = cur.plusDays(1);
            }
            dailyList = tmp;
        } else {
            dailyList = new ArrayList<>(dailyMap.values());
        }

        PublisherRevenueStatisticsVO result = new PublisherRevenueStatisticsVO();
        result.setTotalImpressions(totalImpressions);
        result.setTotalClicks(totalClicks);
        result.setTotalRevenue(totalRevenueBD.setScale(6, RoundingMode.HALF_UP).doubleValue());
        result.setDaily(dailyList);
        return result;
    }



    // 辅助方法：保留小数点（用于逐日累计计算）
    private static double roundDouble(Double v) {
        if (v == null) return 0.0;
        return new BigDecimal(v).setScale(6, RoundingMode.HALF_UP).doubleValue();
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
