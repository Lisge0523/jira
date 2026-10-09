package com.jira.user.controller;

import com.jira.common.Result;
import com.jira.user.dto.UserVO;
import com.jira.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @GetMapping("/users")
    public Result<List<UserVO>> list(){
        return Result.ok(userService.listUsers());
    }
}
