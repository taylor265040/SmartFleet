package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.config.ScoringStrategyFactory;
import com.studyback.smartfleet.entity.ScoringDimension;
import com.studyback.smartfleet.entity.ScoringScene;
import com.studyback.smartfleet.entity.ScoringStrategy;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleRecommendation;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.VehicleScoringService;
import com.studyback.smartfleet.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 车辆评分服务实现类
 * <p>实现多维加权评分引擎，支持策略模式的评分计算和车辆推荐</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleScoringServiceImpl implements VehicleScoringService {

    private final VehicleMapper vehicleMapper;
    private final ScoringStrategyFactory strategyFactory;
    private final RedisUtil redisUtil;

    /** 缓存前缀 */
    private static final String CACHE_PREFIX = "sf:recommend:";

    /** 缓存过期时间（秒） */
    private static final long CACHE_TTL_SECONDS = 300;

    /** 地球平均半径（公里） */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /** 默认搜索半径（公里） */
    private static final double DEFAULT_RADIUS_KM = 10.0;

    @Override
    public double calculateDistanceScore(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("距离不能为负数: " + distance);
        }
        // 使用指数衰减公式：score = 100 * e^(-distance/5)
        // 距离为0时得100分，距离约11.5公里时得10分，距离越大分数越低
        return 100 * Math.exp(-distance / 5.0);
    }

    @Override
    public double calculateBatteryScore(int batteryLevel) {
        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new IllegalArgumentException("电量必须在 0-100 之间: " + batteryLevel);
        }
        // 直接使用电量百分比作为评分
        return batteryLevel;
    }

    @Override
    public double calculateIdleTimeScore(long idleMinutes) {
        if (idleMinutes < 0) {
            throw new IllegalArgumentException("空闲分钟数不能为负数: " + idleMinutes);
        }
        // 空闲时长评分：min(100, idleMinutes * 2)
        // 空闲30分钟得60分，50分钟得100分（封顶）
        return Math.min(100, idleMinutes * 2);
    }

    @Override
    public double calculateHealthScore(int healthScore) {
        if (healthScore < 0 || healthScore > 100) {
            throw new IllegalArgumentException("健康度必须在 0-100 之间: " + healthScore);
        }
        // 直接使用健康度分数作为评分
        return healthScore;
    }

    @Override
    public double calculateWeightedScore(Vehicle vehicle, double userLat, double userLng,
                                         Map<ScoringDimension, Double> weights) {
        if (vehicle == null) {
            throw new IllegalArgumentException("车辆不能为空");
        }
        if (weights == null || weights.isEmpty()) {
            throw new IllegalArgumentException("权重配置不能为空");
        }

        // 计算距离（如果车辆有坐标信息）
        double distance = 0.0;
        if (vehicle.getCurrentLat() != null && vehicle.getCurrentLng() != null) {
            distance = haversineDistance(userLat, userLng,
                    vehicle.getCurrentLat().doubleValue(), vehicle.getCurrentLng().doubleValue());
        }

        // 计算各维度评分
        double distanceScore = calculateDistanceScore(distance);
        double batteryScore = calculateBatteryScore(vehicle.getBatteryLevel() != null ? vehicle.getBatteryLevel() : 0);
        double healthScoreVal = calculateHealthScore(vehicle.getHealthScore() != null ? vehicle.getHealthScore() : 0);

        // 计算空闲时长评分
        long idleMinutes = 0;
        if (vehicle.getIdleStart() != null) {
            idleMinutes = Duration.between(vehicle.getIdleStart(), LocalDateTime.now()).toMinutes();
        }
        double idleTimeScore = calculateIdleTimeScore(idleMinutes);

        // 使用普通策略进行加权计算（默认场景）
        ScoringStrategy strategy = strategyFactory.getStrategy(ScoringScene.NORMAL);
        return strategy.calculateScore(distanceScore, batteryScore, idleTimeScore, healthScoreVal);
    }

    @Override
    public List<VehicleRecommendation> recommendForMap(double lat, double lng, double radius, int limit) {
        validateCoordinates(lat, lng);
        if (limit <= 0) {
            throw new IllegalArgumentException("返回数量限制必须大于0: " + limit);
        }
        // 使用局部final变量，确保lambda中可引用
        final double effectiveRadius = (radius > 0) ? radius : DEFAULT_RADIUS_KM;

        log.info("地图推荐请求: lat={}, lng={}, radius={}, limit={}", lat, lng, effectiveRadius, limit);

        // 检查缓存
        String cacheKey = CACHE_PREFIX + "map:" + lat + ":" + lng + ":" + effectiveRadius + ":" + limit;
        List<VehicleRecommendation> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            log.info("命中缓存: key={}", cacheKey);
            return cached;
        }

        // 查询所有可用车辆
        List<Vehicle> vehicles = loadAvailableVehicles();

        // 筛选半径内的车辆并计算评分
        List<VehicleRecommendation> recommendations = vehicles.stream()
                .map(vehicle -> {
                    double distance = haversineDistance(lat, lng,
                            vehicle.getCurrentLat().doubleValue(), vehicle.getCurrentLng().doubleValue());
                    if (distance > effectiveRadius) {
                        return null;
                    }
                    double score = calculateVehicleScore(vehicle, distance);
                    return VehicleRecommendation.fromVehicleWithLocation(vehicle, score, distance);
                })
                .filter(rec -> rec != null)
                .sorted(Comparator.comparingDouble(VehicleRecommendation::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 写入缓存
        cacheRecommendations(cacheKey, recommendations);

        log.info("地图推荐完成: 返回 {} 辆车", recommendations.size());
        return recommendations;
    }

    @Override
    public List<VehicleRecommendation> recommendForList(double lat, double lng, int limit) {
        validateCoordinates(lat, lng);
        if (limit <= 0) {
            throw new IllegalArgumentException("返回数量限制必须大于0: " + limit);
        }

        log.info("列表推荐请求: lat={}, lng={}, limit={}", lat, lng, limit);

        // 检查缓存
        String cacheKey = CACHE_PREFIX + "list:" + lat + ":" + lng + ":" + limit;
        List<VehicleRecommendation> cached = getCachedRecommendations(cacheKey);
        if (cached != null) {
            log.info("命中缓存: key={}", cacheKey);
            return cached;
        }

        // 查询所有可用车辆
        List<Vehicle> vehicles = loadAvailableVehicles();

        // 计算评分并排序
        List<VehicleRecommendation> recommendations = vehicles.stream()
                .map(vehicle -> {
                    double distance = 0.0;
                    if (vehicle.getCurrentLat() != null && vehicle.getCurrentLng() != null) {
                        distance = haversineDistance(lat, lng,
                                vehicle.getCurrentLat().doubleValue(), vehicle.getCurrentLng().doubleValue());
                    }
                    double score = calculateVehicleScore(vehicle, distance);
                    return VehicleRecommendation.fromVehicleWithoutLocation(vehicle, score, distance);
                })
                .sorted(Comparator.comparingDouble(VehicleRecommendation::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 写入缓存
        cacheRecommendations(cacheKey, recommendations);

        log.info("列表推荐完成: 返回 {} 辆车", recommendations.size());
        return recommendations;
    }

    @Override
    public double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        // Haversine 公式：计算地球表面两点间的球面距离
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /**
     * 计算车辆综合评分（使用默认普通策略）
     *
     * @param vehicle  车辆
     * @param distance 距离（公里）
     * @return 综合评分（0-100）
     */
    private double calculateVehicleScore(Vehicle vehicle, double distance) {
        double distanceScore = calculateDistanceScore(distance);
        double batteryScore = calculateBatteryScore(
                vehicle.getBatteryLevel() != null ? vehicle.getBatteryLevel() : 0);
        double healthScoreVal = calculateHealthScore(
                vehicle.getHealthScore() != null ? vehicle.getHealthScore() : 0);

        // 计算空闲时长
        long idleMinutes = 0;
        if (vehicle.getIdleStart() != null) {
            idleMinutes = Duration.between(vehicle.getIdleStart(), LocalDateTime.now()).toMinutes();
        }
        double idleTimeScore = calculateIdleTimeScore(idleMinutes);

        // 使用普通策略计算加权评分
        ScoringStrategy strategy = strategyFactory.getStrategy(ScoringScene.NORMAL);
        return strategy.calculateScore(distanceScore, batteryScore, idleTimeScore, healthScoreVal);
    }

    /**
     * 加载所有可用车辆
     *
     * @return 可用车辆列表
     */
    private List<Vehicle> loadAvailableVehicles() {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vehicle::getStatus, "AVAILABLE")
                .eq(Vehicle::getDeleted, 0)
                .isNotNull(Vehicle::getCurrentLat)
                .isNotNull(Vehicle::getCurrentLng);
        return vehicleMapper.selectList(wrapper);
    }

    /**
     * 校验坐标有效性
     *
     * @param lat 纬度
     * @param lng 经度
     * @throws IllegalArgumentException 当坐标无效时抛出
     */
    private void validateCoordinates(double lat, double lng) {
        if (lat < -90 || lat > 90) {
            throw new IllegalArgumentException("纬度必须在 -90~90 之间: " + lat);
        }
        if (lng < -180 || lng > 180) {
            throw new IllegalArgumentException("经度必须在 -180~180 之间: " + lng);
        }
    }

    /**
     * 从缓存获取推荐结果
     *
     * @param cacheKey 缓存key
     * @return 缓存的推荐列表，未命中返回null
     */
    @SuppressWarnings("unchecked")
    private List<VehicleRecommendation> getCachedRecommendations(String cacheKey) {
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached instanceof List<?>) {
                return (List<VehicleRecommendation>) cached;
            }
        } catch (Exception e) {
            log.warn("读取缓存失败，降级查询数据库: key={}", cacheKey, e);
        }
        return null;
    }

    /**
     * 将推荐结果写入缓存
     *
     * @param cacheKey      缓存key
     * @param recommendations 推荐列表
     */
    private void cacheRecommendations(String cacheKey, List<VehicleRecommendation> recommendations) {
        try {
            redisUtil.set(cacheKey, recommendations, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("写入缓存失败: key={}", cacheKey, e);
        }
    }
}
