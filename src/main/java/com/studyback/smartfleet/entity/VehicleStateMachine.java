package com.studyback.smartfleet.entity;

import com.studyback.smartfleet.exception.BusinessException;

/**
 * 车辆状态机接口
 * <p>定义车辆状态机的核心操作：状态转移检查和执行</p>
 */
public interface VehicleStateMachine {

    /**
     * 获取当前状态
     *
     * @return 当前车辆状态
     */
    VehicleStatus getCurrentState();

    /**
     * 设置当前状态
     *
     * @param state 目标状态
     */
    void setCurrentState(VehicleStatus state);

    /**
     * 检查是否可以执行指定事件触发的状态转移
     *
     * @param event 状态转移事件
     * @return true=可以转移，false=不可以转移
     */
    boolean canTransition(StateEvent event);

    /**
     * 执行状态转移
     * <p>如果转移合法，返回目标状态；否则抛出 BusinessException</p>
     *
     * @param event 状态转移事件
     * @return 目标状态
     * @throws BusinessException 非法状态转移时抛出
     */
    VehicleStatus transition(StateEvent event) throws BusinessException;
}
