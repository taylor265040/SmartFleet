package com.studyback.smartfleet.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 状态转移定义
 * <p>描述一次合法的状态转移：from + event -> to</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateTransition {

    /** 源状态 */
    private VehicleStatus from;

    /** 触发事件 */
    private StateEvent event;

    /** 目标状态 */
    private VehicleStatus to;
}
