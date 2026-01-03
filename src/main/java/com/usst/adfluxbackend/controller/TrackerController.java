package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.model.dto.publisher.UpdateAdSlotRequest;
import com.usst.adfluxbackend.model.dto.tracker.AdSlotRequest;
import com.usst.adfluxbackend.model.dto.tracker.TrackPageViewRequest;
import com.usst.adfluxbackend.model.dto.tracker.UpdateAdDisplayRequest;
import com.usst.adfluxbackend.model.dto.tracker.UpdateDurationRequest;
import com.usst.adfluxbackend.model.vo.AdSlotVO;
import com.usst.adfluxbackend.model.vo.TrackPageViewVO;
import com.usst.adfluxbackend.service.TrackerService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 页面访问跟踪控制器（无需鉴权）
 */
@RestController
@RequestMapping("/track")
@CrossOrigin(origins = "*", allowCredentials = "false")
@RequireRole(disabled = true)
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
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean success = trackerService.updateVisitDuration(request.getVisitId(), request.getDuration());
        return ResultUtils.success(success);
    }
    
    /**
     * 根据条件筛选并返回合适的广告
     *
     * 示例请求路径：POST /track/ad-slot
     * 请求体：{"trackId": "...", "domain": "...", "adType": 0, "adLayout": "banner"}
     */
    @PostMapping("/ad-slot")
    public BaseResponse<AdSlotVO> getAdForSlot(@RequestBody AdSlotRequest request) {
        if(request == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        
        AdSlotVO adSlotVO = trackerService.selectAdForSlot(
                request.getTrackId(), 
                request.getDomain(), 
                request.getAdType(), 
                request.getAdLayout()
        );
        
        return ResultUtils.success(adSlotVO);
    }
    /**
     * 更新广告展示状态（展示时长 / 点击状态）
     *
     * 示例请求路径：
     * PUT /track/ad-slot/{displayId}
     *
     * 路径参数：
     * displayId - 广告展示记录 ID
     *
     * 请求体示例：
     * {
     *   "duration": 120,
     *   "clicked": 1
     * }
     *
     * 业务说明：
     * - 展示时长 duration 只能递增
     * - 点击状态 clicked 只能从 0 变为 1，不可回退
     * - 当发生点击时，会记录最后一次点击时间
     *
     * @param displayId 广告展示记录 ID
     * @param request   更新展示状态请求体
     * @return 是否更新成功
     */
    @PutMapping("/ad-slot/{displayId}")
    public BaseResponse<Boolean> updateAdDisplay(
            @PathVariable Long displayId,
            @RequestBody UpdateAdDisplayRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean success = trackerService.updateAdDisplay(
                displayId,
                request.getDuration(),
                request.getClicked()
        );
        return ResultUtils.success(success);
    }

}