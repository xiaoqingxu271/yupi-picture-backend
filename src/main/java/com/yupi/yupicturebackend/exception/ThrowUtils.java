package com.yupi.yupicturebackend.exception;

/**
 * 抛异常工具类
 *
 * @author chun0
 * @since 2025/11/7 16:30
 * @version 1.0
 */
public class ThrowUtils {

    /**
     * 当条件为true时，抛出runtimeException
     *
     * @param condition 条件
     * @param runtimeException  运行时异常
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 当条件为true时，抛出BusinessException
     *
     * @param condition  条件
     * @param errorCode  错误码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 当条件为true时，抛出BusinessException
     *
     * @param condition  条件
     * @param errorCode  错误码
     * @param message  错误信息
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
