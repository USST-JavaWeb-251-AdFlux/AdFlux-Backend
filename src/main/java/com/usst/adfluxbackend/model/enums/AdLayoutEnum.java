package com.usst.adfluxbackend.model.enums;

import lombok.Getter;

@Getter
public enum AdLayoutEnum {
    VIDEO(0),
    BANNER(1),
    SIDEBAR(2);
    /**
     * 广告版式（0-video； 1-banner； 2-sidebar）
     */
    private final int code;
    AdLayoutEnum(int code) { this.code = code; }
}
