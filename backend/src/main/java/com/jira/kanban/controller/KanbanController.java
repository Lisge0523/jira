package com.jira.kanban.controller;

import com.jira.kanban.dto.KanbanRequest;
import com.jira.kanban.dto.ReorderRequest;
import com.jira.kanban.entity.Kanban;
import com.jira.kanban.service.KanbanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 看板列接口（M4：K1~K4）。
 *
 * 路径跟 Mock 的集合名 kanbans 一模一样，前端只用换个 host:port。
 * 返回的都是裸 JSON（数组或对象），不套 Result——前端 http.ts 拿到啥就直接用啥，
 * 而且 DELETE、reorder 这些它也会 await response.json()，所以返回体不能是空的。
 * 报错还是走 GlobalExceptionHandler，出来是 { code, message, data }，message 会被 ErrorBox 直接显示。
 */
@RestController
public class KanbanController {

    private final KanbanService kanbanService;

    public KanbanController(KanbanService kanbanService) {
        this.kanbanService = kanbanService;
    }

    /** K1 GET /kanbans?projectId= —— 拿到排好顺序的列，裸数组 */
    @GetMapping("/kanbans")
    public List<Kanban> list(@RequestParam(value = "projectId", required = false) Long projectId) {
        return kanbanService.list(projectId);
    }

    /** K2 POST /kanbans —— 前端传 { name, projectId }，返回新建的对象（带真实 id） */
    @PostMapping("/kanbans")
    public Kanban create(@Valid @RequestBody KanbanRequest request) {
        return kanbanService.create(request);
    }

    /** K4 POST /kanbans/reorder —— 拖列排序，事务在里面 */
    @PostMapping("/kanbans/reorder")
    public List<Kanban> reorder(@Valid @RequestBody ReorderRequest request) {
        return kanbanService.reorder(request);
    }

    /** K3 DELETE /kanbans/{id} —— 删列，里面的任务跟着被删，返回被删的对象 */
    @DeleteMapping("/kanbans/{id}")
    public Kanban delete(@PathVariable("id") Long id) {
        return kanbanService.delete(id);
    }
}
