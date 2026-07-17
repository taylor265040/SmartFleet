package com.studyback.smartfleet.entity;

import com.studyback.smartfleet.exception.BusinessException;
import com.studyback.smartfleet.response.ResultCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 车辆状态机实现类
 * <p>基于状态转移矩阵实现车辆状态的检查和转移。
 * 状态转移矩阵定义了所有合法的 (from, event) -> to 映射关系。</p>
 *
 * <p>合法状态转移规则：
 * AVAILABLE  -> RESERVED    (RESERVE)
 * RESERVED   -> RENTING     (START_RENT)
 * RESERVED   -> AVAILABLE   (CANCEL_RESERVE)
 * RENTING    -> AVAILABLE   (END_RENT)
 * AVAILABLE  -> CHARGING    (START_CHARGE)
 * CHARGING   -> AVAILABLE   (END_CHARGE)
 * AVAILABLE  -> REPAIRING   (START_REPAIR)
 * REPAIRING  -> AVAILABLE   (END_REPAIR)</p>
 */
@Slf4j
public class VehicleStateMachineImpl implements VehicleStateMachine {

    /** 当前状态 */
    private VehicleStatus currentState;

    /**
     * 状态转移矩阵：key = (from, event), value = to
     * <p>使用嵌套 Map 结构：外层 key 为源状态，内层 key 为事件，value 为目标状态</p>
     */
    private static final Map<VehicleStatus, Map<StateEvent, VehicleStatus>> TRANSITION_MATRIX;

    static {
        Map<VehicleStatus, Map<StateEvent, VehicleStatus>> matrix = new HashMap<>();

        // AVAILABLE -> RESERVED (RESERVE)
        // AVAILABLE -> CHARGING (START_CHARGE)
        // AVAILABLE -> REPAIRING (START_REPAIR)
        Map<StateEvent, VehicleStatus> availableTransitions = new HashMap<>();
        availableTransitions.put(StateEvent.RESERVE, VehicleStatus.RESERVED);
        availableTransitions.put(StateEvent.START_CHARGE, VehicleStatus.CHARGING);
        availableTransitions.put(StateEvent.START_REPAIR, VehicleStatus.REPAIRING);
        matrix.put(VehicleStatus.AVAILABLE, availableTransitions);

        // RESERVED -> RENTING (START_RENT)
        // RESERVED -> AVAILABLE (CANCEL_RESERVE)
        Map<StateEvent, VehicleStatus> reservedTransitions = new HashMap<>();
        reservedTransitions.put(StateEvent.START_RENT, VehicleStatus.RENTING);
        reservedTransitions.put(StateEvent.CANCEL_RESERVE, VehicleStatus.AVAILABLE);
        matrix.put(VehicleStatus.RESERVED, reservedTransitions);

        // RENTING -> AVAILABLE (END_RENT)
        Map<StateEvent, VehicleStatus> rentingTransitions = new HashMap<>();
        rentingTransitions.put(StateEvent.END_RENT, VehicleStatus.AVAILABLE);
        matrix.put(VehicleStatus.RENTING, rentingTransitions);

        // CHARGING -> AVAILABLE (END_CHARGE)
        Map<StateEvent, VehicleStatus> chargingTransitions = new HashMap<>();
        chargingTransitions.put(StateEvent.END_CHARGE, VehicleStatus.AVAILABLE);
        matrix.put(VehicleStatus.CHARGING, chargingTransitions);

        // REPAIRING -> AVAILABLE (END_REPAIR)
        Map<StateEvent, VehicleStatus> repairingTransitions = new HashMap<>();
        repairingTransitions.put(StateEvent.END_REPAIR, VehicleStatus.AVAILABLE);
        matrix.put(VehicleStatus.REPAIRING, repairingTransitions);

        TRANSITION_MATRIX = Collections.unmodifiableMap(matrix);
    }

    /**
     * 构造函数
     *
     * @param initialState 初始状态
     * @throws IllegalArgumentException 初始状态为 null 时抛出
     */
    public VehicleStateMachineImpl(VehicleStatus initialState) {
        if (initialState == null) {
            throw new IllegalArgumentException("初始状态不能为 null");
        }
        this.currentState = initialState;
    }

    @Override
    public VehicleStatus getCurrentState() {
        return currentState;
    }

    @Override
    public void setCurrentState(VehicleStatus state) {
        if (state == null) {
            throw new IllegalArgumentException("状态不能设置为 null");
        }
        this.currentState = state;
    }

    @Override
    public boolean canTransition(StateEvent event) {
        if (event == null || currentState == null) {
            return false;
        }
        Map<StateEvent, VehicleStatus> transitions = TRANSITION_MATRIX.get(currentState);
        return transitions != null && transitions.containsKey(event);
    }

    @Override
    public VehicleStatus transition(StateEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("状态转移事件不能为 null");
        }
        if (currentState == null) {
            throw new IllegalStateException("当前状态不能为 null");
        }

        if (!canTransition(event)) {
            log.error("非法状态转移: from={}, event={}", currentState, event);
            throw new BusinessException(ResultCode.INVALID_STATE_TRANSITION,
                    String.format("非法状态转移: %s + %s", currentState, event));
        }

        VehicleStatus targetState = TRANSITION_MATRIX.get(currentState).get(event);
        log.info("状态转移: {} -> {} (event={})", currentState, targetState, event);
        this.currentState = targetState;
        return targetState;
    }

    /**
     * 获取状态转移矩阵（只读）
     *
     * @return 状态转移矩阵
     */
    public static Map<VehicleStatus, Map<StateEvent, VehicleStatus>> getTransitionMatrix() {
        return TRANSITION_MATRIX;
    }

    /**
     * 获取指定状态的所有合法转移
     *
     * @param status 源状态
     * @return 合法的 (event -> target) 映射，不存在则返回空 Map
     */
    public static Map<StateEvent, VehicleStatus> getTransitions(VehicleStatus status) {
        Map<StateEvent, VehicleStatus> transitions = TRANSITION_MATRIX.get(status);
        return transitions != null ? transitions : Collections.emptyMap();
    }
}
