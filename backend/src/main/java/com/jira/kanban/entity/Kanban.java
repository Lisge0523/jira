package com.jira.kanban.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 看板的一列（M4）。
 * 对应 kanban 表，前端的 Kanban 类型只有 { id, name, projectId } 三个字段。
 * sortOrder 是我们内部记顺序用的，不下发（@JsonIgnore），前端看数组顺序渲染列就够了。
 */
public class Kanban {

    private Long id;
    private String name;
    private Long projectId;

    @JsonIgnore
    private Integer sortOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
