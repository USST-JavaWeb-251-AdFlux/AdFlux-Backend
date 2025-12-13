package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.vo.LoginUserVO;
import com.usst.adfluxbackend.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UsersService usersService;


    /**
     * 获取当前登录用户
     */
    @GetMapping("/me")
    public BaseResponse<LoginUserVO> getLoginUser() {
        Users loginUser = usersService.getLoginUser();
        return ResultUtils.success(usersService.getLoginUserVO(loginUser));
    }
}
