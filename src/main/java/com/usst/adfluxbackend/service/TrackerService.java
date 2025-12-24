package com.usst.adfluxbackend.service;

/**
 * 页面访问跟踪 Service 接口
 */
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
}
