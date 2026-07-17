package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.entity.GridDispatch;
import com.studyback.smartfleet.entity.PricingRule;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.mapper.GridDispatchMapper;
import com.studyback.smartfleet.mapper.PricingRuleMapper;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.DynamicPricingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 动态定价服务实现类
 * <p>定价公式：price = basePrice * timeCoefficient * densityCoefficient * historyCoefficient</p>
 * <p>价格上限：basePrice * 3.0</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicPricingServiceImpl implements DynamicPricingService {

    private final PricingRuleMapper pricingRuleMapper;
    private final GridDispatchMapper gridDispatchMapper;

    /** 价格上限倍数 */
    private static final double MAX_PRICE_MULTIPLIER = 3.0;

    /** 早高峰时段系数 (7:00-9:00) */
    private static final double PEAK_MORNING_COEFFICIENT = 1.5;

    /** 晚高峰时段系数 (17:00-19:00) */
    private static final double PEAK_EVENING_COEFFICIENT = 1.5;

    /** 平峰时段系数 */
    private static final double NORMAL_COEFFICIENT = 1.0;

    /** 深夜时段系数 (23:00-5:00) */
    private static final double LATE_NIGHT_COEFFICIENT = 0.8;

    /** 默认密度系数 */
    private static final double DEFAULT_DENSITY_COEFFICIENT = 1.0;

    /** 默认历史热度系数 */
    private static final double DEFAULT_HISTORY_COEFFICIENT = 1.0;

    @Override
    public double calculatePrice(String gridId, LocalDateTime time, double basePrice) {
        // 参数校验
        if (gridId == null || gridId.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "网格ID不能为空");
        }
        if (time == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "时间不能为空");
        }
        if (basePrice <= 0) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "基础价格必须大于0");
        }

        // 1. 计算时段系数
        double timeCoefficient = calculateTimeCoefficient(time);

        // 2. 计算区域密度系数（根据供需比）
        double densityCoefficient = calculateDensityCoefficient(gridId);

        // 3. 计算历史热度系数（根据历史订单量）
        double historyCoefficient = calculateHistoryCoefficient(gridId);

        // 公式：price = basePrice * timeCoefficient * densityCoefficient * historyCoefficient
        double price = basePrice * timeCoefficient * densityCoefficient * historyCoefficient;

        // 价格上限：basePrice * 3.0
        double maxPrice = basePrice * MAX_PRICE_MULTIPLIER;
        if (price > maxPrice) {
            price = maxPrice;
        }

        log.info("动态定价计算: gridId={}, time={}, basePrice={}, timeCoeff={}, densityCoeff={}, historyCoeff={}, result={}",
                gridId, time, basePrice, timeCoefficient, densityCoefficient, historyCoefficient, price);

        return price;
    }

    /**
     * 计算时段系数
     * <p>早高峰(7-9): 1.5</p>
     * <p>晚高峰(17-19): 1.5</p>
     * <p>平峰: 1.0</p>
     * <p>深夜(23-5): 0.8</p>
     */
    private double calculateTimeCoefficient(LocalDateTime time) {
        int hour = time.getHour();

        // 早高峰 7:00-9:00
        if (hour >= 7 && hour < 9) {
            return PEAK_MORNING_COEFFICIENT;
        }
        // 晚高峰 17:00-19:00
        if (hour >= 17 && hour < 19) {
            return PEAK_EVENING_COEFFICIENT;
        }
        // 深夜 23:00-5:00
        if (hour >= 23 || hour < 5) {
            return LATE_NIGHT_COEFFICIENT;
        }
        // 平峰
        return NORMAL_COEFFICIENT;
    }

    /**
     * 计算区域密度系数
     * <p>根据供需比计算：供需比越低，密度系数越高</p>
     * <p>供需比 >= 2.0: 系数 1.0</p>
     * <p>供需比 1.0-2.0: 系数 1.2</p>
     * <p>供需比 0.5-1.0: 系数 1.5</p>
     * <p>供需比 < 0.5: 系数 2.0</p>
     */
    private double calculateDensityCoefficient(String gridId) {
        GridDispatch dispatch = gridDispatchMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GridDispatch>()
                        .eq("geohash", gridId)
        );

        if (dispatch == null || dispatch.getSupplyDemand() == null) {
            return DEFAULT_DENSITY_COEFFICIENT;
        }

        double ratio = dispatch.getSupplyDemand().doubleValue();

        if (ratio >= 2.0) {
            return 1.0;
        } else if (ratio >= 1.0) {
            return 1.2;
        } else if (ratio >= 0.5) {
            return 1.5;
        } else {
            return 2.0;
        }
    }

    /**
     * 计算历史热度系数
     * <p>根据该网格的历史订单量与平均值的比值计算</p>
     * <p>简化实现：使用供需比作为历史热度的代理指标</p>
     */
    private double calculateHistoryCoefficient(String gridId) {
        GridDispatch dispatch = gridDispatchMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<GridDispatch>()
                        .eq("geohash", gridId)
        );

        if (dispatch == null || dispatch.getDemandCount() == null) {
            return DEFAULT_HISTORY_COEFFICIENT;
        }

        // 使用需求量作为历史热度的代理指标
        // 需求量越高，热度系数越高
        int demandCount = dispatch.getDemandCount();

        if (demandCount >= 20) {
            return 1.5;
        } else if (demandCount >= 10) {
            return 1.2;
        } else if (demandCount >= 5) {
            return 1.0;
        } else {
            return 0.9;
        }
    }
}
