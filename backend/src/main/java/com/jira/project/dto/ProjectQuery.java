package com.jira.project.dto;

import lombok.Data;

@Data
public class ProjectQuery {
    /** 当前登录用户，用于成员过滤（最关键，不信任前端传的） */
    private Long userId;
    /** 项目名，模糊匹配 */
    private String name;
    /** 负责人，精确匹配 */
    private Long personId;
    /** 分页：跳过多少条 */
    private int offset;
    /** 每页条数 */
    private int size;
}
