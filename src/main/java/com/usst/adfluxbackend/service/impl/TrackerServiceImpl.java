package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.mapper.AdCategoriesMapper;
import com.usst.adfluxbackend.mapper.ContentVisitStatsMapper;
import com.usst.adfluxbackend.mapper.PublishersMapper;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.model.entity.ContentVisitStats;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.service.TrackerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 页面访问跟踪 Service 实现
 */
@Service
public class TrackerServiceImpl implements TrackerService {

    @Resource
    private ContentVisitStatsMapper contentVisitStatsMapper;

    @Resource
    private AdCategoriesMapper adCategoriesMapper;

    @Resource
    private PublishersMapper publishersMapper;

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
}
