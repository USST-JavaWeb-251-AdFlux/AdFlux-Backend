package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.exception.ThrowUtils;
import com.usst.adfluxbackend.model.dto.tracker.TrackPageViewRequest;
import com.usst.adfluxbackend.model.dto.tracker.UpdateDurationRequest;
import com.usst.adfluxbackend.model.vo.TrackPageViewVO;
import com.usst.adfluxbackend.service.TrackerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 页面访问跟踪控制器（无需鉴权）
 */
@RestController
@RequestMapping("/track")
public class TrackerController {

    @Resource
    private TrackerService trackerService;

    /**
     * 记录页面访问并返回访问ID
     *
     * 示例请求路径：POST /track/page-view
     * 请求体：{"domain": "...", "categoryName": "...", "trackId": "..."}
     */
    @PostMapping("/page-view")
    public BaseResponse<TrackPageViewVO> createPageView(@RequestBody TrackPageViewRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long visitId = trackerService.trackPageView(request.getDomain(), request.getCategoryName(), request.getTrackId());
        TrackPageViewVO trackPageViewVO = new TrackPageViewVO();
        trackPageViewVO.setVisitId(String.valueOf(visitId));
        return ResultUtils.success(trackPageViewVO);
    }

    /**
     * 更新访问记录的停留时长
     *
     * 示例请求路径：PUT /track/page-view
     * 请求体：{"visitId": 123, "duration": 45}
     */
    @PutMapping("/page-view")
    public BaseResponse<Boolean> updateDuration(@RequestBody UpdateDurationRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean success = trackerService.updateVisitDuration(request.getVisitId(), request.getDuration());
        return ResultUtils.success(success);
    }
}
