package com.jira.security;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.project.mapper.ProjectMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 项目成员权限校验：所有涉及项目数据的接口都应调用它。
 */
@Component
@RequiredArgsConstructor
public class ProjectMemberChecker {
    private  final ProjectMemberMapper projectMemberMapper;
    /** 不是项目成员时抛 403 */
    public void check(Long projectId, Long userId) {
        if (projectMemberMapper.countMember(projectId, userId) == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN,"无权限访问该项目");
        }
    }
}
