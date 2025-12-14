package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.model.entity.ContentVisitStats;
import com.usst.adfluxbackend.service.ContentVisitStatsService;
import com.usst.adfluxbackend.mapper.ContentVisitStatsMapper;
import org.springframework.stereotype.Service;

/**
* @author 30637
* @description 针对表【content_visit_stats(访问内容行为统计)】的数据库操作Service实现
* @createDate 2025-12-14 10:58:37
*/
@Service
public class ContentVisitStatsServiceImpl extends ServiceImpl<ContentVisitStatsMapper, ContentVisitStats>
    implements ContentVisitStatsService{

}




