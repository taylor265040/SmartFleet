package com.studyback.smartfleet.entity;

import java.util.Map;

/**
 * 高峰时段评分策略
 * <p>电量权重最高，优先推荐电量充足的车辆，避免用户在途中电量不足</p>
 * <p>适用场景：早晚高峰、节假日等高需求时段</p>
 */
public class PeakHourScoringStrategy extends AbstractScoringStrategy {

    /** 高峰时段默认权重：距离0.2、电量0.45、空闲时长0.1、健康度0.25 */
    private static final Map<ScoringDimension, Double> DEFAULT_WEIGHTS = Map.of(
            ScoringDimension.DISTANCE, 0.2,
            ScoringDimension.BATTERY, 0.45,
            ScoringDimension.IDLE_TIME, 0.1,
            ScoringDimension.HEALTH, 0.25
    );

    public PeakHourScoringStrategy() {
        super(DEFAULT_WEIGHTS);
    }

    public PeakHourScoringStrategy(Map<ScoringDimension, Double> weights) {
        super(weights);
    }

    @Override
    public ScoringScene getScene() {
        return ScoringScene.PEAK_HOUR;
    }
}
