package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.user.UserLoginRequest;
import com.usst.adfluxbackend.model.dto.user.UserRegisterRequest;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@RequireRole(disabled = true)
public class AuthController {
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
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String username = userLoginRequest.getUsername();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = usersService.userLogin(username, userPassword);
        return ResultUtils.success(loginUserVO);
    }
}
