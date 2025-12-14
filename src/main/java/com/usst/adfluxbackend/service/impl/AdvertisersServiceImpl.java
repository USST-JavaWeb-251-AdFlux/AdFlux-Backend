package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.mapper.AdvertisersMapper;
import com.usst.adfluxbackend.model.dto.advertiser.AdvertiserAddRequest;
import com.usst.adfluxbackend.model.entity.Advertisers;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.vo.AdvertiserProfileVO;
import com.usst.adfluxbackend.service.AdvertisersService;
import com.usst.adfluxbackend.service.UsersService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author 30637
* @description 针对表【advertisers(广告业主信息表)】的数据库操作Service实现
* @createDate 2025-12-14 10:49:43
*/
@Service
public class AdvertisersServiceImpl extends ServiceImpl<AdvertisersMapper, Advertisers>
    implements AdvertisersService{

    @Resource
    private UsersService usersService;

    /**
     * 获取当前登录广告主的公司信息
     *
     * @return 广告主公司信息
     */
    @Override
    public AdvertiserProfileVO getAdvertiserProfile() {
        // 从BaseContext获取当前登录用户ID
        Long currentUserId = BaseContext.getCurrentId();

        // 查询广告主信息
        Advertisers advertiser = this.getOne(
                this.lambdaQuery()
                        .eq(Advertisers::getAdvertiserId, currentUserId)
                        .getWrapper()
        );

        // 查询用户信息
        Users user = usersService.getById(currentUserId);

        // 组装VO对象
        AdvertiserProfileVO profileVO = new AdvertiserProfileVO();
        if (advertiser != null) {
            profileVO.setAdvertiserId(advertiser.getAdvertiserId());
            profileVO.setCompanyName(advertiser.getCompanyName());
        }
        
        if (user != null) {
            profileVO.setUserId(user.getUserId());
            profileVO.setEmail(user.getEmail());
            profileVO.setPhone(user.getPhone());
        }

        return profileVO;
    }

    /**
     * 添加广告主公司名称
     *
     * @param addRequest 添加请求
     * @return 是否添加成功
     */
    @Override
    public boolean addCompanyName(AdvertiserAddRequest addRequest) {
        // 从BaseContext获取当前登录用户ID
        Long currentUserId = BaseContext.getCurrentId();
        
        // 构造更新对象
        Advertisers advertiser = new Advertisers();
        advertiser.setAdvertiserId(currentUserId);
        advertiser.setCompanyName(addRequest.getCompanyName());
        
        // 添加数据库
        return this.save(advertiser);
    }
}