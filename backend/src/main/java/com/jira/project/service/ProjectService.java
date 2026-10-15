package com.jira.project.service;


import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.common.PageResult;
import com.jira.project.dto.ProjectCreateRequest;
import com.jira.project.dto.ProjectQuery;
import com.jira.project.dto.ProjectUpdateRequest;
import com.jira.project.entity.Project;
import com.jira.project.mapper.ProjectMapper;
import com.jira.project.mapper.ProjectMemberMapper;
import com.jira.security.ProjectMemberChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectMapper projectMapper;
    private final ProjectMemberChecker memberChecker;
    private final ProjectMemberMapper projectMemberMapper;

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
    @Transactional
    public Project create(ProjectCreateRequest request, Long userId) {
        Project project = new Project();
        project.setName(request.getName());
        project.setOrganization(request.getOrganization());
        project.setPersonId(request.getPersonId());
        project.setCreated(System.currentTimeMillis());    // 毫秒时间戳，与前契约一致
        project.setPin(false);
        projectMapper.insert(project);          // 插入后 project.getId() 有值

        // 创建者自动成为成员，否则创建完自己都看不到这个项目
        projectMemberMapper.insert(project.getId(), userId);
        return project;
    }
    public void delete(Long id,Long userId){
        getById(id,userId);
        projectMapper.deleteById(id);
    }

    public Project update(Long id, ProjectUpdateRequest request, Long userId) {
        getById(id,userId);
        Project update =  new Project();
        update.setId(id);
        update.setName(request.getName());
        update.setOrganization(request.getOrganization());
        update.setPersonId(request.getPersonId());
        update.setPin(request.getPin());
        projectMapper.update(update);
        return projectMapper.selectById(id);
    }
}
