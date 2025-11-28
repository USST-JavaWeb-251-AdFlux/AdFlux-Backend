package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.entity.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.usst.adfluxbackend.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 30637
* @description 针对表【users(用户)】的数据库操作Service
* @createDate 2025-11-27 20:34:47
*/
public interface UsersService extends IService<Users> {

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    Users getLoginUser(HttpServletRequest request);

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
     * 获取加密后的密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

     /**
      * 用户登录
      *
      * @param username
      * @param userPassword
      * @param request
      * @return
      */
    LoginUserVO userLogin(String username, String userPassword, HttpServletRequest request);
}
