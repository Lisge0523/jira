package com.jira.security;


import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器：统一校验 token，校验通过后把 userId 放进请求属性，
 * 后续 Controller 用 @RequestAttribute ("userId") 取值。
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {
    public  static  final String ATTR_USER_ID = "userId";

    private  final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
//        放行跨域预检请求（OPTIONS），否则浏览器跨域调用会失败
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if ( authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"未登录或登录已过期");
        }
        // 解析失败（签名不对 / 已过期）内部会抛 401
        Long userId = jwtUtil.parseUserId(authHeader.substring(7));

        request.setAttribute(ATTR_USER_ID, userId);
        return true;// true = 放行；false = 中断请求
    }
}
