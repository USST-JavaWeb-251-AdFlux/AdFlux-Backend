package com.usst.adfluxbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.constant.UserConstant;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.enums.UserRoleEnum;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.service.UsersService;
import com.usst.adfluxbackend.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;

/**
* @author 30637
* @description 针对表【users(用户)】的数据库操作Service实现
* @createDate 2025-11-27 20:34:47
*/
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    @Override
    public Users getLoginUser(HttpServletRequest request) {
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        Users currentUser = (Users) userObj;
        if (currentUser == null || currentUser.getUserId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询（追求性能的话可以注释，直接返回上述结果）
        Long userId = currentUser.getUserId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏类的用户信息
     *
     * @param user 用户
     * @return 脱敏后的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(Users user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

     /**
      * 用户注册
      *
      * @param userRegisterRequest 用户注册请求
      * @return 新用户id
      */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        // 1. 校验参数
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        String username = userRegisterRequest.getUsername();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StrUtil.hasBlank(username, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 2. 检查用户账号是否和数据库中已有的重复
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 3. 密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据到数据库中
        Users user = new Users();
        user.setUsername(username);
        user.setUserPassword(encryptPassword);
        user.setUserRole(UserRoleEnum.ADMIN.getValue());
        user.setPhone(userRegisterRequest.getPhone());
        user.setEmail(userRegisterRequest.getEmail());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getUserId();
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 加盐，混淆密码
        final String SALT = "usst";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    @Override
    public LoginUserVO userLogin(String username, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(username, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码错误");
        }
        // 2. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询数据库中的用户是否存在
        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("userPassword", encryptPassword);
        Users user = this.baseMapper.selectOne(queryWrapper);
        // 不存在，抛异常
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        // 4. 保存用户的登录态 用于判断用户是否登录
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }
}




