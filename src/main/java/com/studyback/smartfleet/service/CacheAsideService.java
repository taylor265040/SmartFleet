package com.studyback.smartfleet.service;

import com.studyback.smartfleet.entity.Vehicle;

/**
 * Cache Aside 服务接口
 * <p>车辆状态优先走 Redis，失效回源 MySQL</p>
 * <p>缓存 key: sf:vehicle:{vehicleId}，TTL: 30 分钟</p>
 * <p>空值缓存：防止缓存穿透，TTL: 5 分钟</p>
 */
public interface CacheAsideService {

    /**
     * 获取车辆（Cache Aside 读模式）
     * <p>优先从 Redis 读取，缓存失效回源 MySQL 并回写缓存</p>
     * <p>缓存空值 5 分钟防止缓存穿透</p>
     *
     * @param vehicleId 车辆ID
     * @return 车辆实体，不存在返回 null
     */
    Vehicle getVehicle(Long vehicleId);

    /**
     * 更新车辆（Cache Aside 写模式）
     * <p>先更新数据库，再更新缓存</p>
     *
     * @param vehicle 车辆实体
     */
    void updateVehicle(Vehicle vehicle);

    /**
     * 删除车辆（Cache Aside 删除模式）
     * <p>先删除数据库，再删除缓存</p>
     *
     * @param vehicleId 车辆ID
     */
    void deleteVehicle(Long vehicleId);
}
