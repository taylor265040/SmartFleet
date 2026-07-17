package com.studyback.smartfleet.service.impl;

import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.service.CacheAsideService;
import com.studyback.smartfleet.service.VehicleService;
import com.studyback.smartfleet.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Cache Aside 服务实现
 * <p>缓存 key: sf:vehicle:{vehicleId}，TTL: 30 分钟</p>
 * <p>空值缓存：防止缓存穿透，TTL: 5 分钟</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheAsideServiceImpl implements CacheAsideService {

    private static final String VEHICLE_CACHE_KEY_PREFIX = "sf:vehicle:";
    private static final long VEHICLE_CACHE_TTL_MINUTES = 30;
    private static final long NULL_CACHE_TTL_MINUTES = 5;
    private static final String NULL_PLACEHOLDER = "NULL";

    private final RedisUtil redisUtil;
    private final VehicleService vehicleService;

    /**
     * Cache Aside 读模式：
     * 1. 优先从 Redis 读取缓存
     * 2. 如果缓存命中（包括空值占位符），直接返回
     * 3. 如果缓存未命中，回源 MySQL 查询
     * 4. 将查询结果写入 Redis 缓存（空值缓存 5 分钟防止穿透）
     * 5. Redis 故障时降级到数据库查询
     */
    @Override
    public Vehicle getVehicle(Long vehicleId) {
        String cacheKey = VEHICLE_CACHE_KEY_PREFIX + vehicleId;

        // Step 1: 优先从 Redis 读取缓存
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached != null) {
                // 命中空值占位符，防止缓存穿透
                if (NULL_PLACEHOLDER.equals(cached.toString())) {
                    log.debug("Cache Aside 命中空值缓存, vehicleId={}", vehicleId);
                    return null;
                }
                // 命中有效缓存
                if (cached instanceof Vehicle) {
                    log.debug("Cache Aside 缓存命中, vehicleId={}", vehicleId);
                    return (Vehicle) cached;
                }
                // 缓存数据类型异常，删除后回源
                log.warn("Cache Aside 缓存数据类型异常, vehicleId={}, type={}", vehicleId, cached.getClass().getName());
                redisUtil.delete(cacheKey);
            }
        } catch (Exception e) {
            // Redis 故障降级到数据库查询
            log.warn("Cache Aside Redis 读取失败, 降级到数据库, vehicleId={}", vehicleId, e);
        }

        // Step 2: 缓存未命中，回源 MySQL 查询
        Vehicle vehicle = vehicleService.getById(vehicleId);

        // Step 3: 将查询结果写入 Redis 缓存
        try {
            if (vehicle != null) {
                // 正常数据缓存 30 分钟
                redisUtil.set(cacheKey, vehicle, VEHICLE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.debug("Cache Aside 回写缓存, vehicleId={}", vehicleId);
            } else {
                // 空值缓存 5 分钟，防止缓存穿透
                redisUtil.set(cacheKey, NULL_PLACEHOLDER, NULL_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.debug("Cache Aside 写入空值缓存, vehicleId={}", vehicleId);
            }
        } catch (Exception e) {
            // Redis 写入失败不影响业务，记录日志即可
            log.warn("Cache Aside 缓存写入失败, vehicleId={}", vehicleId, e);
        }

        return vehicle;
    }

    /**
     * Cache Aside 写模式：
     * 1. 先更新数据库
     * 2. 再更新缓存（而非删除缓存，确保缓存与数据库一致）
     */
    @Override
    public void updateVehicle(Vehicle vehicle) {
        // Step 1: 先更新数据库
        vehicleService.updateById(vehicle);
        log.info("Cache Aside 数据库更新成功, vehicleId={}", vehicle.getId());

        // Step 2: 再更新缓存
        String cacheKey = VEHICLE_CACHE_KEY_PREFIX + vehicle.getId();
        try {
            redisUtil.set(cacheKey, vehicle, VEHICLE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("Cache Aside 缓存更新成功, vehicleId={}", vehicle.getId());
        } catch (Exception e) {
            // 缓存更新失败不影响业务，记录日志
            log.warn("Cache Aside 缓存更新失败, vehicleId={}", vehicle.getId(), e);
        }
    }

    /**
     * Cache Aside 删除模式：
     * 1. 先删除数据库记录
     * 2. 再删除缓存
     */
    @Override
    public void deleteVehicle(Long vehicleId) {
        // Step 1: 先删除数据库
        vehicleService.removeById(vehicleId);
        log.info("Cache Aside 数据库删除成功, vehicleId={}", vehicleId);

        // Step 2: 再删除缓存
        String cacheKey = VEHICLE_CACHE_KEY_PREFIX + vehicleId;
        try {
            redisUtil.delete(cacheKey);
            log.debug("Cache Aside 缓存删除成功, vehicleId={}", vehicleId);
        } catch (Exception e) {
            // 缓存删除失败不影响业务，记录日志
            log.warn("Cache Aside 缓存删除失败, vehicleId={}", vehicleId, e);
        }
    }
}
