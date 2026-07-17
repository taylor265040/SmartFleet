package com.studyback.smartfleet.exception;

import com.studyback.smartfleet.response.ResultCode;
import lombok.Getter;

/**
 * 业务异常类
 * <p>用于业务逻辑中的异常抛出，由全局异常处理器统一捕获处理</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private final int code;

    /** 结果码 */
    private final ResultCode resultCode;

    /**
     * 使用 ResultCode 构造
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
    }

    /**
     * 使用 ResultCode 和自定义消息构造
     */
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.resultCode = resultCode;
    }

    /**
     * 使用自定义状态码和消息构造
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.resultCode = null;
    }
}
