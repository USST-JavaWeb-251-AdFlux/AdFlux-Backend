package com.usst.adfluxbackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.usst.adfluxbackend.mapper.AdCategoriesMapper;
import com.usst.adfluxbackend.model.entity.AdCategories;
import com.usst.adfluxbackend.service.AdCategoriesService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
* @author 30637
* @description 针对表【ad_categories(广告分类表)】的数据库操作Service实现
* @createDate 2025-12-14 10:56:37
*/
@Service
public class AdCategoriesServiceImpl extends ServiceImpl<AdCategoriesMapper, AdCategories>
    implements AdCategoriesService{

    /**
     * 获取所有广告分类列表
     *
     * @return 广告分类列表
     */
    @Override
    public List<AdCategories> listAllCategories() {
        return this.list();
    }

    /**
     * 创建新的广告分类
     *
     * @param categoryName 分类名称
     * @return 创建的分类信息
     */
    @Override
    public AdCategories createCategory(String categoryName) {
        AdCategories category = new AdCategories();
        category.setCategoryName(categoryName);
        category.setCreateTime(new Date());
        this.save(category);
        return category;
    }
}