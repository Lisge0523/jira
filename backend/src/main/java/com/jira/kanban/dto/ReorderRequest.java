package com.jira.kanban.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 拖拽排序的请求体（POST /kanbans/reorder）。
 * 前端契约：{ fromId, referenceId, type: "before" | "after" }。
 * referenceId 可能是 0 或不传，意思是「拖到最后一个」——前端 reorder.ts 就是这么算的。
 * Mock 后端碰到这种会直接 400，属于 Mock 的 bug，我们这边已经支持了。
 */
public class ReorderRequest {

    /** 要拖走的看板 id */
    @NotNull(message = "fromId 不能为空")
    private Long fromId;

    /** 拖到谁旁边；0 / null 表示直接拖到最后 */
    private Long referenceId;

    /** 放前面还是放后面 */
    @NotBlank(message = "type 不能为空")
    private String type;

    public Long getFromId() {
        return fromId;
    }

    public void setFromId(Long fromId) {
        this.fromId = fromId;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
