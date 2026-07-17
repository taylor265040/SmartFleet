package com.studyback.smartfleet.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.studyback.smartfleet.entity.Order;

/**
 * 订单 Service 接口
 * <p>提供订单创建、确认、完成、取消等业务方法，以及一键租赁功能</p>
 */
public interface OrderService extends IService<Order> {

    /**
     * 创建订单
     * <p>流程：校验车辆 -> 预占锁 -> 更新车辆状态 -> 创建订单（乐观锁）</p>
     *
     * @param userId    用户ID
     * @param vehicleId 车辆ID
     * @param startLat  起点纬度
     * @param startLng  起点经度
     * @return 创建的订单
     */
    Order createOrder(Long userId, Long vehicleId, double startLat, double startLng);

    /**
     * 确认订单
     * <p>流程：校验订单状态 -> 校验持有者 -> 更新车辆为 RENTING -> 更新订单为 RUNNING</p>
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 确认后的订单
     */
    Order confirmOrder(Long orderId, Long userId);

    /**
     * 完成订单
     * <p>流程：校验订单状态 -> 更新车辆为 AVAILABLE -> 释放预占锁 -> 更新订单为 COMPLETED</p>
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 完成后的订单
     */
    Order completeOrder(Long orderId, Long userId);

    /**
     * 取消订单
     * <p>流程：校验订单状态 -> 释放预占锁 -> 更新车辆为 AVAILABLE -> 更新订单为 CANCELLED</p>
     *
     * @param orderId 订单ID
     * @param userId  用户ID
     * @return 取消后的订单
     */
    Order cancelOrder(Long orderId, Long userId);

    /**
     * 一键租赁
     * <p>流程：推荐车辆 -> 依次尝试预占锁 -> 创建订单</p>
     *
     * @param userId 用户ID
     * @param lat    用户纬度
     * @param lng    用户经度
     * @return 创建的订单
     */
    Order quickRent(Long userId, double lat, double lng);

    /**
     * 根据 ID 查询订单
     *
     * @param orderId 订单ID
     * @return 订单信息
     */
    Order getOrderById(Long orderId);
}
