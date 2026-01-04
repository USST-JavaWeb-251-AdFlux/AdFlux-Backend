package com.usst.adfluxbackend.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.usst.adfluxbackend.common.debug.AdDebugContext;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.mapper.*;
import com.usst.adfluxbackend.model.dto.tracker.AdSlotRequest;
import com.usst.adfluxbackend.model.entity.*;
import com.usst.adfluxbackend.model.vo.AdSlotVO;
import com.usst.adfluxbackend.service.TrackerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.usst.adfluxbackend.common.AdBillCalculator.calculateAdvertiserCost;
import static com.usst.adfluxbackend.constant.CostConstant.PRICE_PER_CLICK;
import static com.usst.adfluxbackend.constant.CostConstant.PRICE_PER_DISPLAY;
import static java.sql.Timestamp.valueOf;
import static java.time.Duration.between;

@Service
public class TrackerServiceImpl implements TrackerService {

    @Resource
    private ContentVisitStatsMapper contentVisitStatsMapper;

    @Resource
    private AdCategoriesMapper adCategoriesMapper;

    @Resource
    private PublishersMapper publishersMapper;

    @Resource
    private AdvertisementsMapper advertisementsMapper;

    @Resource
    private AdDisplaysMapper adDisplaysMapper;

    @Override
    public long trackPageView(String domain, String categoryName, String trackId) {
        // 根据域名查找网站ID
        QueryWrapper<Publishers> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("domain", domain);
        Publishers publisher = publishersMapper.selectOne(pubWrapper);
        // 域名不存在 -> 参数错误
        ThrowUtils.throwIf(publisher == null, ErrorCode.PARAMS_ERROR, "域名未找到对应的网站");

        // 新增：拒绝未验证的网站
        Integer isVerified = publisher.getIsVerified();
        ThrowUtils.throwIf(isVerified == null || isVerified.intValue() != 1,
                ErrorCode.FORBIDDEN_ERROR, "该网站未通过验证，拒绝访问");

        Long websiteId = publisher.getWebsiteId();

        // 根据类别名称查找类别ID
        QueryWrapper<AdCategories> catWrapper = new QueryWrapper<>();
        catWrapper.eq("categoryName", categoryName);
        AdCategories category = adCategoriesMapper.selectOne(catWrapper);
        ThrowUtils.throwIf(category == null, ErrorCode.PARAMS_ERROR, "广告类别未找到");
        Long categoryId = category.getCategoryId();

        // 构造访问记录实体并插入数据库
        ContentVisitStats stats = new ContentVisitStats();
        stats.setWebsiteId(websiteId);
        stats.setCategoryId(categoryId);
        stats.setTrackId(trackId);
        stats.setDuration(0); // 初始停留时长为0
        // 不设置 timestamp，数据库将使用默认 CURRENT_TIMESTAMP
        contentVisitStatsMapper.insert(stats);
        return stats.getVisitId();
    }


    @Override
    public boolean updateVisitDuration(Long visitId, Integer duration) {
        // 查询现有的访问记录
        ContentVisitStats stats = contentVisitStatsMapper.selectById(visitId);
        ThrowUtils.throwIf(stats == null, ErrorCode.PARAMS_ERROR, "访问记录不存在");
        Integer oldDuration = stats.getDuration();
        // 新的停留时长不能小于原来记录
        ThrowUtils.throwIf(duration < oldDuration, ErrorCode.PARAMS_ERROR, "新的停留时长不能比原来更短");

        // 更新停留时长
        UpdateWrapper<ContentVisitStats> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("visitId", visitId).set("duration", duration);
        int rows = contentVisitStatsMapper.update(null, updateWrapper);
        return rows > 0;
    }

    @Override
    public AdSlotVO selectAdForSlot(String trackId, String domain, Integer adType, Integer adLayout) {
        // 第一步：根据域名获取网站ID
        QueryWrapper<Publishers> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("domain", domain);
        Publishers publisher = publishersMapper.selectOne(pubWrapper);
        if(publisher == null){
            throw new BusinessException(1, "域名未找到对应的网站");
        }
        // 新增：拒绝未验证的网站
        Integer isVerified = publisher.getIsVerified();
        ThrowUtils.throwIf(isVerified == null || isVerified.intValue() != 1,
                ErrorCode.FORBIDDEN_ERROR, "该网站未通过验证，拒绝访问");

        Long websiteId = publisher.getWebsiteId();

        // 1. 计算30天前的 Date 对象
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();

        // 2. 构建查询条件
        LambdaQueryWrapper<Advertisements> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisements::getAdType, adType)
                .eq(Advertisements::getAdLayout, adLayout)
                .eq(Advertisements::getReviewStatus, 1) // 1-通过
                .eq(Advertisements::getIsActive, 1)     // 1-是
                // 筛选创建时间在最近30天内 (createTime >= 30天前)
                .ge(Advertisements::getCreateTime, thirtyDaysAgo)
                // 推荐：按时间倒序排列，最新的广告在前
                .orderByDesc(Advertisements::getCreateTime);

