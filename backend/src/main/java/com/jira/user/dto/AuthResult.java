package com.jira.user.dto;

/**
 * 登录/注册的服务层返回：user 给前端，token 只用来写 Cookie。
 *
 * 注意：token 绝不能出现在返回给前端的 JSON 里，
 * 否则前端仍能从响应体读到它，HttpOnly Cookie 就失去意义了。
 */
public record AuthResult(UserVO user, String token) {
}
