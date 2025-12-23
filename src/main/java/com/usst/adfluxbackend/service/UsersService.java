package com.usst.adfluxbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.model.vo.UsersVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 30637
* @description 针对表【users(用户)】的数据库操作Service
* @createDate 2025-11-27 20:34:47
*/
public interface UsersService extends IService<Users> {

    /**
     * 获取登录用户的VO对象
     *
     * @param loginUser
     * @return
     */
    LoginUserVO getLoginUserVO(Users loginUser);

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

     /**
      * 用户登录
      *
      * @param username
      * @param userPassword
      * @return
      */
    LoginUserVO userLogin(String username, String userPassword);

     /**
      * 获取当前登录用户
      *
      * @return
      */
    Users getLoginUser();
    
    /**
     * 管理员获取用户列表
     *
     * @param role 角色过滤
     * @return 用户列表
     */
    List<UsersVO> listUsers(String role);
    
    /**
     * 管理员创建管理员账号
     *
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param phone 电话
     * @return 创建的管理员用户
     */
    Users createAdmin(String username, String password, String email, String phone);
}