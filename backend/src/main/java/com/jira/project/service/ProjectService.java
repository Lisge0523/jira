package com.jira.project.service;


import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.common.PageResult;
import com.jira.project.dto.ProjectQuery;
import com.jira.project.entity.Project;
import com.jira.project.mapper.ProjectMapper;
import com.jira.security.ProjectMemberChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectMemberChecker memberChecker;

    public Project getById(Long id,Long userId){
        Project project = projectMapper.selectById(id);
        if(project == null){
            throw new BusinessException(ErrorCode.NOT_FOUND,"项目不存在");
        }
        memberChecker.check(id,userId);
        return project;
    }
    public PageResult<Project> list(String name, Long personId, int page, int size, Long userId) {
        int safePage = Math.max(page, 1);        // 页码兜底，避免 offset 为负导致 SQL 报错

        ProjectQuery query = new ProjectQuery();
        query.setUserId(userId);
        query.setName(name);
        query.setPersonId(personId);
        query.setOffset((safePage - 1) * size);
        query.setSize(size);

        long total = projectMapper.countByQuery(query);
        List<Project> list = projectMapper.selectPage(query);
        return new PageResult<>(list, total);
    }
}
