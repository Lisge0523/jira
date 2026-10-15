package com.jira.epic.controller;

import com.jira.epic.dto.EpicRequest;
import com.jira.epic.entity.Epic;
import com.jira.epic.service.EpicService;
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
 * 任务组接口（M7：E1~E3）。
 *
 * 路径跟 Mock 的集合名 epics 一致。返回全是裸 JSON，不套 Result（前端 http.ts 拿到啥就用啥）。
 * 时间字段注意：下发的是 start / end（毫秒数），不是 start_time——那是数据库里的事。
 */
@RestController
public class EpicController {

    private final EpicService epicService;

    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    /** E1 GET /epics?projectId= —— 裸数组，前端 Epic 页直接渲染 */
    @GetMapping("/epics")
    public List<Epic> list(@RequestParam(value = "projectId", required = false) Long projectId) {
        return epicService.list(projectId);
    }

    /** E2 POST /epics —— 前端实际只传 { name, projectId }，返回带真实 id 的新对象 */
    @PostMapping("/epics")
    public Epic create(@Valid @RequestBody EpicRequest request) {
        return epicService.create(request);
    }

    /** E3 DELETE /epics/{id} —— 组下任务不删、epic_id 置空，返回被删的对象 */
    @DeleteMapping("/epics/{id}")
    public Epic delete(@PathVariable("id") Long id) {
        return epicService.delete(id);
    }
}
