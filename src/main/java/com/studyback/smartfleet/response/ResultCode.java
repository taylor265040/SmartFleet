package com.studyback.smartfleet.response;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未认证 */
    UNAUTHORIZED(401, "请先登录"),

    /** 无权限 */
    FORBIDDEN(403, "无权操作"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 资源冲突 */
    CONFLICT(409, "资源冲突"),

    /** 资源已过期 */
    GONE(410, "资源已过期"),

    /** 请求过于频繁 */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器内部错误"),

    /** 业务异常 */
    BUSINESS_ERROR(1000, "业务异常"),

    /** 车辆不可用 */
    VEHICLE_NOT_AVAILABLE(1001, "该车辆当前不可租用"),

    /** 无可用 vehicles */
    NO_VEHICLE(1002, "附近暂无可用车辆"),

    /** 用户已有进行中的订单 */
    ORDER_IN_PROGRESS(1003, "您有一笔进行中的订单，请先完成"),

    /** 预占锁获取失败 */
    LOCK_FAILED(1004, "该车辆正在被其他用户选择中"),

    /** 订单状态不允许操作 */
    ORDER_STATUS_INVALID(1005, "订单状态不允许此操作"),

    /** 用户名已存在 */
    USERNAME_EXISTS(1006, "用户名已存在"),

    /** Token 无效或已过期 */
    TOKEN_INVALID(1007, "Token 无效或已过期"),

    /** 用户名或密码错误 */
    LOGIN_FAILED(1008, "用户名或密码错误"),

    /** 非法状态转移 */
    INVALID_STATE_TRANSITION(1009, "非法状态转移");

    /** 状态码 */
    private final int code;

    /** 消息 */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
