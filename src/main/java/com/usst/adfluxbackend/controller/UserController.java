package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.user.UserLoginRequest;
import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UsersService usersService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        long result = usersService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String username = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = usersService.userLogin(username, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        Users loginUser = usersService.getLoginUser(request);
        return ResultUtils.success(usersService.getLoginUserVO(loginUser));
    }
}
