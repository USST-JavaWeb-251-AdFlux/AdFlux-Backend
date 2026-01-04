package com.usst.adfluxbackend.common;


import com.usst.adfluxbackend.model.entity.AdDisplays;
import java.math.BigDecimal;
import java.util.List;

// 引入常量
import static com.usst.adfluxbackend.constant.CostConstant.*;

/**
 * 广告计费核心计算组件
 * 纯逻辑计算，不涉及数据库操作
 */
public class AdBillCalculator {

    /**
     * 计算广告主的花费 (Total Cost)
     * = 点击次数 * 点击单价 + 展示次数 * 展示单价
     */
    public static BigDecimal calculateAdvertiserCost(List<AdDisplays> displays) {
        BigDecimal totalCost = BigDecimal.ZERO;
        if (displays == null || displays.isEmpty()) {
            return totalCost;
        }

        for (AdDisplays display : displays) {
            // 假设 1 代表点击
            if (display.getClicked() != null && display.getClicked() == 1) {
                totalCost = totalCost.add(PRICE_PER_CLICK);
            } else {
                totalCost = totalCost.add(PRICE_PER_DISPLAY);
            }
        }
        return totalCost;
    }

    /**
     * 计算平台收益 (Platform Revenue)
     * 假设平台收益 = 广告主花费 * 抽成率 (例如 50%)
     * 或者：收益 = 广告主花费 (如果全归平台)
     */
    public static BigDecimal calculatePlatformRevenue(BigDecimal totalAdvertiserCost) {
        if (totalAdvertiserCost == null) {
            return BigDecimal.ZERO;
        }
        // 使用常量类中的抽成率进行计算
        return totalAdvertiserCost.multiply(PLATFORM_COMMISSION_RATE);
    }
    
    /**
     * 计算流量主(网站主)的分成 (Publisher Revenue)
     * = 总花费 - 平台收益
     */
    public static BigDecimal calculatePublisherRevenue(BigDecimal totalAdvertiserCost) {
        if (totalAdvertiserCost == null) {
             return BigDecimal.ZERO;
        }
        BigDecimal platformShare = calculatePlatformRevenue(totalAdvertiserCost);
        return totalAdvertiserCost.subtract(platformShare);
    }
}