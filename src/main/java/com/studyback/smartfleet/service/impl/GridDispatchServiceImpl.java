package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyback.smartfleet.entity.GridDispatch;
import com.studyback.smartfleet.entity.OrderStatus;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleStatus;
import com.studyback.smartfleet.mapper.GridDispatchMapper;
import com.studyback.smartfleet.mapper.OrderMapper;
import com.studyback.smartfleet.mapper.VehicleMapper;
import com.studyback.smartfleet.service.GeoHashService;
import com.studyback.smartfleet.service.GridDispatchService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 网格运力 Service 实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GridDispatchServiceImpl extends ServiceImpl<GridDispatchMapper, GridDispatch>
        implements GridDispatchService {

    private final VehicleMapper vehicleMapper;
    private final OrderMapper orderMapper;
    private final GeoHashService geoHashService;

    /** 低供区域阈值 */
    private static final double LOW_SUPPLY_THRESHOLD = 0.5;

    /** 默认 GeoHash 精度 */
    private static final int GEOHASH_PRECISION = 6;

    @Override
    public double calculateSupplyDemandRatio(String gridId) {
        if (gridId == null || gridId.isEmpty()) {
            return 0.0;
        }

        GridDispatch dispatch = lambdaQuery()
                .eq(GridDispatch::getGeohash, gridId)
                .one();

        if (dispatch == null) {
            log.info("网格运力数据不存在: gridId={}", gridId);
            return 0.0;
        }

        int availableCount = dispatch.getAvailableCount() != null ? dispatch.getAvailableCount() : 0;
        int demandCount = dispatch.getDemandCount() != null ? dispatch.getDemandCount() : 0;

        if (demandCount == 0) {
            return availableCount > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }

        double ratio = (double) availableCount / demandCount;
        log.info("供需比计算: gridId={}, available={}, demand={}, ratio={}", gridId, availableCount, demandCount, ratio);
        return ratio;
    }

    @Override
    public void updateGridCapacity(String gridId) {
        if (gridId == null || gridId.isEmpty()) {
            return;
        }

        log.info("更新网格运力数据: gridId={}", gridId);

        // 统计该网格内的车辆数量（通过 GeoHash 匹配）
        // 查询所有车辆，按 GeoHash 分组统计
        List<Vehicle> allVehicles = vehicleMapper.selectList(null);

        int totalVehicles = 0;
        int availableCount = 0;

        for (Vehicle vehicle : allVehicles) {
            if (vehicle.getCurrentLat() == null || vehicle.getCurrentLng() == null) {
                continue;
            }
            String vehicleGeoHash = geoHashService.encode(
                    vehicle.getCurrentLat().doubleValue(),
                    vehicle.getCurrentLng().doubleValue(),
                    GEOHASH_PRECISION
            );
            if (gridId.equals(vehicleGeoHash)) {
                totalVehicles++;
                if (VehicleStatus.AVAILABLE.getValue().equals(vehicle.getStatus())) {
                    availableCount++;
                }
            }
        }

        // 统计该网格内的活跃订单数
        // 活跃订单 = CREATED + CONFIRMED + RUNNING
        int demandCount = 0;
        // 这里简化处理：通过查询所有活跃订单，检查起点是否在该网格
        // 实际生产中应该有更高效的方式
        var activeOrders = orderMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.studyback.smartfleet.entity.Order>()
                        .in("status", OrderStatus.CREATED.getValue(),
                                OrderStatus.CONFIRMED.getValue(),
                                OrderStatus.RUNNING.getValue())
        );

        for (var order : activeOrders) {
            if (order.getStartLat() != null && order.getStartLng() != null) {
                String orderGeoHash = geoHashService.encode(
                        order.getStartLat().doubleValue(),
                        order.getStartLng().doubleValue(),
                        GEOHASH_PRECISION
                );
                if (gridId.equals(orderGeoHash)) {
                    demandCount++;
                }
            }
        }

        // 计算供需比
        double supplyDemandRatio = demandCount == 0
                ? (availableCount > 0 ? Double.POSITIVE_INFINITY : 0.0)
                : (double) availableCount / demandCount;

        // 更新或插入网格运力记录
        GridDispatch existing = lambdaQuery()
                .eq(GridDispatch::getGeohash, gridId)
                .one();

        if (existing != null) {
            existing.setTotalVehicles(totalVehicles);
            existing.setAvailableCount(availableCount);
            existing.setDemandCount(demandCount);
            existing.setSupplyDemand(BigDecimal.valueOf(supplyDemandRatio == Double.POSITIVE_INFINITY ? 9999 : supplyDemandRatio));
            existing.setUpdateTime(LocalDateTime.now());
            updateById(existing);
        } else {
            GridDispatch newDispatch = new GridDispatch();
            newDispatch.setGeohash(gridId);
            newDispatch.setTotalVehicles(totalVehicles);
            newDispatch.setAvailableCount(availableCount);
            newDispatch.setDemandCount(demandCount);
            newDispatch.setSupplyDemand(BigDecimal.valueOf(supplyDemandRatio == Double.POSITIVE_INFINITY ? 9999 : supplyDemandRatio));
            newDispatch.setUpdateTime(LocalDateTime.now());
            save(newDispatch);
        }

        log.info("网格运力更新完成: gridId={}, total={}, available={}, demand={}, ratio={}",
                gridId, totalVehicles, availableCount, demandCount, supplyDemandRatio);
    }

    @Override
    public boolean isLowSupplyArea(String gridId) {
        double ratio = calculateSupplyDemandRatio(gridId);
        boolean isLow = ratio < LOW_SUPPLY_THRESHOLD;
        log.info("低供区域判断: gridId={}, ratio={}, isLowSupply={}", gridId, ratio, isLow);
        return isLow;
    }

    @Override
    public Map<String, Integer> getVehicleDistribution() {
        log.info("获取车辆分布统计");

        Map<String, Integer> distribution = new HashMap<>();

        List<Vehicle> allVehicles = vehicleMapper.selectList(null);

        for (Vehicle vehicle : allVehicles) {
            if (vehicle.getCurrentLat() == null || vehicle.getCurrentLng() == null) {
                continue;
            }
            String geoHash = geoHashService.encode(
                    vehicle.getCurrentLat().doubleValue(),
                    vehicle.getCurrentLng().doubleValue(),
                    GEOHASH_PRECISION
            );
            distribution.merge(geoHash, 1, Integer::sum);
        }

        log.info("车辆分布统计完成: gridCount={}", distribution.size());
        return distribution;
    }
}
