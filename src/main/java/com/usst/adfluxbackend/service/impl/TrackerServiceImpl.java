package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
        ThrowUtils.throwIf(publisher == null, ErrorCode.PARAMS_ERROR, "域名未找到对应的网站");
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
    public AdSlotVO selectAdForSlot(String trackId, String domain, Integer adType, String adLayout) {
        // 第一步：根据域名获取网站ID
        QueryWrapper<Publishers> pubWrapper = new QueryWrapper<>();
        pubWrapper.eq("domain", domain);
        Publishers publisher = publishersMapper.selectOne(pubWrapper);
        if(publisher == null){
            throw new BusinessException(1, "域名未找到对应的网站");
        }
        Long websiteId = publisher.getWebsiteId();

        // 从 advertisements 表中筛选符合条件的广告
        // todo 后续将adLayout修改过后需要修改
        QueryWrapper<Advertisements> adWrapper = new QueryWrapper<>();
        adWrapper.eq("adType", adType)
                .eq("adLayout", adLayout)
                .eq("reviewStatus", 1) // 已通过审核
                .eq("isActive", 1);    // 激活状态

        List<Advertisements> candidateAds = advertisementsMapper.selectList(adWrapper);
        
        if (candidateAds.isEmpty()) {
            throw new BusinessException(1, "没有符合条件的广告");
        }

        // 过滤超出周预算的广告
        List<Advertisements> filteredAds = new ArrayList<>();
        for (Advertisements ad : candidateAds) {
            if (!isWeeklyBudgetExceeded(ad.getAdId(), ad.getWeeklyBudget())) {
                filteredAds.add(ad);
            }
        }

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
            // 如果无法计算权重，则随机选择一个分类
            selectedCategoryId = filteredAds.get(0).getCategoryId();
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

        // 第五步：返回响应
        AdSlotVO adSlotVO = new AdSlotVO();
        adSlotVO.setDisplayId(displayId);
        adSlotVO.setMediaUrl(selectedAd.getMediaUrl());
        adSlotVO.setTitle(selectedAd.getTitle());
        adSlotVO.setLandingPage(selectedAd.getLandingPage());

        return adSlotVO;
    }

    /**
     * 检查广告的周预算是否已超支
     * 这里假设预算为金额，需要根据实际业务逻辑调整
     */
    private boolean isWeeklyBudgetExceeded(Long adId, BigDecimal weeklyBudget) {
        if (weeklyBudget == null || weeklyBudget.compareTo(BigDecimal.ZERO) <= 0) {
            return false; // 如果没有设置预算或预算为0或负数，则认为没有超支
        }

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
        BigDecimal  totalCost = BigDecimal.ZERO;
        for (AdDisplays display : displays) {
            if (display.getClicked() == 1) {
                totalCost = totalCost.add(BigDecimal.valueOf(2));
            } else {
                totalCost = totalCost.add(BigDecimal.valueOf(0.01));
            }
        }

        // 判断是否已超支 = 0也可投放，允许少量超出
        return totalCost.compareTo(weeklyBudget) > 0;
    }

    /**
     * 根据用户历史行为计算分类权重
     */
    private Map<Long, Double> calculateCategoryWeights(String trackId, Set<Long> categoryIds) {
        Map<Long, Double> weights = new HashMap<>();

        // 浏览历史加权：查询 content_visit_stats 表
        QueryWrapper<ContentVisitStats> visitStatsWrapper = new QueryWrapper<>();
        visitStatsWrapper.eq("trackId", trackId)
                .in("categoryId", categoryIds);
        List<ContentVisitStats> visitStatsList = contentVisitStatsMapper.selectList(visitStatsWrapper);

        // 计算各分类的访问时长权重
        Map<Long, Integer> categoryDurationMap = new HashMap<>();
        for (ContentVisitStats stat : visitStatsList) {
            Long categoryId = stat.getCategoryId();
            categoryDurationMap.merge(categoryId, stat.getDuration(), Integer::sum);
        }

        // 归一化权重
        int totalDuration = categoryDurationMap.values().stream().mapToInt(Integer::intValue).sum();
        if (totalDuration > 0) {
            for (Long categoryId : categoryIds) {
                int duration = categoryDurationMap.getOrDefault(categoryId, 0);
                weights.put(categoryId, (double) duration / totalDuration);
            }
        } else {
            // 如果没有历史数据，给所有分类相同的权重
            for (Long categoryId : categoryIds) {
                weights.put(categoryId, 1.0 / categoryIds.size());
            }
        }

        // 点击历史加权：查询 ad_displays 表
        QueryWrapper<AdDisplays> displayWrapper = new QueryWrapper<>();
        displayWrapper.eq("trackId", trackId);
        List<AdDisplays> displayList = adDisplaysMapper.selectList(displayWrapper);

        // 计算用户总点击率
        int totalClicks = (int) displayList.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();
        int totalDisplays = displayList.size();
        double avgClickRate = totalDisplays > 0 ? (double) totalClicks / totalDisplays : 0.0;

        // 根据用户实际点击率，给分类增加权重
        if (totalDisplays > 0) {
            // 按分类统计点击率 统计分类展示总数和点击总数
            Map<Long, Integer> categoryClicks = new HashMap<>();
            Map<Long, Double> categoryDisplays = new HashMap<>();

            for (AdDisplays display : displayList) {
                // 需要获取广告的分类ID
                Advertisements ad = advertisementsMapper.selectById(display.getAdId());
                if (ad != null && categoryIds.contains(ad.getCategoryId())) {
                    Long categoryId = ad.getCategoryId();

                    Instant instant = display.getDisplayTime().toInstant();
                    Timestamp timestamp = Timestamp.from(instant);
                    
                    // 计算时间权重值
                    double timeWeight = calculateTimeWeight(timestamp);
                    
                    // 使用时间权重更新展示统计
                    categoryDisplays.merge(categoryId, timeWeight, Double::sum);
                    if (display.getClicked() != null && display.getClicked() == 1) {
                        // 点击也应用时间权重
                        categoryClicks.merge(categoryId, (int) Math.round(timeWeight), Integer::sum);
                    }
                }
            }

            // 更新权重，结合点击率
            for (Long categoryId : categoryIds) {
                Double categoryDisplayCount = categoryDisplays.getOrDefault(categoryId, 0.0);
                if (categoryDisplayCount > 0) {
                    int categoryClickCount = categoryClicks.getOrDefault(categoryId, 0);
                    // 分类点击率：该类型点击总数 / 该类型展示总数
                    double categoryClickRate = (double) categoryClickCount / categoryDisplayCount;

                    // 计算权重差异
                    // todo 这些地方后期记录一下日志
                    // 该分类点击率 - 该用户点击率
                    double weightAdjustment = (categoryClickRate - avgClickRate) * 0.8; // 调整幅度 突出点击率的影响
                    double currentWeight = weights.get(categoryId);
                    weights.put(categoryId, Math.max(0, currentWeight + weightAdjustment));
                }
            }
        }

        // 标准化权重 归一化
        double weightSum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (weightSum > 0) {
            for (Map.Entry<Long, Double> entry : weights.entrySet()) {
                weights.put(entry.getKey(), entry.getValue() / weightSum);
            }
        }

        return weights;
    }

    /**
     * 使用加权随机算法选择一个分类
     */
    private Long selectCategoryByWeight(Map<Long, Double> weights) {
        if (weights.isEmpty()) {
            return null;
        }

        // 生成随机数
        double randomValue = Math.random();
        double cumulativeWeight = 0.0;
        
        for (Map.Entry<Long, Double> entry : weights.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue <= cumulativeWeight) {
                return entry.getKey();
            }
        }

        // 如果随机选择失败，返回第一个分类
        return weights.keySet().iterator().next();
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
}