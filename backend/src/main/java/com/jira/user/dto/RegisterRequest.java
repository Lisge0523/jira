package com.jira.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest
{
    @NotBlank(message = "请输入用户名")
    @Size(min = 2,max = 50,message = "用户名长度需在 2-50 之间")
    private String username;

    @NotBlank(message = "请输入密码")
    @Size(min = 6,max = 50,message = "密码长度需在 6-50 之间")
    private String password;
}
