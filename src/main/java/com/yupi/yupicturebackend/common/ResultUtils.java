package com.yupi.yupicturebackend.common;

import com.yupi.yupicturebackend.exception.ErrorCode;

/**
* @author chun0
* @since 2025/11/7 16:39
* @version 1.0
*/    
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 响应数据
     * @param <T>  响应数据类型
     * @return 成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "ok", data);
    }

    /**
     * 失败
     *
     * @param errorCode 错误码
     * @param <T>       响应数据类型
     * @return 错误响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    /**
     * 失败
     *
     * @param code    错误码
     * @param message 错误信息
     * @param <T>     响应数据类型
     * @return 错误响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

     /**
     * 失败
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param <T>       响应数据类型
     * @return 错误响应
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), message, null);
    }
}
