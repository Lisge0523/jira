package com.jira.epic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建任务组的请求体（POST /epics）。
 * 前端 create-epic.tsx 的表单【目前只有名称一个字段】，实际传上来的是 { name, projectId }，
 * 所以 start / end 只能是可选的——别照文档写「三样都必填」，那样创建功能直接 400。
 * 日期字段顺手也收着：以后前端补上日期选择器（B-4 前端侧的工作）就能直接用。
 */
public class EpicRequest {

    @NotBlank(message = "任务组名称不能为空")
    private String name;

    @NotNull(message = "项目 id 不能为空")
    private Long projectId;

    /** 开始时间（毫秒时间戳），可空 */
    private Long start;

    /** 结束时间（毫秒时间戳），可空 */
    private Long end;

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

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }
}
