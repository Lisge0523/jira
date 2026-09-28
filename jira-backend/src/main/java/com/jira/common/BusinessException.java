package com.jira.common;

/**
 * 业务异常：Service 层校验不通过时抛出，由 GlobalExceptionHandler 统一转成响应。
 *
 * 用法：
 *   throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
