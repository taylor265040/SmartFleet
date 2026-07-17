package com.studyback.smartfleet.config;

import com.studyback.smartfleet.entity.AbstractScoringStrategy;
import com.studyback.smartfleet.entity.LowBatteryScoringStrategy;
import com.studyback.smartfleet.entity.NormalScoringStrategy;
import com.studyback.smartfleet.entity.PeakHourScoringStrategy;
import com.studyback.smartfleet.entity.ScoringDimension;
import com.studyback.smartfleet.entity.ScoringScene;
import com.studyback.smartfleet.entity.ScoringStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 评分策略工厂
 * <p>管理所有评分策略实例，支持运行时动态获取和切换策略</p>
 * <p>策略实例以 ConcurrentHashMap 存储，支持并发访问</p>
 */
@Slf4j
@Component
public class ScoringStrategyFactory {

    /** 策略实例缓存：场景 -> 策略实例 */
    private final Map<ScoringScene, ScoringStrategy> strategies = new ConcurrentHashMap<>();

    public ScoringStrategyFactory() {
        // 初始化默认策略
        strategies.put(ScoringScene.NORMAL, new NormalScoringStrategy());
        strategies.put(ScoringScene.PEAK_HOUR, new PeakHourScoringStrategy());
        strategies.put(ScoringScene.LOW_BATTERY, new LowBatteryScoringStrategy());
        log.info("评分策略工厂初始化完成，已注册 {} 个策略", strategies.size());
    }

    /**
     * 根据场景获取评分策略
     *
     * @param scene 评分场景
     * @return 对应的评分策略
     * @throws IllegalArgumentException 当场景为null时抛出
     */
    public ScoringStrategy getStrategy(ScoringScene scene) {
        if (scene == null) {
            throw new IllegalArgumentException("评分场景不能为空");
        }
        ScoringStrategy strategy = strategies.get(scene);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的评分策略: " + scene);
        }
        return strategy;
    }

    /**
     * 更新指定场景的策略权重（支持运行时热更新，不重启服务）
     *
     * @param scene    评分场景
     * @param weights  新的权重配置
     */
    public void updateWeights(ScoringScene scene, Map<ScoringDimension, Double> weights) {
        if (scene == null) {
            throw new IllegalArgumentException("评分场景不能为空");
        }
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("权重配置不能为空");
        }
        ScoringStrategy strategy = strategies.get(scene);
        if (strategy instanceof AbstractScoringStrategy) {
            ((AbstractScoringStrategy) strategy).updateWeights(weights);
            log.info("已更新场景 {} 的权重配置: {}", scene, weights);
        } else {
            log.warn("场景 {} 的策略不支持权重更新", scene);
        }
    }

    /**
     * 获取所有已注册的策略
     *
     * @return 所有策略的副本
     */
    public Map<ScoringScene, ScoringStrategy> getAllStrategies() {
        return new EnumMap<>(strategies);
    }
}
