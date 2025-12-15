package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 广告位信息表
 * @TableName ad_placements
 */
@TableName(value ="ad_placements")
@Data
public class AdPlacements {
    /**
     * 广告位 ID
     */
    @TableId
    private Long placementId;

    /**
     * 网站 ID
     */
    private Long websiteId;

    /**
     * 广告位名称
     */
    private String placementName;

    /**
     * 广告版式（banner/sidebar/card等）
     */
    private String adLayout;

    /**
     * 创建时间
     */
    private Date createTime;
}