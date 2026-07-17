package com.studyback.smartfleet.entity;

import java.util.Map;

/**
 * 评分策略接口
 * <p>定义不同场景下的评分策略，使用策略模式实现运行时动态切换</p>
 */
public interface ScoringStrategy {

    /**
     * 获取策略对应的评分场景
     *
     * @return 评分场景枚举
     */
    ScoringScene getScene();

    /**
     * 获取当前策略的权重配置
     *
     * @return 各维度权重，权重和归一化后为1.0
     */
    Map<ScoringDimension, Double> getWeights();

    /**
     * 计算车辆综合评分
     *
     * @param distanceScore  距离评分（0-100）
     * @param batteryScore   电量评分（0-100）
     * @param idleTimeScore  空闲时长评分（0-100）
     * @param healthScore    健康度评分（0-100）
     * @return 加权综合评分（0-100）
     */
    double calculateScore(double distanceScore, double batteryScore,
                          double idleTimeScore, double healthScore);
}
