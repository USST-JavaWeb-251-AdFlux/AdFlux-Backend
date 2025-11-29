package com.usst.adfluxbackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.constant.UserConstant;
import com.usst.adfluxbackend.context.BaseContext;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.TokenException;
import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.enums.UserRoleEnum;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.service.UsersService;
import com.usst.adfluxbackend.mapper.UsersMapper;
import com.usst.adfluxbackend.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.el.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
* @author 30637
* @description 针对表【users(用户)】的数据库操作Service实现
* @createDate 2025-11-27 20:34:47
*/
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    @Autowired
    private JwtUtils jwtUtils;


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
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
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
    public LoginUserVO userLogin(String username, String userPassword) {
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
        Map<String, Object> jwtClaims = new HashMap<>();
        jwtClaims.put("userId", user.getUserId());
        jwtClaims.put("username", username);
        jwtClaims.put("userRole", user.getUserRole());
        // 生成token
        String token = jwtUtils.generateToken(jwtClaims);
        // 5. 脱敏返回用户信息
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户
     */
    @Override
    public Users getLoginUser() {
        Long currentId = BaseContext.getCurrentId();
        if (currentId == null) {
            throw new TokenException(ErrorCode.TOKEN_ERROR, "Token过期或无效");
        }
        return baseMapper.selectById(currentId);
    }
}