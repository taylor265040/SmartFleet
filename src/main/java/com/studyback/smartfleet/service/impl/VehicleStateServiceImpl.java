package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.entity.StateChangeRecord;
import com.studyback.smartfleet.entity.StateEvent;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleStateMachine;
import com.studyback.smartfleet.entity.VehicleStateMachineImpl;
import com.studyback.smartfleet.entity.VehicleStatus;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.mapper.StateChangeRecordMapper;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.VehicleStateService;
import com.studyback.smartfleet.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 车辆状态服务实现类
 * <p>提供车辆状态查询、变更、历史记录等业务方法</p>
 * <p>状态变更时通过乐观锁保证并发安全，同步更新 Redis 缓存（降级处理）</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleStateServiceImpl implements VehicleStateService {

    /** Redis 缓存 key 前缀 */
    private static final String VEHICLE_STATUS_CACHE_PREFIX = "sf:vehicle:status:";

    /** 缓存过期时间：24 小时 */
    private static final long CACHE_EXPIRE_HOURS = 24;

    private final VehicleMapper vehicleMapper;
    private final StateChangeRecordMapper stateChangeRecordMapper;
    private final RedisUtil redisUtil;

    @Override
    public VehicleStatus getVehicleStatus(Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为 null");
        }

        // 优先从 Redis 缓存获取
        String cacheKey = VEHICLE_STATUS_CACHE_PREFIX + vehicleId;
        try {
            Object cached = redisUtil.get(cacheKey);
            if (cached != null) {
                VehicleStatus cachedStatus = VehicleStatus.fromValue(String.valueOf(cached));
                if (cachedStatus != null) {
                    log.debug("从 Redis 缓存获取车辆状态: vehicleId={}, status={}", vehicleId, cachedStatus);
                    return cachedStatus;
                }
            }
        } catch (Exception e) {
            // Redis 缓存获取失败，降级处理，继续从数据库查询
            log.warn("Redis 缓存获取失败，降级从数据库查询: vehicleId={}", vehicleId, e);
        }

        // 缓存未命中，从数据库查询
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "车辆不存在: " + vehicleId);
        }

        VehicleStatus status = VehicleStatus.fromValue(vehicle.getStatus());
        if (status == null) {
            log.warn("车辆状态值无效: vehicleId={}, status={}", vehicleId, vehicle.getStatus());
            return VehicleStatus.AVAILABLE;
        }

        // 回填缓存（降级处理）
        try {
            redisUtil.set(cacheKey, status.getValue(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 缓存回填失败: vehicleId={}", vehicleId, e);
        }

        return status;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VehicleStatus changeVehicleStatus(Long vehicleId, StateEvent event) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为 null");
        }
        if (event == null) {
            throw new IllegalArgumentException("状态转移事件不能为 null");
        }

        log.info("变更车辆状态: vehicleId={}, event={}", vehicleId, event);

        // 查询车辆（含乐观锁版本号）
        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "车辆不存在: " + vehicleId);
        }

        // 获取当前状态
        VehicleStatus currentStatus = VehicleStatus.fromValue(vehicle.getStatus());
        if (currentStatus == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR,
                    "车辆当前状态无效: vehicleId=" + vehicleId + ", status=" + vehicle.getStatus());
        }

        // 通过状态机校验并获取目标状态
        VehicleStateMachine stateMachine = new VehicleStateMachineImpl(currentStatus);
        VehicleStatus targetStatus = stateMachine.transition(event);

        // 乐观锁更新数据库
        Vehicle updateVehicle = new Vehicle();
        updateVehicle.setId(vehicleId);
        updateVehicle.setStatus(targetStatus.getValue());
        updateVehicle.setVersion(vehicle.getVersion());
        int rows = vehicleMapper.updateById(updateVehicle);
        if (rows == 0) {
            log.error("乐观锁更新失败，车辆状态已被其他线程修改: vehicleId={}", vehicleId);
            throw new BusinessException(ResultCode.CONFLICT, "车辆状态已被其他操作修改，请重试");
        }

        // 记录状态变更历史
        saveStateChangeRecord(vehicleId, currentStatus, targetStatus, event);

        // 更新 Redis 缓存（降级处理）
        updateRedisCache(vehicleId, targetStatus);

        log.info("车辆状态变更成功: vehicleId={}, {} -> {} (event={})",
                vehicleId, currentStatus, targetStatus, event);

        return targetStatus;
    }

    @Override
    public List<StateChangeRecord> getStateChangeHistory(Long vehicleId) {
        if (vehicleId == null) {
            throw new IllegalArgumentException("车辆ID不能为 null");
        }
        LambdaQueryWrapper<StateChangeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StateChangeRecord::getVehicleId, vehicleId)
                .orderByDesc(StateChangeRecord::getChangeTime);
        return stateChangeRecordMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Long> countByStatus() {
        Map<String, Long> result = new HashMap<>();
        for (VehicleStatus status : VehicleStatus.values()) {
            LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Vehicle::getStatus, status.getValue());
            Long count = vehicleMapper.selectCount(wrapper);
            result.put(status.getValue(), count);
        }
        return result;
    }

    /**
     * 保存状态变更记录
     */
    private void saveStateChangeRecord(Long vehicleId, VehicleStatus fromStatus,
                                        VehicleStatus toStatus, StateEvent event) {
        try {
            StateChangeRecord record = new StateChangeRecord();
            record.setVehicleId(vehicleId);
            record.setFromStatus(fromStatus.getValue());
            record.setToStatus(toStatus.getValue());
            record.setEvent(event.getValue());
            record.setChangeTime(LocalDateTime.now());
            stateChangeRecordMapper.insert(record);
        } catch (Exception e) {
            // 状态变更记录保存失败不影响主流程
            log.error("保存状态变更记录失败: vehicleId={}", vehicleId, e);
        }
    }

    /**
     * 更新 Redis 缓存（降级处理，不影响主流程）
     */
    private void updateRedisCache(Long vehicleId, VehicleStatus status) {
        String cacheKey = VEHICLE_STATUS_CACHE_PREFIX + vehicleId;
        try {
            redisUtil.set(cacheKey, status.getValue(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Redis 缓存更新失败（降级处理）: vehicleId={}", vehicleId, e);
        }
    }
}
