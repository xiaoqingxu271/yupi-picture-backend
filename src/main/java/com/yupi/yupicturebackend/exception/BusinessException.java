package com.yupi.yupicturebackend.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author chun0
 * @since 2025/11/7 16:30
 * @version 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        this(errorCode.getCode(), message);
    }

}
