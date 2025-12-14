package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.service.PublishersService;
import com.usst.adfluxbackend.mapper.PublishersMapper;
import org.springframework.stereotype.Service;

/**
* @author 30637
* @description 针对表【publishers(网站站长信息表)】的数据库操作Service实现
* @createDate 2025-12-14 10:53:24
*/
@Service
public class PublishersServiceImpl extends ServiceImpl<PublishersMapper, Publishers>
    implements PublishersService{

}




