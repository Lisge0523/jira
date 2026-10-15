package com.jira.epic.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 任务组（M7 / Epic）。
 * 前端 types/epic.ts 长这样：{ id, name, projectId, start, end }，start/end 是毫秒时间戳。
 * 表里列名叫 start_time / end_time（MyBatis 驼峰转换接成 startTime/endTime），
 * 但下发/上报的 JSON 键名得叫 start / end —— 加个 @JsonProperty 改个名，前端一行不用改。
 */
public class Epic {

    private Long id;
    private String name;
    private Long projectId;

    /** 开始时间（毫秒时间戳），前端叫 start */
    @JsonProperty("start")
    private Long startTime;

    /** 结束时间（毫秒时间戳），前端叫 end */
    @JsonProperty("end")
    private Long endTime;

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

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
}
