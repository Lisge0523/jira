package com.jira.tasktype.service;

import com.jira.tasktype.entity.TaskType;
import com.jira.tasktype.mapper.TaskTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务类型是个字典表，不用管登录和权限，直接查全量就完事了。
 */
@Service
public class TaskTypeService {

    private final TaskTypeMapper taskTypeMapper;

    public TaskTypeService(TaskTypeMapper taskTypeMapper) {
        this.taskTypeMapper = taskTypeMapper;
    }

    public List<TaskType> list() {
        return taskTypeMapper.selectAll();
    }
}
