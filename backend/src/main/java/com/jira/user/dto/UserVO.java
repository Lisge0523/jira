package com.jira.user.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVO {
    private Long id;
    private String name;
    private String email;
    private String title;
    private String organization;

// 注意：登录凭证走 HttpOnly Cookie，这里不返回 token
}
