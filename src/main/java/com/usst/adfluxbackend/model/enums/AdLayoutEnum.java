package com.usst.adfluxbackend.model.enums;

public enum AdLayoutEnum {
    BANNER(0),
    SIDEBAR(1),
    CARD(2);

    private final int code;
    AdLayoutEnum(int code) { this.code = code; }
    public int getCode() { return code; }
}
