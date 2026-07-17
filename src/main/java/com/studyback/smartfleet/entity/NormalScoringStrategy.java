package com.studyback.smartfleet.entity;

import java.util.Map;

/**
 * 普通时段评分策略
 * <p>距离权重最高，优先推荐距离用户最近的可用车辆</p>
 * <p>适用场景：非高峰时段，运力充足时使用</p>
 */
public class NormalScoringStrategy extends AbstractScoringStrategy {

    /** 普通时段默认权重：距离0.4、电量0.25、空闲时长0.15、健康度0.2 */
    private static final Map<ScoringDimension, Double> DEFAULT_WEIGHTS = Map.of(
            ScoringDimension.DISTANCE, 0.4,
            ScoringDimension.BATTERY, 0.25,
            ScoringDimension.IDLE_TIME, 0.15,
            ScoringDimension.HEALTH, 0.2
    );

    public NormalScoringStrategy() {
        super(DEFAULT_WEIGHTS);
    }

    public NormalScoringStrategy(Map<ScoringDimension, Double> weights) {
        super(weights);
    }

    @Override
    public ScoringScene getScene() {
        return ScoringScene.NORMAL;
    }
}
