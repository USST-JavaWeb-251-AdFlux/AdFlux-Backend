package com.usst.adfluxbackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 媒体文件类型枚举
 */
@Getter
public enum MediaType {

    IMAGE("图片", 0),
    VIDEO("视频", 1),
    ILLEGAL("非法类型", -1);

    private final String text;

    private final Integer value;

    MediaType(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的 value
     * @return 枚举值
     */
    public static MediaType getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MediaType mediaType : MediaType.values()) {
            if (mediaType.value.equals(value)) {
                return mediaType;
            }
        }
        return null;
    }
}