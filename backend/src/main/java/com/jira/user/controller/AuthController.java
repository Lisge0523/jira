
package com.jira.user.controller;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.common.Result;
import com.jira.user.dto.LoginRequest;
import com.jira.user.dto.RegisterRequest;
import com.jira.user.dto.UserVO;
import com.jira.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
/**
 * 认证接口。
 *
 * 待实现：
 *   - POST /login
 *   - POST /register
 *   - GET  /me
 */
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        UserVO user = userService.login(request);
        return Result.ok(Map.of("user", user));
    }
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        UserVO user = userService.register(request);
        return Result.ok(Map.of("user", user));
    }
    @GetMapping("/me")
    public Result<Map<String,Object>> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
    //1. 校验请求头格式：必须是 "Bearer xxx"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"未登录或登录已过期");
        }
        //去掉 "Bearer " 前缀，剩下的就是 token
        String token = authHeader.substring(7);
        //转交给server处理
        UserVO user = userService.me(token);
        return Result.ok(Map.of("user", user));
    }
}