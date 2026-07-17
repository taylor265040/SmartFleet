package com.studyback.smartfleet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 定价规则实体类
 */
@Data
@TableName("tb_pricing_rule")
public class PricingRule {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** GeoHash编码（为空表示全局默认规则） */
    private String geohash;

    /** 基础价格 */
    private BigDecimal basePrice;

    /** 时段系数 */
    private BigDecimal timeCoefficient;

    /** 区域密度系数 */
    private BigDecimal densityCoefficient;

    /** 历史热度系数 */
    private BigDecimal historyCoefficient;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
