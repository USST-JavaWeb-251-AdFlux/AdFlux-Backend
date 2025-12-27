package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
@RequireRole
public class AppController {

    @Value("${app.version:[Unknown]}")
    private String appVersion;

    /**
     * Returns the current application version.
     *
     * @return a {@link BaseResponse} containing the application version string
     */
    @GetMapping("/version")
    public BaseResponse<String> getAppVersion() {
        return ResultUtils.success(appVersion);
    }
}
