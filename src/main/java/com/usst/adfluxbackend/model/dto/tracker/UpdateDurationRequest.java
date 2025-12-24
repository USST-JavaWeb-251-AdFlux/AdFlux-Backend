package com.usst.adfluxbackend.model.dto.tracker;

import lombok.Data;
import java.io.Serializable;

/**
 * 更新页面停留时长请求对象
 */
@Data
public class UpdateDurationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 访问记录ID
     */
    private Long visitId;

    /**
     * 新的停留时长（秒）
     */
    private Integer duration;
}
