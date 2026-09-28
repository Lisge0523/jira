package com.jira.common;

/**
 * 错误码与提示语。
 *
 * 前端会把 message 直接展示给用户（ErrorBox），所以 message 用中文、面向用户。
 * 新增错误码时，请同时确认对应的 HTTP 状态码（见 GlobalExceptionHandler）。
 */
public enum ErrorCode {

    SUCCESS(0, "成功"),

    /** 参数错误：缺少必填、格式非法、取值范围不对 */
    BAD_REQUEST(400, "参数错误"),

    /** 未登录或 token 失效。前端收到 401 会清 token 并刷新页面 */
    UNAUTHORIZED(401, "未登录或登录已过期"),

    /** 已登录但无权访问该资源（例如不是该项目成员） */
    FORBIDDEN(403, "无权限访问"),

    /** 记录不存在 */
    NOT_FOUND(404, "记录不存在"),

    /** 数据冲突：用户名已存在、不能重复的数据等 */
    CONFLICT(409, "数据冲突"),

    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
