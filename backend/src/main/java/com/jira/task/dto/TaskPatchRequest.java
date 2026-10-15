package com.jira.task.dto;

/**
 * 编辑任务的请求体（PATCH /tasks/:id，T4）。
 * 前端把整个 task 对象传上来，我们只认这四样：name / processorId / typeId / note，
 * 别的字段（kanbanId、projectId 这些）一律不动——挪列只能走 reorder，防止绕过排序。
 * 语义：字段为 null / 0 = 这次不改（前端 IdSelect 用 0 当「占位/未选」，Service 里会归一化）。
 */
public class TaskPatchRequest {

    private String name;
    private Long processorId;
    private Long typeId;
    private String note;

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
}
