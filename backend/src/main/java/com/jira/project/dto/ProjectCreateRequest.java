package com.jira.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreateRequest {
    @NotBlank(message = "请输入项目名")
    private String name;
    @NotBlank(message = "请输入部门名")
    private String organization;
    /** 负责人，可空 */
    private Long personId;
}
