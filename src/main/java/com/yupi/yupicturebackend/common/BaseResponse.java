package com.yupi.yupicturebackend.common;

import com.yupi.yupicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 基础响应类
 *
 * @author chun0
 * @since 2025/11/7 16:36
 * @version 1.0
 */
@Data
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private int code;
    private String message;
    private T data;

    public BaseResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public BaseResponse(int code, T data) {
        this(code, "", data);
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), errorCode.getMessage(), null);
    }

}
