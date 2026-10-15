package com.jira.task.controller;

import com.jira.task.dto.TaskPatchRequest;
import com.jira.task.dto.TaskReorderRequest;
import com.jira.task.dto.TaskRequest;
import com.jira.task.entity.Task;
import com.jira.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务接口（M5：T1~T6）。
 *
 * 路径跟 Mock 的集合名 tasks 一致，前端只用换 host:port。
 * 返回全是裸 JSON（看板页要一次拿全部任务渲染多列，T1 不分页，文档 1.3 定的）。
 * tagId 参数：功能砍掉了（R6），前端传过来就直接无视，不会报错。
 */
@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** T1 GET /tasks?projectId=&name=&processorId=&typeId= —— 筛选全可选，裸数组 */
    @GetMapping("/tasks")
    public List<Task> list(@RequestParam(value = "projectId", required = false) Long projectId,
                           @RequestParam(value = "name", required = false) String name,
                           @RequestParam(value = "processorId", required = false) Long processorId,
                           @RequestParam(value = "typeId", required = false) Long typeId) {
        return taskService.list(projectId, name, processorId, typeId);
    }

    /** T6 POST /tasks/reorder —— 跨列拖拽，事务里挪列 + 重排 */
    @PostMapping("/tasks/reorder")
    public List<Task> reorder(@Valid @RequestBody TaskReorderRequest request) {
        return taskService.reorder(request);
    }

    /** T2 GET /tasks/{id} —— 单查，找不到给 404 */
    @GetMapping("/tasks/{id}")
    public Task detail(@PathVariable("id") Long id) {
        return taskService.detail(id);
    }

    /** T3 POST /tasks —— 前端传 { name, projectId, kanbanId }，返回带真实 id 的新对象 */
    @PostMapping("/tasks")
    public Task create(@Valid @RequestBody TaskRequest request) {
        return taskService.create(request);
    }

    /** T4 PATCH /tasks/{id} —— 部分更新，null/0 的字段不动 */
    @PatchMapping("/tasks/{id}")
    public Task patch(@PathVariable("id") Long id, @RequestBody TaskPatchRequest request) {
        return taskService.patch(id, request);
    }

    /** T5 DELETE /tasks/{id} —— 删除，返回被删的对象 */
    @DeleteMapping("/tasks/{id}")
    public Task delete(@PathVariable("id") Long id) {
        return taskService.delete(id);
    }
}
