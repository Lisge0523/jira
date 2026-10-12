package com.jira.project.dto;

import lombok.Data;

/**
 * 项目部分更新：所有字段都是可选的，
 * 只有不为 null 的字段才会被更新（null = 不改这个字段）。
 */
@Data
public class ProjectUpdateRequest {
    private String name;
    private String organization;
    private Long personId;
    private Boolean pin;

}
