package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.service.UsersService;
import com.usst.adfluxbackend.mapper.UsersMapper;
import org.springframework.stereotype.Service;

/**
* @author 30637
* @description 针对表【users(用户)】的数据库操作Service实现
* @createDate 2025-11-27 20:34:47
*/
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

}




