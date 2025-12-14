package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.dto.advertiser.AdvertiserAddRequest;
import com.usst.adfluxbackend.model.entity.Advertisers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.vo.AdvertiserProfileVO;

/**
* @author 30637
* @description 针对表【advertisers(广告业主信息表)】的数据库操作Service
* @createDate 2025-12-14 10:49:43
*/
public interface AdvertisersService extends IService<Advertisers> {

    /**
     * 获取当前登录广告主的公司信息
     *
     * @return 广告主公司信息
     */
    AdvertiserProfileVO getAdvertiserProfile();

    /**
     * 添加广告主公司名称
     *
     * @param addRequest 添加请求
     * @return 是否添加成功
     */
    boolean addCompanyName(AdvertiserAddRequest addRequest);
}