package com.usst.adfluxbackend.constant;

import java.math.BigDecimal;

/**
 * 费用相关常量类
 * <p>
 * 采用 final class + private constructor 模式，防止继承和实例化
 */
public final class CostConstant {

    /**
     * 私有构造器，防止被 new
     */
    private CostConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========================================================================
    // 计费单价
    // ========================================================================

    /**
     * 广告单次点击费用 (CPC - Cost Per Click)
     * 对应业务代码中的 BigDecimal.valueOf(2)
     */
    public static final BigDecimal PRICE_PER_CLICK = new BigDecimal("2.00");

    /**
     * 广告单次展示费用 (CPD - Cost Per Display / Impression)
     * 对应业务代码中的 BigDecimal.valueOf(0.01)
     */
    public static final BigDecimal PRICE_PER_DISPLAY = new BigDecimal("0.01");

    // ========================================================================
    // 分成与费率
    // ========================================================================

    /**
     * 平台抽成率 (Platform Commission Rate)
     * 示例：0.30 表示平台拿走 30%，流量主(Publisher) 拿 70%
     */
    public static final BigDecimal PLATFORM_COMMISSION_RATE = new BigDecimal("0.30");
}