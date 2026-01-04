package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.mapper.AdDisplaysMapper;
import com.usst.adfluxbackend.mapper.AdvertisementsMapper;
import com.usst.adfluxbackend.model.dto.ad.AdvertisementQueryRequest;
import com.usst.adfluxbackend.model.dto.ad.CreateAdvertisementRequest;
import com.usst.adfluxbackend.model.dto.ad.UpdateAdvertisementRequest;
import com.usst.adfluxbackend.model.dto.ad.ToggleAdStatusRequest;
import com.usst.adfluxbackend.model.dto.statistic.DataOverviewQueryRequest;
import com.usst.adfluxbackend.model.entity.AdDisplays;
import com.usst.adfluxbackend.model.entity.Advertisements;
import com.usst.adfluxbackend.model.enums.AdLayoutEnum;
import com.usst.adfluxbackend.model.vo.*;
import com.usst.adfluxbackend.service.AdvertisementsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author 30637
* @description 针对表【advertisements(广告信息表)】的数据库操作Service实现
* @createDate 2025-12-14 10:41:14
*/
@Service
public class AdvertisementsServiceImpl extends ServiceImpl<AdvertisementsMapper, Advertisements>
    implements AdvertisementsService{

    @Resource
    private AdDisplaysMapper adDisplaysMapper;

    @Resource
    private AdvertisementsMapper advertisementsMapper;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 根据条件分页查询当前广告主的广告列表
     *
     * @param queryRequest 查询条件
     * @return 广告分页列表
     */
    @Override
    public IPage<AdvertisementVO> listAdvertisementsByPage(AdvertisementQueryRequest queryRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 构造分页对象
        Page<Advertisements> page = new Page<>(queryRequest.getPage(), queryRequest.getPageSize());
        
        // 构造查询条件
        QueryWrapper<Advertisements> queryWrapper =
                new QueryWrapper<>();
        queryWrapper.eq("advertiserId", currentAdvertiserId);
        
        // 添加可选的筛选条件
        if (queryRequest.getReviewStatus() != null) {
            queryWrapper.eq("reviewStatus", queryRequest.getReviewStatus());
        }
        if (queryRequest.getIsActive() != null) {
            queryWrapper.eq("isActive", queryRequest.getIsActive());
        }
        
        // 执行分页查询
        IPage<Advertisements> advertisementPage = this.page(page, queryWrapper);
        
        // 转换为VO对象
        Page<AdvertisementVO> voPage = new Page<>(advertisementPage.getCurrent(), advertisementPage.getSize(), advertisementPage.getTotal());
        voPage.setRecords(advertisementPage.getRecords().stream().map(advertisement -> {
            AdvertisementVO vo = new AdvertisementVO();
            BeanUtils.copyProperties(advertisement, vo);
            return vo;
        }).collect(Collectors.toList()));
        
        return voPage;
    }

    /**
     * 创建新的广告
     *
     * @param createRequest 创建广告请求
     * @return 创建的广告信息
     */
    @Override
    public AdvertisementVO createAdvertisement(CreateAdvertisementRequest createRequest) {

        // ---------- 基础参数校验 ----------
        ThrowUtils.throwIf(createRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");

        // title：非空
        String title = createRequest.getTitle();
        ThrowUtils.throwIf(title == null || title.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "广告标题 title 不能为空");
        title = title.trim();
        ThrowUtils.throwIf(title.length() > 255,
                ErrorCode.PARAMS_ERROR, "广告标题长度不能超过 255 字符");

        // weeklyBudget：非空、非负、>=200
        BigDecimal weeklyBudget = createRequest.getWeeklyBudget();
        ThrowUtils.throwIf(weeklyBudget == null,
                ErrorCode.PARAMS_ERROR, "周预算 weeklyBudget 不能为空");
        ThrowUtils.throwIf(weeklyBudget.compareTo(BigDecimal.ZERO) < 0,
                ErrorCode.PARAMS_ERROR, "周预算 weeklyBudget 不能为负数");
        ThrowUtils.throwIf(weeklyBudget.compareTo(new BigDecimal("200")) < 0,
                ErrorCode.PARAMS_ERROR, "周预算 weeklyBudget 最低为 200");

        // adLayout：只能是 0 / 1 / 2
        Integer adLayout = createRequest.getAdLayout();
        ThrowUtils.throwIf(adLayout == null ||
                        !(adLayout.equals(AdLayoutEnum.BANNER.getCode()) || adLayout.equals(AdLayoutEnum.SIDEBAR.getCode()) || adLayout.equals(AdLayoutEnum.VIDEO.getCode())),
                ErrorCode.PARAMS_ERROR, "广告版式 adLayout 只允许 0-video / 1-banner / 2-sidebar");

        // mediaUrl：非空
        String mediaUrl = createRequest.getMediaUrl();
        ThrowUtils.throwIf(mediaUrl == null || mediaUrl.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "广告素材 mediaUrl 不能为空");
        mediaUrl = mediaUrl.trim();
        ThrowUtils.throwIf(mediaUrl.length() > 512,
                ErrorCode.PARAMS_ERROR, "广告素材 mediaUrl 长度不能超过 512");

        // landingPage：可选，但如果存在必须是合法 URL
        String landingPage = createRequest.getLandingPage();
        if (landingPage != null && !landingPage.trim().isEmpty()) {
            landingPage = landingPage.trim();
            try {
                new java.net.URL(landingPage);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "landingPage 不是合法的 URL");
            }
            ThrowUtils.throwIf(landingPage.length() > 512,
                    ErrorCode.PARAMS_ERROR, "landingPage 长度不能超过 512");
        }

        // categoryId：可选，但必须 > 0
        Long categoryId = createRequest.getCategoryId();
        ThrowUtils.throwIf(categoryId != null && categoryId <= 0,
                ErrorCode.PARAMS_ERROR, "categoryId 必须为正数");

        // ---------- 构造并保存广告 ----------
        Long currentAdvertiserId = BaseContext.getCurrentId();
        ThrowUtils.throwIf(currentAdvertiserId == null,
                ErrorCode.NOT_LOGIN_ERROR, "未登录，无法创建广告");

        Advertisements advertisement = new Advertisements();
        BeanUtils.copyProperties(createRequest, advertisement);

        // 覆盖关键字段（确保使用校验后的值）
        fillAdvertisementForCreate(advertisement, createRequest, currentAdvertiserId);
        this.save(advertisement);

        AdvertisementVO advertisementVO = new AdvertisementVO();
        BeanUtils.copyProperties(advertisement, advertisementVO);
        return advertisementVO;
    }


    /**
     * 获取广告详情
     *
     * @param adId 广告ID
     * @return 广告详情
     */
    @Override
    public AdvertisementVO getAdvertisement(Long adId) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 查询广告详情
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        
        if (advertisement == null) {
            return null;
        }
        
        // 转换为VO对象
        AdvertisementVO advertisementVO = new AdvertisementVO();
        BeanUtils.copyProperties(advertisement, advertisementVO);
        return advertisementVO;
    }

    /**
     * 更新广告信息
     *
     * @param adId 广告ID
     * @param updateRequest 更新广告请求
     * @return 更新后的广告信息
     */
    @Override
    public AdvertisementVO updateAdvertisement(Long adId, UpdateAdvertisementRequest updateRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 查询广告详情
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        
        if (advertisement == null) {
            return null;
        }
        
        // 更新字段（仅更新非空字段）
        if (StringUtils.hasText(updateRequest.getTitle())) {
            advertisement.setTitle(updateRequest.getTitle());
        }
        if (StringUtils.hasText(updateRequest.getMediaUrl())) {
            advertisement.setMediaUrl(updateRequest.getMediaUrl());
        }
        if(updateRequest.getAdType() != null){
            advertisement.setAdType(updateRequest.getAdType());
        }
        if (StringUtils.hasText(updateRequest.getLandingPage())) {
            advertisement.setLandingPage(updateRequest.getLandingPage());
        }
        if (updateRequest.getCategoryId() != null) {
            advertisement.setCategoryId(updateRequest.getCategoryId());
        }
        if (updateRequest.getAdLayout() != null) {
            advertisement.setAdLayout(updateRequest.getAdLayout());
        }
        if (updateRequest.getWeeklyBudget() != null) {
            advertisement.setWeeklyBudget(updateRequest.getWeeklyBudget());
        }
        
        // 重新设置为待审核状态
        advertisement.setReviewStatus(0);
        // 更新编辑时间
        advertisement.setEditTime(new Date());
        
        // 保存到数据库
        this.updateById(advertisement);
        
        // 转换为VO对象并返回
        AdvertisementVO advertisementVO = new AdvertisementVO();
        BeanUtils.copyProperties(advertisement, advertisementVO);
        return advertisementVO;
    }

