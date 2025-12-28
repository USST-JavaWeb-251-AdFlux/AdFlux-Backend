package com.usst.adfluxbackend.controller;

import com.usst.adfluxbackend.annotation.RequireRole;
import com.usst.adfluxbackend.common.BaseResponse;
import com.usst.adfluxbackend.common.ResultUtils;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.model.vo.CategoryVO;
import com.usst.adfluxbackend.service.AdCategoriesService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/common")
@RequireRole
public class CommonController {

    @Resource
    private AdCategoriesService adCategoriesService;

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
}
