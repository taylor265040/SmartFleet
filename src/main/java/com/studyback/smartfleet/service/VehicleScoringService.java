package com.studyback.smartfleet.service;

import com.studyback.smartfleet.entity.ScoringDimension;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleRecommendation;

import java.util.List;
import java.util.Map;

/**
 * 车辆评分服务接口
 * <p>提供多维加权评分计算和车辆推荐功能</p>
 */
public interface VehicleScoringService {

    /**
     * 计算距离评分
     * <p>使用 Haversine 公式计算两点间距离，再转换为 0-100 分</p>
     * <p>公式：score = 100 * e^(-distance/5)</p>
     *
     * @param distance 距离（公里），必须 >= 0
     * @return 评分（0-100）
     * @throws IllegalArgumentException 当距离为负数时抛出
     */
    double calculateDistanceScore(double distance);

    /**
     * 计算电量评分
     * <p>直接使用电量百分比作为评分</p>
     *
     * @param batteryLevel 电量百分比（0-100）
     * @return 评分（0-100）
     * @throws IllegalArgumentException 当电量不在 0-100 范围时抛出
     */
    double calculateBatteryScore(int batteryLevel);

    /**
     * 计算空闲时长评分
     * <p>公式：min(100, idleMinutes * 2)</p>
     * <p>空闲时间适中时分数最高，避免刚还车的车辆和长时间闲置的车辆</p>
     *
     * @param idleMinutes 空闲分钟数，必须 >= 0
     * @return 评分（0-100）
     * @throws IllegalArgumentException 当空闲分钟数为负数时抛出
     */
    double calculateIdleTimeScore(long idleMinutes);

    /**
     * 计算健康度评分
     * <p>直接使用健康度分数作为评分</p>
     *
     * @param healthScore 健康度分数（0-100）
     * @return 评分（0-100）
     * @throws IllegalArgumentException 当健康度不在 0-100 范围时抛出
     */
    double calculateHealthScore(int healthScore);

    /**
     * 计算加权评分
     * <p>根据各维度权重对车辆进行多维加权评分</p>
     * <p>权重和不为1时自动归一化</p>
     *
     * @param vehicle 车辆实体
     * @param userLat 用户纬度
     * @param userLng 用户经度
     * @param weights 各维度权重
     * @return 加权总分（0-100）
     * @throws IllegalArgumentException 当车辆为null或权重为空时抛出
     */
    double calculateWeightedScore(Vehicle vehicle, double userLat, double userLng,
                                  Map<ScoringDimension, Double> weights);

    /**
     * 地图区域车辆推荐
     * <p>返回指定范围内评分最高的车辆列表，包含坐标信息</p>
     *
     * @param lat    中心点纬度（-90~90）
     * @param lng    中心点经度（-180~180）
     * @param radius 搜索半径（公里）
     * @param limit  返回数量限制
     * @return 推荐车辆列表（含坐标），按评分降序排序
     * @throws IllegalArgumentException 当坐标无效或limit <= 0时抛出
     */
    List<VehicleRecommendation> recommendForMap(double lat, double lng, double radius, int limit);

    /**
     * 车辆列表推荐
     * <p>返回评分最高的车辆列表，不含坐标信息</p>
     *
     * @param lat   用户纬度（-90~90）
     * @param lng   用户经度（-180~180）
     * @param limit 返回数量限制
     * @return 推荐车辆列表（不含坐标），按评分降序排序
     * @throws IllegalArgumentException 当坐标无效或limit <= 0时抛出
     */
    List<VehicleRecommendation> recommendForList(double lat, double lng, int limit);

    /**
     * 使用 Haversine 公式计算两点间的球面距离（公里）
     *
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 距离（公里）
     */
    double haversineDistance(double lat1, double lng1, double lat2, double lng2);
}
