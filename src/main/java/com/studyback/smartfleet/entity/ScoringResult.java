package com.studyback.smartfleet.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 评分结果类
 * <p>封装单个车辆的多维度评分结果和加权总分</p>
 */
@Data
public class ScoringResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 车辆ID */
    private Long vehicleId;

    /** 各维度的评分 */
    private Map<ScoringDimension, Double> dimensionScores;

    /** 加权总分（0-100） */
    private double totalScore;

    /** 评分使用的场景 */
    private ScoringScene scene;

    /** 评分使用的权重 */
    private Map<ScoringDimension, Double> weights;

    public ScoringResult() {
    }

    public ScoringResult(Long vehicleId, Map<ScoringDimension, Double> dimensionScores,
                         double totalScore, ScoringScene scene,
                         Map<ScoringDimension, Double> weights) {
        this.vehicleId = vehicleId;
        this.dimensionScores = dimensionScores;
        this.totalScore = totalScore;
        this.scene = scene;
        this.weights = weights;
    }
}
