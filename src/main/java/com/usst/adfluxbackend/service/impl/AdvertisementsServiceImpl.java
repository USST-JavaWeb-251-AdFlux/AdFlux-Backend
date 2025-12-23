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
import com.usst.adfluxbackend.model.vo.AdvertisementStatisticsVO;
import com.usst.adfluxbackend.model.vo.AdvertisementVO;
import com.usst.adfluxbackend.model.vo.DailyStatisticsVO;
import com.usst.adfluxbackend.model.vo.DataOverviewVO;
import com.usst.adfluxbackend.service.AdvertisementsService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
//    @Override
//    public AdvertisementVO createAdvertisement(CreateAdvertisementRequest createRequest) {
//        Long currentAdvertiserId = BaseContext.getCurrentId();
//
//        // 构造广告实体
//        Advertisements advertisement = new Advertisements();
//        BeanUtils.copyProperties(createRequest, advertisement);
//        advertisement.setAdvertiserId(currentAdvertiserId);
//        // 设置初始状态：待审核，未启用
//        advertisement.setReviewStatus(0);
//        advertisement.setIsActive(0);
//        // 设置创建和编辑时间
//        Date now = new Date();
//        advertisement.setCreateTime(now);
//        advertisement.setEditTime(now);
//
//        // 保存到数据库
//        this.save(advertisement);
//
//        // 转换为VO对象并返回
//        AdvertisementVO advertisementVO = new AdvertisementVO();
//        BeanUtils.copyProperties(advertisement, advertisementVO);
//        return advertisementVO;
//    }
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
        String adLayout = createRequest.getAdLayout();
        ThrowUtils.throwIf(adLayout == null ||
                        !(adLayout.equals("0") || adLayout.equals("1") || adLayout.equals("2")),
                ErrorCode.PARAMS_ERROR, "广告版式 adLayout 只允许 0-banner / 1-sidebar / 2-card");

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
        if (StringUtils.hasText(updateRequest.getAdLayout())) {
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
    public DataOverviewVO getDataOverview(DataOverviewQueryRequest queryRequest) {
        Long currentAdvertiserId = BaseContext.getCurrentId();
        
        // 构造查询条件
        LambdaQueryWrapper<AdDisplays> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdDisplays::getAdId, currentAdvertiserId);
        
        // 添加日期筛选条件
        if (StringUtils.hasText(queryRequest.getStartDate())) {
            queryWrapper.ge(AdDisplays::getDisplayTime, queryRequest.getStartDate());
        }
        if (StringUtils.hasText(queryRequest.getEndDate())) {
            queryWrapper.le(AdDisplays::getDisplayTime, queryRequest.getEndDate());
        }
        
        // 查询展示数据
        List<AdDisplays> adDisplaysList = adDisplaysMapper.selectList(queryWrapper);
        
        // 计算统计数据
        long totalImpressions = adDisplaysList.size();
        long totalClicks = adDisplaysList.stream()
                .mapToLong(display -> display.getClicked() != null ? display.getClicked() : 0)
                .sum();
        
        double ctr = totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0;
        
        // TODO: 计算总花费，这里暂时设为0
        double totalSpend = 0.0;
        
        // 构造返回对象
        DataOverviewVO dataOverviewVO = new DataOverviewVO();
        dataOverviewVO.setTotalImpressions(totalImpressions);
        dataOverviewVO.setTotalClicks(totalClicks);
        dataOverviewVO.setCtr(ctr);
        dataOverviewVO.setTotalSpend(totalSpend);
        
        return dataOverviewVO;
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

        // 验证广告是否属于当前广告主
        Advertisements advertisement = this.getOne(
                new LambdaQueryWrapper<Advertisements>()
                        .eq(Advertisements::getAdId, adId)
                        .eq(Advertisements::getAdvertiserId, currentAdvertiserId)
        );

        if (advertisement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "广告不存在或只能查看自己上传的广告");
        }

        // 构造查询条件
        LambdaQueryWrapper<AdDisplays> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AdDisplays::getAdId, adId);

        // 添加日期筛选条件
        if (StringUtils.hasText(queryRequest.getStartDate())) {
            queryWrapper.ge(AdDisplays::getDisplayTime, queryRequest.getStartDate());
        }
        if (StringUtils.hasText(queryRequest.getEndDate())) {
            queryWrapper.le(AdDisplays::getDisplayTime, queryRequest.getEndDate());
        }

        // 查询展示数据
        List<AdDisplays> adDisplaysList = adDisplaysMapper.selectList(queryWrapper);

        // 计算统计数据
        long totalImpressions = adDisplaysList.size();
        long totalClicks = adDisplaysList.stream()
                .mapToLong(display -> display.getClicked() != null ? display.getClicked() : 0)
                .sum();

        double ctr = totalImpressions > 0 ? (double) totalClicks / totalImpressions : 0.0;

        // 生成完整日期范围
        List<DailyStatisticsVO> dailyStats = new ArrayList<>();
        if (StringUtils.hasText(queryRequest.getStartDate()) && StringUtils.hasText(queryRequest.getEndDate())) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = dateFormat.parse(queryRequest.getStartDate());
                Date endDate = dateFormat.parse(queryRequest.getEndDate());

                // 按天分组统计
                Map<String, DailyStatisticsVO> dailyStatsMap = new LinkedHashMap<>();

                for (AdDisplays display : adDisplaysList) {
                    String dateStr = dateFormat.format(display.getDisplayTime());
                    DailyStatisticsVO dailyStat = dailyStatsMap.computeIfAbsent(dateStr, k -> {
                        DailyStatisticsVO stat = new DailyStatisticsVO();
                        stat.setDate(k);
                        stat.setImpressions(0L);
                        stat.setClicks(0L);
                        return stat;
                    });

                    // 增加展示数
                    dailyStat.setImpressions(dailyStat.getImpressions() + 1);

                    // 增加点击数
                    if (display.getClicked() != null && display.getClicked() == 1) {
                        dailyStat.setClicks(dailyStat.getClicks() + 1);
                    }
                }

                // 生成完整日期范围并填充数据
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                while (!calendar.getTime().after(endDate)) {
                    String dateStr = dateFormat.format(calendar.getTime());
                    DailyStatisticsVO dailyStat = dailyStatsMap.get(dateStr);
                    if (dailyStat == null) {
                        dailyStat = new DailyStatisticsVO();
                        dailyStat.setDate(dateStr);
                        dailyStat.setImpressions(0L);
                        dailyStat.setClicks(0L);
                    }
                    dailyStats.add(dailyStat);
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (ParseException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "日期格式错误");
            }
        } else {
            // 如果没有指定日期范围，按原逻辑处理
            Map<String, DailyStatisticsVO> dailyStatsMap = new LinkedHashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (AdDisplays display : adDisplaysList) {
                String dateStr = dateFormat.format(display.getDisplayTime());
                DailyStatisticsVO dailyStat = dailyStatsMap.computeIfAbsent(dateStr, k -> {
                    DailyStatisticsVO stat = new DailyStatisticsVO();
                    stat.setDate(k);
                    stat.setImpressions(0L);
                    stat.setClicks(0L);
                    return stat;
                });

                // 增加展示数
                dailyStat.setImpressions(dailyStat.getImpressions() + 1);

                // 增加点击数
                if (display.getClicked() != null && display.getClicked() == 1) {
                    dailyStat.setClicks(dailyStat.getClicks() + 1);
                }
            }
            dailyStats = new ArrayList<>(dailyStatsMap.values());
        }

        // 构造返回对象
        AdvertisementStatisticsVO statisticsVO = new AdvertisementStatisticsVO();
        statisticsVO.setAdId(adId);
        statisticsVO.setTotalImpressions(totalImpressions);
        statisticsVO.setTotalClicks(totalClicks);
        statisticsVO.setCtr(ctr);
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
        // 默认查询待审核广告
        if (status == null) {
            status = 0;
        }
        
        // 构造分页对象
        Page<Advertisements> pages = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        
        // 构造查询条件
        LambdaQueryWrapper<Advertisements> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Advertisements::getReviewStatus, status);
        
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
    public AdvertisementVO reviewAdvertisement(Long adId, Integer reviewStatus, String reason) {
        // 查询广告详情
        Advertisements advertisement = this.getById(adId);
        
        if (advertisement == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "广告不存在");
        }
        
        // 更新审核状态
        advertisement.setReviewStatus(reviewStatus);
        // 更新编辑时间
        advertisement.setEditTime(new Date());
        // 添加拒绝原因
        advertisement.setRejectReason(reason);
        
        // 保存到数据库
        this.updateById(advertisement);
        
        // 转换为VO对象并返回
        AdvertisementVO advertisementVO = new AdvertisementVO();
        BeanUtils.copyProperties(advertisement, advertisementVO);
        return advertisementVO;
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

}