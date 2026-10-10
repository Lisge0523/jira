package com.jira.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 新建看板的请求体（POST /kanbans）。前端就传 { name, projectId }，俩都必填。
 */
public class KanbanRequest {

    @NotBlank(message = "看板名称不能为空")
    private String name;

    @NotNull(message = "项目 id 不能为空")
    private Long projectId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }
}
