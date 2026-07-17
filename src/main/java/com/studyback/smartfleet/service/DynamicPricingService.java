package com.studyback.smartfleet.service;

import java.time.LocalDateTime;

/**
 * 动态定价服务接口
 * <p>根据时段系数、区域密度、历史热度计算动态价格</p>
 */
public interface DynamicPricingService {

    /**
     * 计算动态价格
     * <p>公式：price = basePrice * timeCoefficient * densityCoefficient * historyCoefficient</p>
     * <p>价格上限：basePrice * 3.0</p>
     *
     * @param gridId    网格 GeoHash 编码
     * @param time      时间
     * @param basePrice 基础价格
     * @return 动态计算后的价格
     */
    double calculatePrice(String gridId, LocalDateTime time, double basePrice);
}
