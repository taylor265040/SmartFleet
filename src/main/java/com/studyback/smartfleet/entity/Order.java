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
 * 订单实体类
 */
@Data
@TableName("tb_order")
public class Order {

    /** 订单ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 车辆ID */
    private Long vehicleId;

    /** 状态：CREATED/RUNNING/COMPLETED/CANCELLED */
    private String status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 总金额 */
    private BigDecimal totalAmount;

    /** 起点纬度 */
    private BigDecimal startLat;

    /** 起点经度 */
    private BigDecimal startLng;

    /** 终点纬度 */
    private BigDecimal endLat;

    /** 终点经度 */
    private BigDecimal endLng;

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
