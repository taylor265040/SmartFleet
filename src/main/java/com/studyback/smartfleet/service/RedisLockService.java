package com.studyback.smartfleet.service;

/**
 * Redis 预占锁服务接口
 * <p>基于 Redis Lua 脚本实现车辆预占锁，保证原子性</p>
 */
public interface RedisLockService {

    /**
     * 尝试获取车辆预占锁
     * <p>使用 Lua 脚本原子性地检查锁状态并设置锁</p>
     *
     * @param vehicleId   车辆ID
     * @param userId      用户ID
     * @param lockSeconds 锁过期时间（秒）
     * @return true=获取成功，false=已被其他人锁定
     * @throws IllegalArgumentException 参数为空或无效时抛出
     */
    boolean tryLock(Long vehicleId, Long userId, int lockSeconds);

    /**
     * 释放车辆预占锁
     * <p>仅锁持有者可以释放，防止误释放</p>
     *
     * @param vehicleId 车辆ID
     * @param userId    用户ID（必须是当前锁持有者）
     * @return true=释放成功，false=不是持有者或锁不存在
     */
    boolean releaseLock(Long vehicleId, Long userId);

    /**
     * 检查车辆是否被锁定
     *
     * @param vehicleId 车辆ID
     * @return true=已被锁定，false=未锁定
     */
    boolean isLocked(Long vehicleId);

    /**
     * 获取车辆锁的持有者
     *
     * @param vehicleId 车辆ID
     * @return 持有者用户ID，未锁定返回 null
     */
    Long getLockOwner(Long vehicleId);
}
