package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 广告展示表
 * @TableName ad_displays
 */
@TableName(value ="ad_displays")
@Data
public class AdDisplays {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long displayId;

    /**
     * 匿名用户标识
     */
    private String trackId;

    /**
     * 广告 ID
     */
    private Long adId;

    /**
     * 网站 ID
     */
    private Long websiteId;

    /**
     * 停留时长
     */
    private Integer duration;

    /**
     * 是否点击
     */
    private Integer clicked;

    /**
     * 展示时间
     */
    private Date displayTime;

    /**
     * 点击时间
     */
    private Date clickTime;
}