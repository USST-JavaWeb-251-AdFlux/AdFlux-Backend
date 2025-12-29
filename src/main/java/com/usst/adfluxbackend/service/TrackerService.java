package com.usst.adfluxbackend.service;
import com.usst.adfluxbackend.model.vo.AdSlotVO;

public interface TrackerService {
    /**
     * 记录页面访问并返回访问ID
     *
     * @param domain 网站域名
     * @param categoryName 广告类别名称
     * @param trackId 匿名用户标识
     * @return 插入的访问记录ID
     */
    long trackPageView(String domain, String categoryName, String trackId);

    /**
     * 更新访问记录的停留时长
     *
     * @param visitId 访问记录ID
     * @param duration 新的停留时长
     * @return 更新是否成功
     */
    boolean updateVisitDuration(Long visitId, Integer duration);
    
    /**
     * 根据条件筛选并返回合适的广告
     *
     * @param trackId 匿名用户标识
     * @param domain 用户当前访问的网站域名
     * @param adType 请求的广告类型
     * @param adLayout 广告的展现形式
     * @return AdSlotVO 广告数据
     */
    AdSlotVO selectAdForSlot(String trackId, String domain, Integer adType, Integer adLayout);
}