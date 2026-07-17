package com.studyback.smartfleet.service;

import com.studyback.smartfleet.entity.StateChangeRecord;
import com.studyback.smartfleet.entity.StateEvent;
import com.studyback.smartfleet.entity.VehicleStatus;

import java.util.List;
import java.util.Map;

/**
 * 车辆状态服务接口
 * <p>提供车辆状态查询、变更、历史记录等业务方法</p>
 */
public interface VehicleStateService {

    /**
     * 获取车辆当前状态
     * <p>优先从 Redis 缓存获取，缓存未命中则从数据库查询并回填缓存</p>
     *
     * @param vehicleId 车辆ID
     * @return 车辆状态
     */
    VehicleStatus getVehicleStatus(Long vehicleId);

    /**
     * 变更车辆状态
     * <p>流程：校验车辆存在 -> 获取当前状态 -> 状态机校验 -> 乐观锁更新数据库 -> 更新 Redis 缓存 -> 记录变更历史</p>
     *
     * @param vehicleId 车辆ID
     * @param event     触发事件
     * @return 变更后的状态
     */
    VehicleStatus changeVehicleStatus(Long vehicleId, StateEvent event);

    /**
     * 获取车辆状态变更历史
     *
     * @param vehicleId 车辆ID
     * @return 状态变更记录列表（按时间倒序）
     */
    List<StateChangeRecord> getStateChangeHistory(Long vehicleId);

    /**
     * 按状态统计车辆数量
     *
     * @return key=状态值, value=车辆数量
     */
    Map<String, Long> countByStatus();
}
