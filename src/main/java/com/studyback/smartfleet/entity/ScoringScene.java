package com.studyback.smartfleet.entity;

/**
 * 评分场景枚举
 * <p>定义不同的评分场景，每个场景对应不同的权重配置</p>
 */
public enum ScoringScene {

    /** 普通时段：距离权重高，优先推荐距离近的车辆 */
    NORMAL,

    /** 高峰时段：电量权重高，优先推荐电量充足的车辆 */
    PEAK_HOUR,

    /** 低电量召回：空闲时长权重高，优先召回长时间空闲的低电量车辆 */
    LOW_BATTERY
}