        // 3. 执行查询
        List<Advertisements> candidateAds = advertisementsMapper.selectList(wrapper);
        
        if (candidateAds.isEmpty()) {
            throw new BusinessException(1, "没有符合条件的广告");
        }

        // 过滤超出周预算的广告
        List<Advertisements> filteredAds = new ArrayList<>();
        List<Map<String, Object>> budgetCheckDetails = new ArrayList<>();
        for (Advertisements ad : candidateAds) {
            Map<String, Object> adCheckResult = new HashMap<>();
            // 调用检查方法，传入 map 进行填充
            boolean isPassed = !isWeeklyBudgetExceeded(ad.getAdId(), ad.getWeeklyBudget(), adCheckResult);

            if (isPassed) {
                filteredAds.add(ad);
            }
            // 统计前20条即可
            if(budgetCheckDetails.size() <= 20){
                budgetCheckDetails.add(adCheckResult);
            }
        }
        AdDebugContext.recordData("budgetFilterDetails", budgetCheckDetails);
        if (filteredAds.isEmpty()) {
            throw new BusinessException(1, "所有广告都超出预算");
        }

        // 获取候选广告的所有分类ID
        Set<Long> categoryIds = filteredAds.stream()
                .map(Advertisements::getCategoryId)
                .collect(Collectors.toSet());

        // 第二步：根据用户喜好为分类加权
        Map<Long, Double> categoryWeights = calculateCategoryWeights(trackId, categoryIds);

        // 第三步：使用加权随机算法选择一个分类
        Long selectedCategoryId = selectCategoryByWeight(categoryWeights);
        if (selectedCategoryId == null) {
            // 降级策略：随机选择
            selectedCategoryId = filteredAds.get(0).getCategoryId();
            // 记录降级事件
            Map<String, Object> fallbackEvent = new HashMap<>();
            fallbackEvent.put("reason", "Weight calculation failed");
            fallbackEvent.put("action", "Random fallback");
            fallbackEvent.put("selectedCategoryId", selectedCategoryId);
            AdDebugContext.recordData("selectionFallback", fallbackEvent);
        }

        Long finalSelectCategoryId = selectedCategoryId;

        // 在该分类下随机选择一个具体的广告（前面过滤出来的广告集合）
        List<Advertisements> adsInCategory = filteredAds.stream()
                .filter(ad -> ad.getCategoryId().equals(finalSelectCategoryId))
                .toList();
        if (adsInCategory.isEmpty()) {
            throw new BusinessException(1, "该分类下没有广告");
        }
        Random random = new Random();
        // 随机选择
        Advertisements selectedAd = adsInCategory.get(random.nextInt(adsInCategory.size()));
        // 第四步：记录展示数据
        AdDisplays adDisplay = new AdDisplays();
        adDisplay.setTrackId(trackId);
        adDisplay.setAdId(selectedAd.getAdId());
        adDisplay.setWebsiteId(websiteId);
        adDisplay.setDuration(0);
        adDisplay.setClicked(0);
        adDisplaysMapper.insert(adDisplay);
        Long displayId = adDisplay.getDisplayId();

        Map<String, Object> finalWinner = new HashMap<>();
        finalWinner.put("categoryId", finalSelectCategoryId);
        finalWinner.put("adId", selectedAd.getAdId());
        finalWinner.put("adTitle", selectedAd.getTitle());
        AdDebugContext.recordData("finalSelect", finalWinner);

        // 第五步：返回响应
        AdSlotVO adSlotVO = new AdSlotVO();
        adSlotVO.setDisplayId(displayId);
        adSlotVO.setAdLayout(adLayout);
        adSlotVO.setMediaUrl(selectedAd.getMediaUrl());
        adSlotVO.setTitle(selectedAd.getTitle());
        adSlotVO.setLandingPage(selectedAd.getLandingPage());

