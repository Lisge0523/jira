
package com.jira.user.controller;

import com.jira.common.Result;
import com.jira.user.dto.LoginRequest;
import com.jira.user.dto.UserVO;
import com.jira.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
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
}