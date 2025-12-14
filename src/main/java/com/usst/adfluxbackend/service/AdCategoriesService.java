package com.usst.adfluxbackend.service;

import com.usst.adfluxbackend.model.entity.AdCategories;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 30637
* @description 针对表【ad_categories(广告分类表)】的数据库操作Service
* @createDate 2025-12-14 10:56:37
*/
public interface AdCategoriesService extends IService<AdCategories> {

    /**
     * 获取所有广告分类列表
     *
     * @return 广告分类列表
     */
    List<AdCategories> listAllCategories();

    /**
     * 创建新的广告分类
     *
     * @param categoryName 分类名称
     * @return 创建的分类信息
     */
    AdCategories createCategory(String categoryName);
}