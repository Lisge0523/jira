
package com.jira.user.controller;

import com.jira.common.Result;
import com.jira.security.TokenCookie;
import com.jira.user.dto.AuthResult;
import com.jira.user.dto.LoginRequest;
import com.jira.user.dto.RegisterRequest;
import com.jira.user.dto.UserVO;
import com.jira.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
/**
 * 认证接口。
 *
 * 登录凭证走 HttpOnly Cookie，响应体里不再返回 token。
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final TokenCookie tokenCookie;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request,
                                             HttpServletResponse response) {
        AuthResult result = userService.login(request);
        // token 只写进 HttpOnly Cookie，不出现在响应体里
        tokenCookie.write(response, result.token());
        return Result.ok(Map.of("user", result.user()));
    }
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request,
                                                HttpServletResponse response) {
        AuthResult result = userService.register(request);
        tokenCookie.write(response, result.token());
        return Result.ok(Map.of("user", result.user()));
    }
    @GetMapping("/me")
    public Result<Map<String,Object>> me(@RequestAttribute("userId") Long userId) {
        UserVO user = userService.me(userId);
        return Result.ok(Map.of("user", user));
    }
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        // HttpOnly Cookie 前端删不掉，必须由服务端用 Max-Age=0 清理
        tokenCookie.clear(response);
        return Result.ok();
    }
}
