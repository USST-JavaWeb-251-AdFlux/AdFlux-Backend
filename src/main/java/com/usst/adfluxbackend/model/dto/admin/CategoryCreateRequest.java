package com.usst.adfluxbackend.model.dto.admin;

import lombok.Data;

import java.io.Serializable;

/**
 * 广告分类创建请求
 */
@Data
public class CategoryCreateRequest implements Serializable {

    /**
     * 类别名称
     */
    private String categoryName;

    private static final long serialVersionUID = 1L;
}