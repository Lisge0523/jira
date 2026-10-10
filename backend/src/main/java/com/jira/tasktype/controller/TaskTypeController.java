package com.jira.tasktype.controller;

import com.jira.tasktype.entity.TaskType;
import com.jira.tasktype.service.TaskTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 任务类型接口（M6 / Y1）。
 *
 * GET /taskTypes —— 把类型全给前端。
 *
 * 这里返回的是「裸数组」，没套 Result。为啥？前端 http.ts 拿到响应就直接当 TaskType[] 用，
 * 套一层它反而解析不了。文档 1.3 也定了：这种列表就直接返回数组，前端一行都不用改。
 */
@RestController
public class TaskTypeController {

    private final TaskTypeService taskTypeService;

    public TaskTypeController(TaskTypeService taskTypeService) {
        this.taskTypeService = taskTypeService;
    }

    @GetMapping("/taskTypes")
    public List<TaskType> list() {
        return taskTypeService.list();
    }
}
