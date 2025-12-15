package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 广告分类表
 * @TableName ad_categories
 */
@TableName(value ="ad_categories")
@Data
public class AdCategories {
    /**
     * 广告类别 ID
     */
    private Long categoryId;

    /**
     * 类别名称
     */
    private String categoryName;

    /**
     * 创建时间
     */
    private Date createTime;
}