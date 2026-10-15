package com.jira.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 新建任务的请求体（POST /tasks，T3）。
 * 前端必传 name / projectId / kanbanId，其余（经办人、类型、任务组、备注）可能顺手带过来，也存。
 */
public class TaskRequest {

    @NotBlank(message = "任务名不能为空")
    private String name;

    @NotNull(message = "项目 id 不能为空")
    private Long projectId;

    @NotNull(message = "看板列 id 不能为空")
    private Long kanbanId;

    private Long processorId;
    private Long typeId;
    private Long epicId;
    private String note;

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

    public Long getKanbanId() {
        return kanbanId;
    }

    public void setKanbanId(Long kanbanId) {
        this.kanbanId = kanbanId;
    }

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
