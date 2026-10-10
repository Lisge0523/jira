package com.jira.security;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 项目权限校验的统一入口（文档 8.4 说好了：A 提供，B 只管调）。
 *
 * ⚠️ 这是个临时版：A 的 JWT 拦截器还没来，所以现在这么用：
 *   - jira.auth.enabled = false（默认）：只查项目在不在，成员关系先不查；
 *   - 等 A 把登录做好：置 true，然后把下面那个解析 Mock token 的临时逻辑删掉，
 *     改成从 A 的拦截器（比如 UserContext）里拿 userId，业务代码一行不用动。
 */
@Component
public class ProjectAccessGuard {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MOCK_TOKEN_PREFIX = "token_";

    private final ProjectAccessMapper projectAccessMapper;

    /** 临时开关，A 的鉴权拦截器交付后置为 true */
    @Value("${jira.auth.enabled:false}")
    private boolean authEnabled;

    public ProjectAccessGuard(ProjectAccessMapper projectAccessMapper) {
        this.projectAccessMapper = projectAccessMapper;
    }

    /**
     * 先看项目在不在；鉴权开了的话，再看这人是不是项目成员。
     * 所有看板/任务/任务组接口都得走这里，前端传的 projectId 不能直接信。
     */
    public void requireProjectAccess(Long projectId) {
        if (projectId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "项目 id 不能为空");
        }
        if (projectAccessMapper.countProject(projectId) == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "项目不存在");
        }
        if (!authEnabled) {
            return;
        }
        Long userId = currentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        if (projectAccessMapper.countMember(projectId, userId) == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /** 临时招：从 Authorization 头里把用户 id 扣出来（只认 Mock 那种 token_1_xxx 格式） */
    private Long currentUserId() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return null;
        }
        // token_1_1699999999999 -> 1
        String token = header.substring(BEARER_PREFIX.length());
        if (!token.startsWith(MOCK_TOKEN_PREFIX)) {
            return null;
        }
        String[] parts = token.split("_");
        if (parts.length < 2) {
            return null;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }
}
