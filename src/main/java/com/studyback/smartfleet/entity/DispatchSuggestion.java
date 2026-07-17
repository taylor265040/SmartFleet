package com.studyback.smartfleet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 运力调度建议实体类
 */
@Data
@TableName("tb_dispatch_suggestion")
public class DispatchSuggestion {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 目标网格GeoHash */
    private String targetGridId;

    /** 当前可用车辆数 */
    private Integer currentVehicleCount;

    /** 建议调度车辆数 */
    private Integer suggestedVehicleCount;

    /** 供需比 */
    private BigDecimal supplyDemandRatio;

    /** 调度优先级：HIGH/MEDIUM/LOW */
    private String priority;

    /** 调度原因 */
    private String reason;

    /** 创建时间 */
    private LocalDateTime createTime;
}
