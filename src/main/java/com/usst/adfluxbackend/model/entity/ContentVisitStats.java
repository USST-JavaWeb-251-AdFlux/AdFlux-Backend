package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 访问内容行为统计
 * @TableName content_visit_stats
 */
@TableName(value ="content_visit_stats")
@Data
public class ContentVisitStats {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long visitId;

    /**
     * 网站 ID
     */
    private Long websiteId;

    /**
     * 广告类别 ID
     */
    private Long categoryId;

    /**
     * 匿名用户标识
     */
    private String trackId;

    /**
     * 停留时长
     */
    private Integer duration;

    /**
     * 访问时间
     */
    private Date timestamp;
}