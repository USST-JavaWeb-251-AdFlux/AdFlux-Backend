package com.usst.adfluxbackend.model.vo;

import lombok.Data;

@Data
public class FileVO {
    // 媒体文件访问 URL
    String mediaUrl;
    // 媒体文件类型: 0 - image, 1 - video
    Integer adType;
}
