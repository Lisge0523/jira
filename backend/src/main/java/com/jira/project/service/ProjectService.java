package com.jira.project.service;


import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.project.entity.Project;
import com.jira.project.mapper.ProjectMapper;
import com.jira.security.ProjectMemberChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
