package com.studyback.smartfleet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 网格运力实体类
 */
@Data
@TableName("tb_grid_dispatch")
public class GridDispatch {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** GeoHash编码 */
    private String geohash;

    /** 车辆总数 */
    private Integer totalVehicles;

    /** 可用车辆数 */
    private Integer availableCount;

    /** 需求订单数 */
    private Integer demandCount;

    /** 供需比 */
    private BigDecimal supplyDemand;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
