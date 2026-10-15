package com.jira.task.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 任务跨列拖拽的请求体（POST /tasks/reorder，T6）。
 * 前端契约（utils/kanban.ts SortProps）：{ fromId, referenceId, type, fromKanbanId, toKanbanId }。
 *
 * 两个从前端代码里核实来的实际行为，服务端都兼容了：
 *   1. 前端只在「跨列」拖时才发这个请求（同列拖完直接 return 不发）；
 *   2. 拖到空列时 referenceId 是 undefined，JSON 里这个字段整个消失——
 *      服务端按「追加到目标列末尾」处理，跟前端 reorder.ts 的 !referenceId 分支一致。
 */
public class TaskReorderRequest {

    @NotNull(message = "fromId 不能为空")
    private Long fromId;

    /** 拖到哪个任务旁边；null / 0 = 追加到列尾 */
    private Long referenceId;

    /** 放参照前面还是后面 */
    private String type;

    /** 从哪列拖出来的（前端会传，服务端以数据库实际值为准，不信任这个） */
    private Long fromKanbanId;

    /** 拖进哪列 */
    @NotNull(message = "toKanbanId 不能为空")
    private Long toKanbanId;

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

    public Long getFromKanbanId() {
        return fromKanbanId;
    }

    public void setFromKanbanId(Long fromKanbanId) {
        this.fromKanbanId = fromKanbanId;
    }

    public Long getToKanbanId() {
        return toKanbanId;
    }

    public void setToKanbanId(Long toKanbanId) {
        this.toKanbanId = toKanbanId;
    }
}
