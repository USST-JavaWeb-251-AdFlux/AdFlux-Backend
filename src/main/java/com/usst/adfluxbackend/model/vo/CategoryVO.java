package com.usst.adfluxbackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 广告分类信息视图
 */
@Data
public class CategoryVO implements Serializable {

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

    private static final long serialVersionUID = 1L;
}