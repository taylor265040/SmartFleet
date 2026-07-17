package com.studyback.smartfleet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 车辆状态变更记录实体
 * <p>持久化每次车辆状态变更的详细信息，用于审计和历史查询</p>
 */
@Data
@TableName("tb_state_change_record")
public class StateChangeRecord {

    /** 记录ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 车辆ID */
    private Long vehicleId;

    /** 变更前状态 */
    private String fromStatus;

    /** 变更后状态 */
    private String toStatus;

    /** 触发事件 */
    private String event;

    /** 变更时间 */
    private LocalDateTime changeTime;
}
