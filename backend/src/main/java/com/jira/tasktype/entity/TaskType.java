package com.jira.tasktype.entity;

/**
 * 任务类型，就是个字典表。
 * 对应 task_type，前端那边 types/task-type.ts 长这样：{ id: number, name: string }。
 * 注意 id 是有含义的：0 是「类型」占位、1 是 Bug、2 是 Task，前端靠这个选图标，别动它。
 */
public class TaskType {

    private Long id;
    private String name;

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
}
