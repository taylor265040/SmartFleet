package com.studyback.smartfleet.entity;

/**
 * 评分维度枚举
 * <p>定义多维加权评分引擎的评分维度</p>
 */
public enum ScoringDimension {

    /** 距离维度：车辆到用户的距离 */
    DISTANCE,

    /** 电量维度：车辆当前电量百分比 */
    BATTERY,

    /** 空闲时长维度：车辆空闲时间 */
    IDLE_TIME,

    /** 健康度维度：车辆健康评分 */
    HEALTH
}
