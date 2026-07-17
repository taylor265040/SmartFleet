package com.studyback.smartfleet.entity;

import java.util.Map;

/**
 * 低电量召回评分策略
 * <p>空闲时长权重最高，优先召回长时间空闲的低电量车辆进行充电维护</p>
 * <p>适用场景：系统主动召回低电量车辆，或为充电调度提供决策依据</p>
 */
public class LowBatteryScoringStrategy extends AbstractScoringStrategy {

    /** 低电量召回默认权重：距离0.15、电量0.15、空闲时长0.45、健康度0.25 */
    private static final Map<ScoringDimension, Double> DEFAULT_WEIGHTS = Map.of(
            ScoringDimension.DISTANCE, 0.15,
            ScoringDimension.BATTERY, 0.15,
            ScoringDimension.IDLE_TIME, 0.45,
            ScoringDimension.HEALTH, 0.25
    );

    public LowBatteryScoringStrategy() {
        super(DEFAULT_WEIGHTS);
    }

    public LowBatteryScoringStrategy(Map<ScoringDimension, Double> weights) {
        super(weights);
    }

    @Override
    public ScoringScene getScene() {
        return ScoringScene.LOW_BATTERY;
    }
}
