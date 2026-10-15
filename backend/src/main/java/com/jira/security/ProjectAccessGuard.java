package com.jira.security;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 项目权限校验的统一入口（文档 8.4 说好了：A 提供，B 只管调）。
 *
 * A 的 LoginInterceptor 已经到位了，这里正式接上：
 * 拦截器验完 JWT 会把 userId 塞进请求属性，我们从属性里拿，自己不再解析 token；
 * 成员关系也真的开始查了（不是以前那个只验项目存在的临时版）。
 */
@Component
public class ProjectAccessGuard {

    private final ProjectAccessMapper projectAccessMapper;

    public ProjectAccessGuard(ProjectAccessMapper projectAccessMapper) {
        this.projectAccessMapper = projectAccessMapper;
    }

    /**
     * 先看项目在不在，再看这人是不是项目成员。
     * 所有看板/任务/任务组接口都得走这里，前端传的 projectId 不能直接信。
     */
    public void requireProjectAccess(Long projectId) {
        if (projectId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "项目 id 不能为空");
        }
        if (projectAccessMapper.countProject(projectId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }
        Long userId = currentUserId();
        if (userId == null) {
            // 正常走不到这儿：拦截器比这里先拦；留着当哨兵——防的是拦截器放行规则被改坏
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (projectAccessMapper.countMember(projectId, userId) == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /** 从 LoginInterceptor 放进请求的属性里拿当前用户 id */
    private Long currentUserId() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        Object attr = request.getAttribute(LoginInterceptor.ATTR_USER_ID);
        return attr instanceof Long ? (Long) attr : null;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
