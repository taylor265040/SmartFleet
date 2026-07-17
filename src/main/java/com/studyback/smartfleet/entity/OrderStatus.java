package com.studyback.smartfleet.entity;

import lombok.Getter;

/**
 * 订单状态枚举
 * <p>状态流转：CREATED -> CONFIRMED -> RUNNING -> COMPLETED</p>
 * <p>取消流转：CREATED -> CANCELLED</p>
 */
@Getter
public enum OrderStatus {

    /** 已创建（预占中） */
    CREATED("CREATED", "已创建"),

    /** 已确认 */
    CONFIRMED("CONFIRMED", "已确认"),

    /** 运行中（车辆已取） */
    RUNNING("RUNNING", "运行中"),

    /** 已完成 */
    COMPLETED("COMPLETED", "已完成"),

    /** 已取消 */
    CANCELLED("CANCELLED", "已取消");

    /** 状态值 */
    private final String value;

    /** 状态描述 */
    private final String description;

    OrderStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 状态值
     * @return 对应的枚举，不存在返回 null
     */
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
