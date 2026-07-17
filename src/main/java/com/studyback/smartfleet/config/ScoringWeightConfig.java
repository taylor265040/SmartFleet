package com.studyback.smartfleet.config;

import com.studyback.smartfleet.entity.ScoringDimension;
import com.studyback.smartfleet.entity.ScoringScene;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 评分权重配置类
 * <p>从 application.yml 读取各场景的评分权重配置</p>
 * <p>配置示例：</p>
 * <pre>
 * scoring:
 *   weights:
 *     normal:
 *       distance: 0.4
 *       battery: 0.25
 *       idle-time: 0.15
 *       health: 0.2
 *     peak-hour:
 *       distance: 0.2
 *       battery: 0.45
 *       idle-time: 0.1
 *       health: 0.25
 *     low-battery:
 *       distance: 0.15
 *       battery: 0.15
 *       idle-time: 0.45
 *       health: 0.25
 * </pre>
 */
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "scoring")
public class ScoringWeightConfig {

    /** 各场景的权重配置 */
    private Map<String, Map<String, Double>> weights = new HashMap<>();

    /**
     * 获取指定场景的权重配置
     *
     * @param scene 评分场景
     * @return 维度权重映射
     */
    public Map<ScoringDimension, Double> getWeightsForScene(ScoringScene scene) {
        String sceneKey = toKebabCase(scene.name());
        Map<String, Double> sceneWeights = weights.get(sceneKey);

        if (sceneWeights == null || sceneWeights.isEmpty()) {
            log.info("未找到场景 {} 的自定义权重配置，使用默认权重", scene);
            return getDefaultWeights(scene);
        }

        Map<ScoringDimension, Double> result = new HashMap<>();
        for (Map.Entry<String, Double> entry : sceneWeights.entrySet()) {
            ScoringDimension dim = parseDimension(entry.getKey());
            if (dim != null) {
                result.put(dim, entry.getValue());
            }
        }

        // 补充缺失的维度（默认为0）
        for (ScoringDimension dim : ScoringDimension.values()) {
            result.putIfAbsent(dim, 0.0);
        }

        return result;
    }

    /**
     * 获取默认权重（硬编码兜底）
     */
    private Map<ScoringDimension, Double> getDefaultWeights(ScoringScene scene) {
        return switch (scene) {
            case NORMAL -> Map.of(
                    ScoringDimension.DISTANCE, 0.4,
                    ScoringDimension.BATTERY, 0.25,
                    ScoringDimension.IDLE_TIME, 0.15,
                    ScoringDimension.HEALTH, 0.2
            );
            case PEAK_HOUR -> Map.of(
                    ScoringDimension.DISTANCE, 0.2,
                    ScoringDimension.BATTERY, 0.45,
                    ScoringDimension.IDLE_TIME, 0.1,
                    ScoringDimension.HEALTH, 0.25
            );
            case LOW_BATTERY -> Map.of(
                    ScoringDimension.DISTANCE, 0.15,
                    ScoringDimension.BATTERY, 0.15,
                    ScoringDimension.IDLE_TIME, 0.45,
                    ScoringDimension.HEALTH, 0.25
            );
        };
    }

    /**
     * 解析维度名称（支持 kebab-case 和 UPPER_CASE）
     */
    private ScoringDimension parseDimension(String key) {
        try {
            return ScoringDimension.valueOf(key.replace("-", "_").toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("无法解析评分维度: {}", key);
            return null;
        }
    }

    /**
     * 将 UPPER_CASE 转换为 kebab-case
     */
    private String toKebabCase(String value) {
        return value.toLowerCase().replace("_", "-");
    }
}
