package com.studyback.smartfleet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 车辆实体类
 */
@Data
@TableName("tb_vehicle")
public class Vehicle {

    /** 车辆ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 车牌号 */
    private String plateNo;

    /** 车型 */
    private String model;

    /** 当前纬度 */
    private BigDecimal currentLat;

    /** 当前经度 */
    private BigDecimal currentLng;

    /** 电量百分比 */
    private Integer batteryLevel;

    /** 健康度评分 */
    private Integer healthScore;

    /** 状态：AVAILABLE/RESERVED/RENTING/CHARGING/REPAIRING */
    private String status;

    /** 空闲开始时间 */
    private LocalDateTime idleStart;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0-未删除，1-已删除 */
    @TableLogic
    private Integer deleted;
}