        return adSlotVO;
    }

    /**
     * 检查广告的周预算是否已超支
     * 这里假设预算为金额，需要根据实际业务逻辑调整
     */
    private boolean isWeeklyBudgetExceeded(Long adId, BigDecimal weeklyBudget, Map<String, Object> checkResultMap) {
        // 获取当前周的开始日期（周一）
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        ZonedDateTime weekStartDateTime = weekStart.atStartOfDay(ZoneId.systemDefault());

        // 查询本周的广告展示记录
        QueryWrapper<AdDisplays> displayWrapper = new QueryWrapper<>();
        displayWrapper.eq("adId", adId)
                .ge("displayTime", valueOf(weekStartDateTime.toLocalDateTime()));
        List<AdDisplays> displays = adDisplaysMapper.selectList(displayWrapper);

        // todo 有更好的方法再改
        // 遍历displays, 如果该记录的clicked字段为1，计费2元，否则 0.01
        BigDecimal  totalCost = calculateAdvertiserCost(displays);
        // 构建日志信息
        if (checkResultMap != null) {
            checkResultMap.put("adId", adId);
            checkResultMap.put("currentSpent", totalCost.setScale(2, RoundingMode.HALF_UP));
            checkResultMap.put("weeklyBudget", weeklyBudget != null ? weeklyBudget.setScale(2, RoundingMode.HALF_UP) : "Unlimited");
            checkResultMap.put("isPassed", weeklyBudget == null || totalCost.compareTo(weeklyBudget) <= 0);
        }
        if (weeklyBudget == null || weeklyBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        // 判断是否已超支 = 0也可投放，允许少量超出
        return totalCost.compareTo(weeklyBudget) > 0;
    }

    /**
     * 根据用户历史行为计算分类权重
     * 对应原来的 calculateCategoryWeights
     */
    private Map<Long, Double> calculateCategoryWeights(String trackId, Set<Long> categoryIds) {
        Map<Long, Double> weights = new HashMap<>();

        Map<String, Object> scoringDebugData = new HashMap<>();
        List<Map<String, Object>> categoryScoreDetails = new ArrayList<>();

        // --- 第一步：浏览历史加权 ---
        // 查询 content_visit_stats 表
        QueryWrapper<ContentVisitStats> visitStatsWrapper = new QueryWrapper<>();
        visitStatsWrapper.eq("trackId", trackId)
                .in("categoryId", categoryIds);
        List<ContentVisitStats> visitStatsList = contentVisitStatsMapper.selectList(visitStatsWrapper);

        // 统计各分类的访问时长
        Map<Long, Integer> categoryDurationMap = new HashMap<>();
        for (ContentVisitStats stat : visitStatsList) {
            categoryDurationMap.merge(stat.getCategoryId(), stat.getDuration(), Integer::sum);
        }
        // 记录原始浏览数据
        scoringDebugData.put("browseHistorySummary", categoryDurationMap);

        // 计算基础权重
        int totalDuration = categoryDurationMap.values().stream().mapToInt(Integer::intValue).sum();
        for (Long categoryId : categoryIds) {
            if (totalDuration > 0) {
                int duration = categoryDurationMap.getOrDefault(categoryId, 0);
                weights.put(categoryId, (double) duration / totalDuration);
            } else {
                // 如果没有历史数据，给所有分类相同的权重
                weights.put(categoryId, 1.0 / categoryIds.size());
            }
        }

        // --- 第二步：点击历史 & 时间衰减 (Click History & Time Decay) ---
        QueryWrapper<AdDisplays> displayWrapper = new QueryWrapper<>();
        displayWrapper.eq("trackId", trackId);
        List<AdDisplays> displayList = adDisplaysMapper.selectList(displayWrapper);

        // 计算用户总点击率 (Global CTR)
        long totalClicks = displayList.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();
        int totalDisplays = displayList.size();
        double avgClickRate = totalDisplays > 0 ? (double) totalClicks / totalDisplays : 0.0;

        // 记录点击率数据
        scoringDebugData.put("avgClickRate", avgClickRate);
        // 总推送数量
        scoringDebugData.put("totalDisplays", totalDisplays);
        // 用户总点击量
        scoringDebugData.put("totalClicks", totalClicks);

        // --- 第三步：基于点击率的权重修正 (Weight Adjustment) ---
        if (totalDisplays > 0) {
            Map<Long, Integer> categoryClicks = new HashMap<>();
            Map<Long, Double> categoryWeightedDisplays = new HashMap<>();

            // 遍历每一条展示记录，计算时间加权
            for (AdDisplays display : displayList) {
                // 这里为了获取分类ID查询了广告表 (性能优化点：实际生产中建议缓存或连表查询)
                Advertisements ad = advertisementsMapper.selectById(display.getAdId());

                if (ad != null && categoryIds.contains(ad.getCategoryId())) {
                    Long categoryId = ad.getCategoryId();

                    // 计算时间权重值
                    Timestamp displayTime = Timestamp.from(display.getDisplayTime().toInstant());
                    double timeWeight = calculateTimeWeight(displayTime);

                    // 累加“时间加权后”的展示量
                    categoryWeightedDisplays.merge(categoryId, timeWeight, Double::sum);

                    // 累加“时间加权后”的点击量
                    if (display.getClicked() != null && display.getClicked() == 1) {
                        categoryClicks.merge(categoryId, (int) Math.round(timeWeight), Integer::sum);
                    }
                }
            }

            // 更新权重，结合点击率
            for (Long categoryId : categoryIds) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("categoryId", categoryId);

                double currentBaseWeight = weights.getOrDefault(categoryId, 0.0);
                detail.put("baseWeightByContent", currentBaseWeight);

                Double weightedDisplayCount = categoryWeightedDisplays.getOrDefault(categoryId, 0.0);

                if (weightedDisplayCount > 0) {
                    int weightedClickCount = categoryClicks.getOrDefault(categoryId, 0);
                    // 分类点击率 = 加权点击 / 加权展示
                    double categoryClickRate = (double) weightedClickCount / weightedDisplayCount;

                    detail.put("categoryClickRate", categoryClickRate);

                    // 核心公式：计算权重差异
                    // (该分类点击率 - 用户平均点击率) * 调整系数
                    double adjustment = (categoryClickRate - avgClickRate) * 0.8;
                    detail.put("adjustmentRate", adjustment);

                    // 确保权重不为负
                    double finalWeight = Math.max(0, currentBaseWeight + adjustment);
                    weights.put(categoryId, finalWeight);
                } else {
                    detail.put("adjustment", 0.0);
                    detail.put("note", "该分类下无曝光记录");
                }
                categoryScoreDetails.add(detail);
            }
        } else {
            // 如果没有全局展示历史
            for (Long categoryId : categoryIds) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("categoryId", categoryId);
                detail.put("baseWeight", weights.get(categoryId));
                detail.put("note", "无全局展示历史");
                categoryScoreDetails.add(detail);
            }
        }

        // --- 第四步：归一化 (Normalization) ---
        double weightSum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (weightSum > 0) {
            for (Map.Entry<Long, Double> entry : weights.entrySet()) {
                double normalizedValue = entry.getValue() / weightSum;
                weights.put(entry.getKey(), normalizedValue);

                // 更新 debug 详情中的最终归一化权重
                categoryScoreDetails.stream()
                        .filter(d -> d.get("categoryId").equals(entry.getKey()))
                        .findFirst()
                        .ifPresent(d -> d.put("finalNormalizedWeight", normalizedValue));
            }
        }

        scoringDebugData.put("details", categoryScoreDetails);
        AdDebugContext.recordData("scoringProcess", scoringDebugData);

        return weights;
    }

    /**
     * 使用加权随机算法选择一个分类
     */
    private Long selectCategoryByWeight(Map<Long, Double> weights) {
        if (weights == null || weights.isEmpty()) {
            return null;
        }

        double randomValue = Math.random();

        // Debug 数据容器
        Map<String, Object> selectionDebug = new HashMap<>();
        selectionDebug.put("randomSeed", randomValue);
        List<Map<String, Object>> logicTrace = new ArrayList<>();

        double cumulativeWeight = 0.0;
        Long selectedId = null;

        for (Map.Entry<Long, Double> entry : weights.entrySet()) {
            double rangeStart = cumulativeWeight;
            // 累加权重
            cumulativeWeight += entry.getValue();
            double rangeEnd = cumulativeWeight;

            // 检查随机数是否落在此区间
            // 使用 flag 确保只选中第一个命中的分类
            boolean isHit = (randomValue <= rangeEnd) && (selectedId == null);

            if (isHit) {
                selectedId = entry.getKey();
            }

            // 记录步骤详情
            Map<String, Object> step = new HashMap<>();
            step.put("categoryId", entry.getKey());
            step.put("weight", entry.getValue());
            step.put("range", String.format("%.4f - %.4f", rangeStart, rangeEnd)); // 格式化区间字符串
            step.put("isHit", isHit);
            logicTrace.add(step);
        }

        // 兜底策略：如果因为浮点数精度问题没有选中任何分类，默认选第一个
        if (selectedId == null) {
            selectedId = weights.keySet().iterator().next();
            selectionDebug.put("fallbackTriggered", true);
            selectionDebug.put("note", "由于精度问题触发兜底，选择第一个分类");
        }

        selectionDebug.put("logicTrace", logicTrace);
        selectionDebug.put("finalSelectedId", selectedId);

        // 记录选择逻辑结构化数据
        AdDebugContext.recordData("selectionLogic", selectionDebug);

        return selectedId;
    }
    /**
     * 根据展示时间计算时间权重值
     * 时间越近，权重越大
     * @param displayTime 展示时间
     * @return 时间权重值
     */
    private double calculateTimeWeight(Timestamp displayTime) {
        if (displayTime == null) {
            return 1.0; // 如果没有时间，默认权重为1
        }
        
        // 获取当前时间
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime displayDateTime = displayTime.toLocalDateTime().atZone(ZoneId.systemDefault());
        
        // 计算时间差（小时）
        long hoursDiff = between(displayDateTime, now).toHours();
        
        // 定义衰减参数
        // 比如每24小时权重减半，使用指数衰减函数: weight = base * decay_factor^(hours/decay_period)
        double baseWeight = 1.0; // 基础权重
        double decayFactor = 0.5; // 衰减因子
        int decayPeriodHours = 24; // 衰减周期（小时）
        
        // 计算时间权重，越近的时间权重越大
        double timeWeight =baseWeight * Math.pow(decayFactor, (double) hoursDiff / decayPeriodHours);
        
        // 确保权重不小于最小值
        return Math.max(timeWeight, 0.1);
    }
    /**
     * 更新广告展示记录状态
     *
     * 业务规则说明：
     * 1. displayId、duration、clicked 均不能为空
     * 2. clicked 只能是 0 或 1
     * 3. 展示时长 duration 只能递增，不允许回退
     * 4. 点击状态一旦为 1，不允许取消
     * 5. 每次发生点击（clicked = 1）时，记录最后一次点击时间
     *
     * 该方法主要用于广告埋点上报，
     * 用于统计广告展示行为与点击行为。
     *
     * @param displayId 广告展示记录 ID
     * @param duration  当前累计展示时长
     * @param clicked   是否发生点击（0 / 1）
     * @return 是否更新成功
     */
    @Override
    public boolean updateAdDisplay(Long displayId, Integer duration, Integer clicked) {

        // 1. 基本参数校验
        ThrowUtils.throwIf(displayId == null || duration == null || clicked == null,
                ErrorCode.PARAMS_ERROR, "displayId/duration/clicked 不能为空");

        // clicked 只能是 0 或 1
        ThrowUtils.throwIf(clicked != 0 && clicked != 1,
                ErrorCode.PARAMS_ERROR, "clicked 只能是 0 或 1");

        // 2. 查询现有展示记录
        AdDisplays existing = adDisplaysMapper.selectById(displayId);
        ThrowUtils.throwIf(existing == null,
                ErrorCode.NOT_FOUND_ERROR, "展示记录不存在");

        Integer oldDuration = existing.getDuration() == null ? 0 : existing.getDuration();
        Integer oldClicked = existing.getClicked() == null ? 0 : existing.getClicked();

        // 3. duration 只增不减
        ThrowUtils.throwIf(duration < oldDuration,
                ErrorCode.PARAMS_ERROR, "新的停留时长不能比原来更短");

        // 4. 点击状态不可回退
        ThrowUtils.throwIf(oldClicked == 1 && clicked == 0,
                ErrorCode.PARAMS_ERROR, "点击状态不能被取消");

        // 5. 更新数据
        UpdateWrapper<AdDisplays> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("displayId", displayId)
                .set("duration", duration)
                .set("clicked", clicked);

        // 6. 记录最后一次点击时间
        if (clicked == 1) {
            updateWrapper.set("clickTime", new Timestamp(System.currentTimeMillis()));
        }

        return adDisplaysMapper.update(null, updateWrapper) > 0;
    }

}