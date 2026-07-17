package com.studyback.smartfleet.entity;

import java.util.EnumMap;
import java.util.Map;

/**
 * 评分策略抽象基类
 * <p>封装权重归一化和加权评分的通用逻辑</p>
 */
public abstract class AbstractScoringStrategy implements ScoringStrategy {

    /** 原始权重配置（归一化前） */
    private final Map<ScoringDimension, Double> rawWeights;

    /** 归一化后的权重缓存 */
    private Map<ScoringDimension, Double> normalizedWeights;

    protected AbstractScoringStrategy(Map<ScoringDimension, Double> rawWeights) {
        this.rawWeights = new EnumMap<>(rawWeights);
        this.normalizedWeights = normalize(rawWeights);
    }

    @Override
    public Map<ScoringDimension, Double> getWeights() {
        return normalizedWeights;
    }

    /**
     * 更新权重配置（支持运行时热更新）
     *
     * @param newWeights 新的权重配置
     */
    public void updateWeights(Map<ScoringDimension, Double> newWeights) {
        this.rawWeights.clear();
        this.rawWeights.putAll(newWeights);
        this.normalizedWeights = normalize(newWeights);
    }

    @Override
    public double calculateScore(double distanceScore, double batteryScore,
                                 double idleTimeScore, double healthScore) {
        Map<ScoringDimension, Double> weights = getWeights();
        double score = distanceScore * weights.getOrDefault(ScoringDimension.DISTANCE, 0.0)
                + batteryScore * weights.getOrDefault(ScoringDimension.BATTERY, 0.0)
                + idleTimeScore * weights.getOrDefault(ScoringDimension.IDLE_TIME, 0.0)
                + healthScore * weights.getOrDefault(ScoringDimension.HEALTH, 0.0);
        // 确保评分在 0-100 范围内
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 权重归一化：将权重和不为1的情况自动归一化，使权重和等于1.0
     * <p>如果所有权重都为0，则返回等权重配置</p>
     *
     * @param weights 原始权重
     * @return 归一化后的权重
     */
    private Map<ScoringDimension, Double> normalize(Map<ScoringDimension, Double> weights) {
        Map<ScoringDimension, Double> result = new EnumMap<>(ScoringDimension.class);
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        if (sum == 0) {
            // 全零权重时使用等权重
            double equalWeight = 1.0 / ScoringDimension.values().length;
            for (ScoringDimension dim : ScoringDimension.values()) {
                result.put(dim, equalWeight);
            }
        } else {
            for (Map.Entry<ScoringDimension, Double> entry : weights.entrySet()) {
                result.put(entry.getKey(), entry.getValue() / sum);
            }
        }
        return result;
    }
}
