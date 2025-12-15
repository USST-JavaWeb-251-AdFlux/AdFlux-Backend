package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.model.dto.admin.AdReviewRequest;
import com.usst.adfluxbackend.model.dto.admin.AdminCreateRequest;
import com.usst.adfluxbackend.model.dto.admin.CategoryCreateRequest;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.model.entity.Users;
import com.usst.adfluxbackend.model.vo.AdvertisementVO;
import com.usst.adfluxbackend.model.vo.CategoryVO;
import com.usst.adfluxbackend.service.AdCategoriesService;
import com.usst.adfluxbackend.service.AdvertisementsService;
import com.usst.adfluxbackend.service.UsersService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @Resource
    private AdvertisementsService advertisementsService;
    
    @Resource
    private AdCategoriesService adCategoriesService;
    
    @Resource
    private UsersService usersService;
    
    /**
     * 获取待审核广告列表
     *
     * @param status 审核状态，默认为 0（待审核）
     * @param current 页码
     * @param pageSize 每页数量
     * @return 广告分页列表
     */
    @GetMapping("/ads")
    public BaseResponse<com.baomidou.mybatisplus.core.metadata.IPage<AdvertisementVO>> listAdsForAdmin(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "1") Integer current,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        com.baomidou.mybatisplus.core.metadata.IPage<AdvertisementVO> advertisementPage = advertisementsService.listAdsForAdmin(status, current, pageSize);
        return ResultUtils.success(advertisementPage);
    }
    
    /**
     * 提交广告审核结果
     *
     * @param adId 广告ID
     * @param reviewRequest 审核请求
     * @return 更新后的广告详情
     */
    @PutMapping("/ads/{adId}/review")
    public BaseResponse<AdvertisementVO> reviewAdvertisement(@PathVariable Long adId,
                                                           @RequestBody AdReviewRequest reviewRequest) {
        AdvertisementVO advertisementVO = advertisementsService.reviewAdvertisement(
                adId, reviewRequest.getReviewStatus(), reviewRequest.getReason());
        return ResultUtils.success(advertisementVO);
    }
    
    /**
     * 获取广告分类列表
     *
     * @return 广告分类列表
     */
    @GetMapping("/categories")
    public BaseResponse<List<CategoryVO>> listAllCategories() {
        List<AdCategories> categories = adCategoriesService.listAllCategories();
        List<CategoryVO> categoryVOS = categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());
        return ResultUtils.success(categoryVOS);
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
    public BaseResponse<List<Users>> listUsers(@RequestParam(required = false) String role) {
        List<Users> users = usersService.listUsers(role);
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
}