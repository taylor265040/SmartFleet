package com.studyback.smartfleet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.studyback.smartfleet.entity.Order;
import com.studyback.smartfleet.entity.OrderStatus;
import com.studyback.smartfleet.entity.Vehicle;
import com.studyback.smartfleet.entity.VehicleRecommendation;
import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.mapper.OrderMapper;
import com.studyback.smartfleet.response.ResultCode;
import com.studyback.smartfleet.service.OrderService;
import com.studyback.smartfleet.service.RedisLockService;
import com.studyback.smartfleet.service.RetryService;
import com.studyback.smartfleet.service.VehicleScoringService;
import com.studyback.smartfleet.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ConcurrentModificationException;

/**
 * 订单 Service 实现类
 * <p>实现高并发租赁一致性控制：Redis 预占锁 + MySQL 乐观锁 + 自旋重试</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final RedisLockService redisLockService;
    private final RetryService retryService;
    private final VehicleService vehicleService;
    private final VehicleScoringService vehicleScoringService;

    /** 预占锁过期时间（秒）：5 分钟 */
    private static final int LOCK_SECONDS = 300;

    /** 自旋重试最大次数 */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /** 自旋重试间隔（毫秒） */
    private static final long RETRY_INTERVAL_MS = 100;

    /** 推荐车辆数量 */
    private static final int RECOMMEND_LIMIT = 5;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long vehicleId, double startLat, double startLng) {
        // 1. 参数校验
        if (userId == null || vehicleId == null) {
            throw new IllegalArgumentException("用户ID和车辆ID不能为空");
        }

        log.info("创建订单: userId={}, vehicleId={}, lat={}, lng={}", userId, vehicleId, startLat, startLng);

        // 2. 校验车辆是否存在且可用
        Vehicle vehicle = vehicleService.getById(vehicleId);
        if (vehicle == null) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE, "车辆不存在: " + vehicleId);
        }
        if (!"AVAILABLE".equals(vehicle.getStatus())) {
            throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE,
                    "车辆当前不可用，状态: " + vehicle.getStatus());
        }

        // 3. 获取预占锁（Redis Lua 脚本保证原子性）
        boolean locked = redisLockService.tryLock(vehicleId, userId, LOCK_SECONDS);
        if (!locked) {
            throw new BusinessException(ResultCode.LOCK_FAILED);
        }

        try {
            // 4. 使用乐观锁 + 自旋重试更新车辆状态为 RESERVED
            boolean vehicleUpdated = retryService.executeWithRetry(() -> {
                // 重新读取车辆信息（获取最新 version）
                Vehicle latestVehicle = vehicleService.getById(vehicleId);
                if (latestVehicle == null || !"AVAILABLE".equals(latestVehicle.getStatus())) {
                    throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE,
                            "车辆状态已变更: " + (latestVehicle == null ? "null" : latestVehicle.getStatus()));
                }
                latestVehicle.setStatus("RESERVED");
                latestVehicle.setIdleStart(null);
                boolean updated = vehicleService.updateById(latestVehicle);
                if (!updated) {
                    throw new ConcurrentModificationException("更新车辆状态失败，乐观锁冲突");
                }
                return updated;
            }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

            if (!vehicleUpdated) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新车辆状态失败");
            }

            // 5. 创建订单
            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setVehicleId(vehicleId);
            order.setStatus(OrderStatus.CREATED.getValue());
            order.setStartTime(LocalDateTime.now());
            order.setStartLat(BigDecimal.valueOf(startLat));
            order.setStartLng(BigDecimal.valueOf(startLng));
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            order.setVersion(0);
            order.setDeleted(0);

            boolean saved = this.save(order);
            if (!saved) {
                throw new BusinessException(ResultCode.INTERNAL_ERROR, "创建订单失败");
            }

            log.info("订单创建成功: orderId={}, orderNo={}, vehicleId={}", order.getId(), order.getOrderNo(), vehicleId);
            return order;

        } catch (Exception e) {
            // 创建失败时释放锁
            log.error("创建订单异常，释放预占锁: vehicleId={}, userId={}", vehicleId, userId, e);
            redisLockService.releaseLock(vehicleId, userId);
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order confirmOrder(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }

        log.info("确认订单: orderId={}, userId={}", orderId, userId);

        // 1. 校验订单是否存在
        Order order = getOrderById(orderId);

        // 2. 校验订单所有者
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }

        // 3. 校验订单状态：只有 CREATED 状态可以确认
        if (!OrderStatus.CREATED.getValue().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "订单状态不允许确认，当前状态: " + order.getStatus());
        }

        // 4. 校验预占锁是否仍有效
        Long lockOwner = redisLockService.getLockOwner(order.getVehicleId());
        if (lockOwner == null || !userId.equals(lockOwner)) {
            // 预占锁已过期或被释放，自动取消订单
            log.warn("预占锁已失效，自动取消订单: orderId={}, vehicleId={}", orderId, order.getVehicleId());
            cancelOrderInternal(order, "预占锁已过期");
            throw new BusinessException(ResultCode.LOCK_FAILED, "预占锁已过期，订单已自动取消");
        }

        // 5. 使用乐观锁 + 自旋重试更新订单状态为 CONFIRMED
        retryService.executeWithRetry(() -> {
            Order latestOrder = this.getById(orderId);
            if (!OrderStatus.CREATED.getValue().equals(latestOrder.getStatus())) {
                throw new ConcurrentModificationException("订单状态已变更: " + latestOrder.getStatus());
            }
            latestOrder.setStatus(OrderStatus.CONFIRMED.getValue());
            latestOrder.setUpdateTime(LocalDateTime.now());
            boolean updated = this.updateById(latestOrder);
            if (!updated) {
                throw new ConcurrentModificationException("更新订单状态失败，乐观锁冲突");
            }
            return latestOrder;
        }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

        // 6. 使用乐观锁 + 自旋重试更新车辆状态为 RENTING
        boolean vehicleUpdated = retryService.executeWithRetry(() -> {
            Vehicle vehicle = vehicleService.getById(order.getVehicleId());
            if (vehicle == null) {
                throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE, "车辆不存在");
            }
            if (!"RESERVED".equals(vehicle.getStatus())) {
                // 车辆状态已变更，自动取消订单
                log.warn("车辆状态已变更，自动取消订单: vehicleId={}, status={}", vehicle.getId(), vehicle.getStatus());
                cancelOrderInternal(order, "车辆状态已变更: " + vehicle.getStatus());
                throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE,
                        "车辆状态已变更，订单已自动取消");
            }
            vehicle.setStatus("RENTING");
            boolean updated = vehicleService.updateById(vehicle);
            if (!updated) {
                throw new ConcurrentModificationException("更新车辆状态失败，乐观锁冲突");
            }
            return updated;
        }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

        if (!vehicleUpdated) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新车辆状态失败");
        }

        // 7. 使用乐观锁 + 自旋重试更新订单状态为 RUNNING
        Order confirmedOrder = retryService.executeWithRetry(() -> {
            Order latestOrder = this.getById(orderId);
            if (!OrderStatus.CONFIRMED.getValue().equals(latestOrder.getStatus())) {
                throw new ConcurrentModificationException("订单状态已变更: " + latestOrder.getStatus());
            }
            latestOrder.setStatus(OrderStatus.RUNNING.getValue());
            latestOrder.setUpdateTime(LocalDateTime.now());
            boolean updated = this.updateById(latestOrder);
            if (!updated) {
                throw new ConcurrentModificationException("更新订单状态失败，乐观锁冲突");
            }
            return latestOrder;
        }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

        log.info("订单确认成功: orderId={}, status=RUNNING", orderId);
        return confirmedOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order completeOrder(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }

        log.info("完成订单: orderId={}, userId={}", orderId, userId);

        // 1. 校验订单
        Order order = getOrderById(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }
        if (!OrderStatus.RUNNING.getValue().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "订单状态不允许完成，当前状态: " + order.getStatus());
        }

        // 2. 使用乐观锁更新车辆状态为 AVAILABLE
        boolean vehicleUpdated = retryService.executeWithRetry(() -> {
            Vehicle vehicle = vehicleService.getById(order.getVehicleId());
            if (vehicle == null) {
                throw new BusinessException(ResultCode.VEHICLE_NOT_AVAILABLE, "车辆不存在");
            }
            vehicle.setStatus("AVAILABLE");
            vehicle.setIdleStart(LocalDateTime.now());
            boolean updated = vehicleService.updateById(vehicle);
            if (!updated) {
                throw new ConcurrentModificationException("更新车辆状态失败，乐观锁冲突");
            }
            return updated;
        }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

        if (!vehicleUpdated) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "更新车辆状态失败");
        }

        // 3. 释放预占锁
        redisLockService.releaseLock(order.getVehicleId(), userId);

        // 4. 使用乐观锁更新订单状态为 COMPLETED
        Order completedOrder = retryService.executeWithRetry(() -> {
            Order latestOrder = this.getById(orderId);
            if (!OrderStatus.RUNNING.getValue().equals(latestOrder.getStatus())) {
                throw new ConcurrentModificationException("订单状态已变更: " + latestOrder.getStatus());
            }
            latestOrder.setStatus(OrderStatus.COMPLETED.getValue());
            latestOrder.setEndTime(LocalDateTime.now());
            latestOrder.setUpdateTime(LocalDateTime.now());
            boolean updated = this.updateById(latestOrder);
            if (!updated) {
                throw new ConcurrentModificationException("更新订单状态失败，乐观锁冲突");
            }
            return latestOrder;
        }, MAX_RETRY_ATTEMPTS, RETRY_INTERVAL_MS);

        log.info("订单完成: orderId={}, vehicleId={}", orderId, order.getVehicleId());
        return completedOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order cancelOrder(Long orderId, Long userId) {
        if (orderId == null || userId == null) {
            throw new IllegalArgumentException("订单ID和用户ID不能为空");
        }

        log.info("取消订单: orderId={}, userId={}", orderId, userId);

        // 1. 校验订单
        Order order = getOrderById(orderId);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作此订单");
        }

        // 2. 只有 CREATED 状态可以直接取消
        if (!OrderStatus.CREATED.getValue().equals(order.getStatus())) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID,
                    "订单状态不允许取消，当前状态: " + order.getStatus());
        }

        // 3. 释放车辆
        releaseVehicleAndLock(order, "用户主动取消");

        log.info("订单取消成功: orderId={}", orderId);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order quickRent(Long userId, double lat, double lng) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.info("一键租赁: userId={}, lat={}, lng={}", userId, lat, lng);

        // 1. 调用 M3 的车辆推荐服务获取推荐车辆列表
        List<VehicleRecommendation> recommendations = vehicleScoringService.recommendForList(lat, lng, RECOMMEND_LIMIT);
        if (recommendations == null || recommendations.isEmpty()) {
            throw new BusinessException(ResultCode.NO_VEHICLE);
        }

        // 2. 依次尝试预占锁并创建订单
        for (VehicleRecommendation rec : recommendations) {
            Long vehicleId = rec.getVehicleId();
            try {
                log.info("尝试预占推荐车辆: vehicleId={}, score={}", vehicleId, rec.getScore());
                Order order = createOrder(userId, vehicleId, lat, lng);
                log.info("一键租赁成功: orderId={}, vehicleId={}", order.getId(), vehicleId);
                return order;
            } catch (BusinessException e) {
                // 预占失败（车辆被抢、不可用等），继续尝试下一辆
                log.warn("预占车辆失败，尝试下一辆: vehicleId={}, reason={}", vehicleId, e.getMessage());
            }
        }

        // 所有推荐车辆均预占失败
        throw new BusinessException(ResultCode.NO_VEHICLE, "所有推荐车辆均已被占用，请稍后重试");
    }

    @Override
    public Order getOrderById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        Order order = this.getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在: " + orderId);
        }
        return order;
    }

    /**
     * 生成订单号（UUID 去横线）
     *
     * @return 订单号
     */
    private String generateOrderNo() {
        return "SF" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 释放车辆并释放预占锁（内部方法，用于取消订单等场景）
     *
     * @param order  订单
     * @param reason 取消原因（用于日志）
     */
    private void releaseVehicleAndLock(Order order, String reason) {
        Long vehicleId = order.getVehicleId();

        // 释放车辆状态为 AVAILABLE
        try {
            Vehicle vehicle = vehicleService.getById(vehicleId);
            if (vehicle != null && ("RESERVED".equals(vehicle.getStatus()) || "RENTING".equals(vehicle.getStatus()))) {
                vehicle.setStatus("AVAILABLE");
                vehicle.setIdleStart(LocalDateTime.now());
                vehicleService.updateById(vehicle);
                log.info("车辆已释放: vehicleId={}, reason={}", vehicleId, reason);
            }
        } catch (Exception e) {
            log.error("释放车辆状态失败: vehicleId={}", vehicleId, e);
        }

        // 释放预占锁
        try {
            redisLockService.releaseLock(vehicleId, order.getUserId());
        } catch (Exception e) {
            log.error("释放预占锁失败: vehicleId={}", vehicleId, e);
        }

        // 使用乐观锁更新订单状态为 CANCELLED
        try {
            Order latestOrder = this.getById(order.getId());
            if (latestOrder != null && !OrderStatus.CANCELLED.getValue().equals(latestOrder.getStatus())) {
                latestOrder.setStatus(OrderStatus.CANCELLED.getValue());
                latestOrder.setEndTime(LocalDateTime.now());
                latestOrder.setUpdateTime(LocalDateTime.now());
                this.updateById(latestOrder);
                log.info("订单已取消: orderId={}, reason={}", order.getId(), reason);
            }
        } catch (Exception e) {
            log.error("更新订单状态失败: orderId={}", order.getId(), e);
        }
    }

    /**
     * 内部取消订单方法（用于预占锁过期等自动取消场景）
     *
     * @param order  订单
     * @param reason 取消原因
     */
    private void cancelOrderInternal(Order order, String reason) {
        releaseVehicleAndLock(order, reason);
    }
}
