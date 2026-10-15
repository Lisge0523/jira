package com.jira.task.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * 任务卡片（M5）。
 * 对应 task 表。前端的 Task 类型见 types/task.ts：{ id, name, processorId, projectId, epicId, kanbanId, typeId, note }。
 * sortOrder 还是老规矩：内部记顺序用，不下发，前端按数组顺序渲染列内的卡片。
 */
public class Task {

    private Long id;
    private String name;
    /** 经办人，可空 */
    private Long processorId;
    private Long projectId;
    /** 所属任务组，可空；删任务组时被数据库置回 null（ON DELETE SET NULL） */
    private Long epicId;
    private Long kanbanId;
    /** 任务类型：1 Bug / 2 Task，可空 */
    private Long typeId;
    private String note;

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

    public Long getProcessorId() {
        return processorId;
    }

    public void setProcessorId(Long processorId) {
        this.processorId = processorId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getEpicId() {
        return epicId;
    }

    public void setEpicId(Long epicId) {
        this.epicId = epicId;
    }

    public Long getKanbanId() {
        return kanbanId;
    }

    public void setKanbanId(Long kanbanId) {
        this.kanbanId = kanbanId;
    }

    public Long getTypeId() {
        return typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
