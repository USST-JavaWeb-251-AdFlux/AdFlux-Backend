package com.usst.adfluxbackend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.model.dto.admin.AdReviewRequest;
import com.usst.adfluxbackend.model.dto.admin.AdminCreateRequest;
import com.usst.adfluxbackend.model.dto.admin.CategoryCreateRequest;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.model.entity.Publishers;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.vo.*;
import com.usst.adfluxbackend.service.AdCategoriesService;
import com.usst.adfluxbackend.service.AdvertisementsService;
import com.usst.adfluxbackend.service.PublishersService;
import com.usst.adfluxbackend.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequireRole("admin")
@RequestMapping("/admin")
public class AdminController {
    
    @Resource
    private AdvertisementsService advertisementsService;
    
    @Resource
    private AdCategoriesService adCategoriesService;
    
    @Resource
    private UsersService usersService;

    @Resource
    private PublishersService publishersService;


    /**
     * 获取待审核广告列表
     *
     * @param status 审核状态
     * @param page 页码
     * @param pageSize 每页数量
     * @return 广告分页列表
     */
    @GetMapping("/ads")
    public BaseResponse<IPage<AdvertisementVO>> listAdsForAdmin(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        IPage<AdvertisementVO> advertisementPage = advertisementsService.listAdsForAdmin(status, page, pageSize);
        return ResultUtils.success(advertisementPage);
    }
    
    /**
     * 提交广告审核结果
     *
     * @param adId 广告ID
     * @param reviewRequest 审核请求
     * @return 更新后的广告详情
     */
//    @PutMapping("/ads/{adId}/review")
//    public BaseResponse<AdvertisementVO> reviewAdvertisement(@PathVariable Long adId,
//                                                           @RequestBody AdReviewRequest reviewRequest) {
//        AdvertisementVO advertisementVO = advertisementsService.reviewAdvertisement(
//                adId, reviewRequest.getReviewStatus(), reviewRequest.getReason());
//        return ResultUtils.success(advertisementVO);
//    }
    @PutMapping("/ads/{adId}/review")
    public BaseResponse<AdvertisementReviewVO> reviewAdvertisement(@PathVariable Long adId,
            @RequestBody AdReviewRequest reviewRequest) {
        AdvertisementReviewVO reviewVO = advertisementsService.reviewAdvertisement(
                adId, reviewRequest.getReviewStatus(), reviewRequest.getReason());
        return ResultUtils.success(reviewVO);
    }


    /**
     * 新增广告分类
     *
     * @param createRequest 创建分类请求
     * @return 新增的分类对象
     */
    @PostMapping("/categories")
    public BaseResponse<CategoryVO> createCategory(@RequestBody CategoryCreateRequest createRequest) {
        AdCategories category = adCategoriesService.createCategory(createRequest.getCategoryName());
        CategoryVO categoryVO = new CategoryVO();
        BeanUtils.copyProperties(category, categoryVO);
        return ResultUtils.success(categoryVO);
    }
    
    /**
     * 获取用户列表
     *
     * @param role 角色过滤：admin/advertiser/publisher
     * @return 用户列表
     */
    @GetMapping("/users")
    public BaseResponse<List<UsersVO>> listUsers(@RequestParam(required = false) String role) {
        List<UsersVO> users = usersService.listUsers(role);
        return ResultUtils.success(users);
    }
    
    /**
     * 创建管理员账号
     *
     * @param createRequest 创建管理员请求
     * @return 新建管理员的用户信息
     */
    @PostMapping("/users")
    public BaseResponse<Users> createAdmin(@RequestBody AdminCreateRequest createRequest) {
        Users user = usersService.createAdmin(
                createRequest.getUsername(),
                createRequest.getPassword(),
                createRequest.getEmail(),
                createRequest.getPhone()
        );
        return ResultUtils.success(user);
    }

    /**
     * 管理员首页统计数据（汇总若干 key metrics）
     *
     * 返回：
     *  - totalAds: 系统中广告总数
     *  - activeReviewedAds: 已通过审核并启用的广告数
     *  - pendingAds: 待审核广告数
     *  - totalWebsites: 网站数量
     *  - totalUsers: 用户数量
     *  - totalCategories: 广告分类数量
     *
     * @return 管理员仪表盘统计
     */
    @GetMapping("/dashboard")
    public BaseResponse<AdminDashboardVO> getAdminDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();

        // 广告相关统计（advertisementsService 中已实现 countActiveReviewedAds / countPendingAds）
        vo.setTotalAds(advertisementsService.count()); // total
        vo.setActiveReviewedAds(advertisementsService.countActiveReviewedAds());
        vo.setPendingAds(advertisementsService.countPendingAds());

        // 网站数量（PublishersService 提供 listSites）
        List<Publishers> sites = publishersService.listSites();
        vo.setTotalWebsites(sites == null ? 0L : (long) sites.size());

        // 用户数量（usersService.listUsers(null) 返回全部用户 VO 列表）
        List<UsersVO> allUsers = usersService.listUsers(null);
        vo.setTotalUsers(allUsers == null ? 0L : (long) allUsers.size());

        // 分类数量（adCategoriesService 若有 list 方法 用其获取）
        List<AdCategories> categories = adCategoriesService.listAllCategories();
        vo.setTotalCategories(categories == null ? 0L : (long) categories.size());

        return ResultUtils.success(vo);
    }
}