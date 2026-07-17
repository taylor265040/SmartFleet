package com.studyback.smartfleet.entity;

import lombok.Getter;

/**
 * 状态转移事件枚举
 * <p>定义触发车辆状态转移的所有事件：
 * RESERVE: 预占车辆（AVAILABLE -> RESERVED）
 * CANCEL_RESERVE: 取消预占（RESERVED -> AVAILABLE）
 * START_RENT: 开始租赁（RESERVED -> RENTING）
 * END_RENT: 结束租赁（RENTING -> AVAILABLE）
 * START_CHARGE: 开始充电（AVAILABLE -> CHARGING）
 * END_CHARGE: 结束充电（CHARGING -> AVAILABLE）
 * START_REPAIR: 开始维修（AVAILABLE -> REPAIRING）
 * END_REPAIR: 结束维修（REPAIRING -> AVAILABLE）</p>
 */
@Getter
public enum StateEvent {

    /** 预占车辆 */
    RESERVE("RESERVE", "预占车辆"),

    /** 取消预占 */
    CANCEL_RESERVE("CANCEL_RESERVE", "取消预占"),

    /** 开始租赁 */
    START_RENT("START_RENT", "开始租赁"),

    /** 结束租赁 */
    END_RENT("END_RENT", "结束租赁"),

    /** 开始充电 */
    START_CHARGE("START_CHARGE", "开始充电"),

    /** 结束充电 */
    END_CHARGE("END_CHARGE", "结束充电"),

    /** 开始维修 */
    START_REPAIR("START_REPAIR", "开始维修"),

    /** 结束维修 */
    END_REPAIR("END_REPAIR", "结束维修");

    /** 事件值 */
    private final String value;

    /** 事件描述 */
    private final String description;

    StateEvent(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据值查找枚举
     *
     * @param value 事件值
     * @return 对应的枚举，不存在返回 null
     */
    public static StateEvent fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (StateEvent event : values()) {
            if (event.value.equals(value)) {
                return event;
            }
        }
        return null;
    }
}
