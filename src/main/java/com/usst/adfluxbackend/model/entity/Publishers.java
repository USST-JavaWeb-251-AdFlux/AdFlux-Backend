package com.usst.adfluxbackend.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 网站站长信息表
 * @TableName publishers
 */
@TableName(value ="publishers")
@Data
public class Publishers {
    /**
     * 网站 ID
     */
    @TableId
    private Long websiteId;

    /**
     * 网站站长 ID
     */
    private Long publisherId;

    /**
     * 网站名称
     */
    private String websiteName;

    /**
     * 网站地址
     */
    private String domain;

    /**
     * 验证代码
     */
    private String verificationToken;

    /**
     * 是否已通过验证
     */
    private Integer isVerified;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 验证时间
     */
    private Date verifyTime;
}