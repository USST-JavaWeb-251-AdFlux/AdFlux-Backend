package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.exception.BusinessException;
import com.usst.adfluxbackend.exception.ErrorCode;
import com.usst.adfluxbackend.model.dto.tracker.AdSlotRequest;
import com.usst.adfluxbackend.model.dto.tracker.TrackPageViewRequest;
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
@CrossOrigin(origins = "*")
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
}