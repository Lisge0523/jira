package com.jira.user.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 sys_user 表。
 *
 * 注意：本类仅用于服务端内部传递数据；
 * 返回给前端的对象是 com.jira.user.dto.UserVO（不含密码）。
 */
@Data
public class User {

    private Long id;

    private String username;

    /** BCrypt 密码哈希，禁止返回给前端 */
    private String passwordHash;

    private String name;

    private String email;

    /** 职位 */
    private String title;

    private String organization;

    private LocalDateTime createdAt;
}