/**
     *物理删除广告
     *
     * @param adId 广告ID
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteAdvertisement(Long adId) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 查询广告详情
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        
        if (advertisement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "广告不存在或只能删除自己上传的广告");
        }
        
        //物理删除
        return this.removeById(advertisement.getAdId());
    }

    /**
     * 切换广告投放状态
     *
     * @param adId 广告ID
     * @param toggleRequest 切换状态请求
     * @return 更新后的广告信息
     */
    @Override
    public AdvertisementVO toggleAdStatus(Long adId, ToggleAdStatusRequest toggleRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 查询广告详情
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        
        if (advertisement == null) {
            return null;
        }
        
        // 只有审核通过的广告才能切换状态
        if (!Objects.equals(advertisement.getReviewStatus(), 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "广告未审核通过，无法切换状态");
        }
        
        // 更新状态
        advertisement.setIsActive(toggleRequest.getIsActive());
        // 更新编辑时间
        advertisement.setEditTime(new Date());
        
        // 保存到数据库
        this.updateById(advertisement);
        
        // 转换为VO对象并返回
        AdvertisementVO advertisementVO = new AdvertisementVO();
        BeanUtils.copyProperties(advertisement, advertisementVO);
        return advertisementVO;
    }

    /**
     * 获取广告主数据概览
     *
     * @param queryRequest 查询条件
     * @return 数据概览
     */
    @Override
    public DataOverviewStatisticsVO getDataOverview(DataOverviewQueryRequest queryRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();

        // 1. 获取当前广告主的所有广告 ID
        List<Advertisements> ads = advertisementsMapper.selectList(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        List<Long> adIds = (ads == null) ? Collections.emptyList()
                : ads.stream().map(Advertisements::getAdId).collect(Collectors.toList());

        // 如果无广告，返回空结果
        DataOverviewStatisticsVO empty = new DataOverviewStatisticsVO();
        empty.setTotalImpressions(0L);
        empty.setTotalClicks(0L);
        empty.setCtr(0.0);
        empty.setTotalSpend(0.0);
        empty.setDaily(Collections.emptyList());
        if (adIds.isEmpty()) {
            return empty;
        }

        // 2. 解析时间范围
        Date startDate = null;
        Date endDateExclusive = null;
        if (queryRequest != null) {
            try {
                if (StringUtils.hasText(queryRequest.getStartDate())) {
                    startDate = DATE_FMT.parse(queryRequest.getStartDate());
                }
                if (StringUtils.hasText(queryRequest.getEndDate())) {
                    Date parsedEnd = DATE_FMT.parse(queryRequest.getEndDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parsedEnd);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    endDateExclusive = cal.getTime();
                }
            } catch (ParseException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式错误，应为 yyyy-MM-dd");
            }
        }

        // 3. 查询 AdDisplays 记录
        LambdaQueryWrapper<AdDisplays> q = new LambdaQueryWrapper<>();
        q.in(AdDisplays::getAdId, adIds);
        if (startDate != null) {
            q.ge(AdDisplays::getDisplayTime, startDate);
        }
        if (endDateExclusive != null) {
            q.lt(AdDisplays::getDisplayTime, endDateExclusive);
        }
        List<AdDisplays> displays = adDisplaysMapper.selectList(q);
        if (displays == null) displays = Collections.emptyList();

        // 4. 汇总展示/点击/支出
        long totalImpressions = displays.size();
        long totalClicks = displays.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();

        BigDecimal totalSpendBD = BigDecimal.ZERO;
        Map<String, DailyStatisticsVO> dailyMap = new LinkedHashMap<>();
        for (AdDisplays d : displays) {
            String dateKey = DATE_FMT.format(d.getDisplayTime());
            DailyStatisticsVO ds = dailyMap.computeIfAbsent(dateKey, k -> {
                DailyStatisticsVO tmp = new DailyStatisticsVO();
                tmp.setDate(k);
                tmp.setImpressions(0L);
                tmp.setClicks(0L);
                tmp.setSpend(0.0);
                return tmp;
            });
            ds.setImpressions(ds.getImpressions() + 1);
            if (d.getClicked() != null && d.getClicked() == 1) {
                ds.setClicks(ds.getClicks() + 1);
                ds.setSpend(roundDouble(ds.getSpend() + 2.0));
                totalSpendBD = totalSpendBD.add(BigDecimal.valueOf(2.0));
            } else {
                ds.setSpend(roundDouble(ds.getSpend() + 0.01));
                totalSpendBD = totalSpendBD.add(BigDecimal.valueOf(0.01));
            }
        }

        // 5. 填充日期区间（如果提供）
        List<DailyStatisticsVO> dailyList;
        if (startDate != null && endDateExclusive != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDateExclusive);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Date endInclusive = cal.getTime();

            List<DailyStatisticsVO> temp = new ArrayList<>();
            cal.setTime(startDate);
            while (!cal.getTime().after(endInclusive)) {
                String key = DATE_FMT.format(cal.getTime());
                DailyStatisticsVO ds = dailyMap.getOrDefault(key, new DailyStatisticsVO());
                if (ds.getDate() == null) ds.setDate(key);
                if (ds.getImpressions() == null) ds.setImpressions(0L);
                if (ds.getClicks() == null) ds.setClicks(0L);
                if (ds.getSpend() == null) ds.setSpend(0.0);
                temp.add(ds);
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            dailyList = temp;
        } else {
            dailyList = new ArrayList<>(dailyMap.values());
        }

        // 6. 构建返回对象
        DataOverviewStatisticsVO dataOverviewStatisticsVO = new DataOverviewStatisticsVO();
        dataOverviewStatisticsVO.setCtr(totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0);
        dataOverviewStatisticsVO.setTotalClicks(totalClicks);
        dataOverviewStatisticsVO.setTotalImpressions(totalImpressions);
        dataOverviewStatisticsVO.setTotalSpend(totalSpendBD.setScale(2, RoundingMode.HALF_UP).doubleValue());
        dataOverviewStatisticsVO.setDaily(dailyList);
        return dataOverviewStatisticsVO;
    }


    /**
     * 获取广告统计数据
     *
     * @param adId 广告ID
     * @param queryRequest 查询条件
     * @return 广告统计数据
     */
    @Override
    public AdvertisementStatisticsVO getAdvertisementStatistics(Long adId, DataOverviewQueryRequest queryRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();

        // 验证广告归属
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );
        if (advertisement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "广告不存在或只能查看自己上传的广告");
        }

        // 1. 解析时间范围
        Date startDate = null;
        Date endDateExclusive = null;
        if (queryRequest != null) {
            try {
                if (StringUtils.hasText(queryRequest.getStartDate())) {
                    startDate = DATE_FMT.parse(queryRequest.getStartDate());
                }
                if (StringUtils.hasText(queryRequest.getEndDate())) {
                    Date parsedEnd = DATE_FMT.parse(queryRequest.getEndDate());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parsedEnd);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    endDateExclusive = cal.getTime();
                }
            } catch (ParseException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式错误，应为 yyyy-MM-dd");
            }
        }

        // 2. 查询该广告的展示记录
        LambdaQueryWrapper<AdDisplays> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdDisplays::getAdId, adId);
        if (startDate != null) {
            queryWrapper.ge(AdDisplays::getDisplayTime, startDate);
        }
        if (endDateExclusive != null) {
            queryWrapper.lt(AdDisplays::getDisplayTime, endDateExclusive);
        }

        List<AdDisplays> adDisplaysList = adDisplaysMapper.selectList(queryWrapper);
        if (adDisplaysList == null) adDisplaysList = Collections.emptyList();

        long totalImpressions = adDisplaysList.size();
        long totalClicks = adDisplaysList.stream().filter(d -> d.getClicked() != null && d.getClicked() == 1).count();
        BigDecimal totalSpendBD = BigDecimal.ZERO;

        // 3. 生成每日统计
        Map<String, DailyStatisticsVO> dailyStatsMap = new LinkedHashMap<>();
        for (AdDisplays display : adDisplaysList) {
            String dateStr = DATE_FMT.format(display.getDisplayTime());
            DailyStatisticsVO stat = dailyStatsMap.computeIfAbsent(dateStr, k -> {
                DailyStatisticsVO tmp = new DailyStatisticsVO();
                tmp.setDate(k);
                tmp.setImpressions(0L);
                tmp.setClicks(0L);
                tmp.setSpend(0.0);
                return tmp;
            });
            stat.setImpressions(stat.getImpressions() + 1);
            if (display.getClicked() != null && display.getClicked() == 1) {
                stat.setClicks(stat.getClicks() + 1);
                stat.setSpend(roundDouble(stat.getSpend() + 2.0));
                totalSpendBD = totalSpendBD.add(BigDecimal.valueOf(2.0));
            } else {
                stat.setSpend(roundDouble(stat.getSpend() + 0.01));
                totalSpendBD = totalSpendBD.add(BigDecimal.valueOf(0.01));
            }
        }

        // 4. 填充日期区间
        List<DailyStatisticsVO> dailyStats;
        if (startDate != null && endDateExclusive != null) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDateExclusive);
                cal.add(Calendar.DAY_OF_MONTH, -1);
                Date endInclusive = cal.getTime();

                List<DailyStatisticsVO> temp = new ArrayList<>();
                cal.setTime(startDate);
                while (!cal.getTime().after(endInclusive)) {
                    String key = DATE_FMT.format(cal.getTime());
                    DailyStatisticsVO ds = dailyStatsMap.getOrDefault(key, new DailyStatisticsVO());
                    if (ds.getDate() == null) ds.setDate(key);
                    if (ds.getImpressions() == null) ds.setImpressions(0L);
                    if (ds.getClicks() == null) ds.setClicks(0L);
                    if (ds.getSpend() == null) ds.setSpend(0.0);
                    temp.add(ds);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                dailyStats = temp;
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成日期序列失败");
            }
        } else {
            dailyStats = new ArrayList<>(dailyStatsMap.values());
        }

        AdvertisementStatisticsVO statisticsVO = new AdvertisementStatisticsVO();
        statisticsVO.setAdId(adId);
        statisticsVO.setTotalImpressions(totalImpressions);
        statisticsVO.setTotalClicks(totalClicks);
        statisticsVO.setCtr(totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0);
        statisticsVO.setTotalSpend(totalSpendBD.setScale(2, RoundingMode.HALF_UP).doubleValue());
        statisticsVO.setDaily(dailyStats);
        return statisticsVO;
    }


    /**
     * 管理员分页查询广告列表（可按审核状态筛选）
     *
     * @param status 审核状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 广告分页列表
     */
    @Override
    public IPage<AdvertisementVO> listAdsForAdmin(Integer status, Integer page, Integer pageSize) {
        // 构造分页对象
        Page<Advertisements> pages = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        
        // 构造查询条件
        LambdaQueryWrapper<Advertisements> queryWrapper = new LambdaQueryWrapper<>();
        if(status == null){
            // 全查时，按 status 升序排列 (0 -> 1 -> 2)
            queryWrapper.orderByAsc(Advertisements::getReviewStatus);
            queryWrapper.orderByDesc(Advertisements::getCreateTime);
        } else {
            queryWrapper.eq(Advertisements::getReviewStatus, status);
        }

        // 执行分页查询
        IPage<Advertisements> advertisementPage = this.page(pages, queryWrapper);
        
        // 转换为VO对象
        Page<AdvertisementVO> voPage = new Page<>(advertisementPage.getCurrent(), advertisementPage.getSize(), advertisementPage.getTotal());
        voPage.setRecords(advertisementPage.getRecords().stream().map(advertisement -> {
            AdvertisementVO vo = new AdvertisementVO();
            BeanUtils.copyProperties(advertisement, vo);
            return vo;
        }).collect(Collectors.toList()));
        
        return voPage;
    }
    
    /**
     * 管理员审核广告
     *
     * @param adId 广告ID
     * @param reviewStatus 审核状态：0-待审核；1-通过；2-拒绝
     * @param reason 拒绝原因
     * @return 更新后的广告信息
     */

    @Override
    public AdvertisementReviewVO reviewAdvertisement(Long adId, Integer reviewStatus, String reason) {

        Advertisements advertisement = this.getById(adId);
        if (advertisement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "广告不存在");
        }

        advertisement.setReviewStatus(reviewStatus);
        advertisement.setEditTime(new Date());

        if (reviewStatus == 2) {
            advertisement.setRejectReason(reason);
        } else {
            advertisement.setRejectReason(null);
        }

        this.updateById(advertisement);

        AdvertisementReviewVO vo = new AdvertisementReviewVO();
        vo.setAdId(advertisement.getAdId());
        vo.setReviewStatus(advertisement.getReviewStatus());
        vo.setRejectReason(advertisement.getRejectReason());
        vo.setEditTime(advertisement.getEditTime());

        return vo;
    }

    private void fillAdvertisementForCreate(
            Advertisements advertisement,
            CreateAdvertisementRequest request,
            Long advertiserId
    ) {
        advertisement.setTitle(request.getTitle());
        advertisement.setWeeklyBudget(request.getWeeklyBudget());
        advertisement.setAdLayout(request.getAdLayout());
        advertisement.setMediaUrl(request.getMediaUrl());
        advertisement.setLandingPage(request.getLandingPage());
        advertisement.setCategoryId(request.getCategoryId());

        advertisement.setAdvertiserId(advertiserId);
        advertisement.setReviewStatus(0); // 待审核
        advertisement.setIsActive(0);     // 默认不启用

        Date now = new Date();
        advertisement.setCreateTime(now);
        advertisement.setEditTime(now);
    }
    // 辅助方法：对 double 临时值做小数位保留（用于每日累加）
    private static double roundDouble(Double v) {
        if (v == null) return 0.0;
        return new java.math.BigDecimal(v)
                .setScale(6, java.math.RoundingMode.HALF_UP)
                .doubleValue();
    }
}