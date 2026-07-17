package com.studyback.smartfleet.entity;

import lombok.Getter;

/**
 * 车辆状态枚举
 * <p>定义车辆生命周期中的所有状态：
 * AVAILABLE -> RESERVED -> RENTING -> AVAILABLE（租赁流程）
 * AVAILABLE -> CHARGING -> AVAILABLE（充电流程）
 * AVAILABLE -> REPAIRING -> AVAILABLE（维修流程）</p>
 */
@Getter
public enum VehicleStatus {

    /** 空闲可用 */
    AVAILABLE("AVAILABLE", "空闲可用"),

    /** 已预占 */
    RESERVED("RESERVED", "已预占"),

    /** 租赁中 */
    RENTING("RENTING", "租赁中"),

    /** 充电中 */
    CHARGING("CHARGING", "充电中"),

    /** 维修中 */
    REPAIRING("REPAIRING", "维修中");

    /** 状态值 */
    private final String value;

    /** 状态描述 */
    private final String description;

    VehicleStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 状态值
     * @return 对应的枚举，不存在返回 null
     */
    public static VehicleStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (VehicleStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }
}
