package com.studyback.smartfleet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.studyback.smartfleet.entity.Order;
import com.studyback.smartfleet.response.ApiResponse;
import com.studyback.smartfleet.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单 Controller
 * <p>提供订单创建、确认、完成、取消、一键租赁等接口</p>
 */
@Tag(name = "订单管理", description = "订单相关接口")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 创建订单
     */
    @Operation(summary = "创建订单")
    @PostMapping
    public ApiResponse<Order> createOrder(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "车辆ID") @RequestParam Long vehicleId,
            @Parameter(description = "起点纬度") @RequestParam double startLat,
            @Parameter(description = "起点经度") @RequestParam double startLng) {
        Order order = orderService.createOrder(userId, vehicleId, startLat, startLng);
        return ApiResponse.success(order);
    }

    /**
     * 确认订单
     */
    @Operation(summary = "确认订单")
    @PutMapping("/{id}/confirm")
    public ApiResponse<Order> confirmOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        Order order = orderService.confirmOrder(id, userId);
        return ApiResponse.success(order);
    }

    /**
     * 完成订单
     */
    @Operation(summary = "完成订单")
    @PutMapping("/{id}/complete")
    public ApiResponse<Order> completeOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        Order order = orderService.completeOrder(id, userId);
        return ApiResponse.success(order);
    }

    /**
     * 取消订单
     */
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public ApiResponse<Order> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        Order order = orderService.cancelOrder(id, userId);
        return ApiResponse.success(order);
    }

    /**
     * 一键租赁
     */
    @Operation(summary = "一键租赁")
    @PostMapping("/quick-rent")
    public ApiResponse<Order> quickRent(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "用户纬度") @RequestParam double lat,
            @Parameter(description = "用户经度") @RequestParam double lng) {
        Order order = orderService.quickRent(userId, lat, lng);
        return ApiResponse.success(order);
    }

    /**
     * 根据ID查询订单
     */
    @Operation(summary = "根据ID查询订单")
    @GetMapping("/{id}")
    public ApiResponse<Order> getById(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        return ApiResponse.success(order);
    }

    /**
     * 查询订单列表
     */
    @Operation(summary = "查询订单列表")
    @GetMapping
    public ApiResponse<List<Order>> list(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        List<Order> orders = orderService.list(wrapper);
        return ApiResponse.success(orders);
    }
}
