package com.studyback.smartfleet.entity;

import java.util.Map;

/**
 * 状态转移校验器
 * <p>提供静态方法校验两个状态之间的转移是否合法，基于状态转移矩阵</p>
 */
public class StateTransitionValidator {

    private StateTransitionValidator() {
        // 工具类，禁止实例化
    }

    /**
     * 校验从 from 状态到 to 状态的转移是否合法
     * <p>遍历 from 状态的所有合法转移，检查是否存在到达 to 状态的路径</p>
     *
     * @param from 源状态
     * @param to   目标状态
     * @return true=合法转移，false=非法转移
     */
    public static boolean isValidTransition(VehicleStatus from, VehicleStatus to) {
        if (from == null || to == null) {
            return false;
        }
        Map<StateEvent, VehicleStatus> transitions = VehicleStateMachineImpl.getTransitions(from);
        return transitions.containsValue(to);
    }

    /**
     * 校验指定事件在当前状态下是否合法
     *
     * @param currentState 当前状态
     * @param event        触发事件
     * @return true=合法，false=非法
     */
    public static boolean isValidEvent(VehicleStatus currentState, StateEvent event) {
        if (currentState == null || event == null) {
            return false;
        }
        Map<StateEvent, VehicleStatus> transitions = VehicleStateMachineImpl.getTransitions(currentState);
        return transitions.containsKey(event);
    }
}
