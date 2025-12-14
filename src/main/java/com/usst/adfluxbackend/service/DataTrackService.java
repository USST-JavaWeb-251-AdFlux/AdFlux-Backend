package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.mapper.AdDisplaysMapper;
import com.usst.adfluxbackend.mapper.ContentVisitStatsMapper;
import com.usst.adfluxbackend.model.entity.AdDisplays;
import com.usst.adfluxbackend.model.entity.ContentVisitStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataTrackService {

    @Autowired
    private AdDisplaysMapper adDisplaysMapper;

    @Autowired
    private ContentVisitStatsMapper contentVisitStatsMapper;

}