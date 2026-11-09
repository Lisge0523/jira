package com.jira.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录凭证（JWT）的 Cookie 读写。
 *
 * 设计要点：
 *   - HttpOnly：前端 JS 无法读取，XSS 偷不走长期凭证；
 *   - Path=/：登录、退出、清理必须用同一个 Path，否则清不干净；
 *   - Secure / SameSite 由配置决定：本地 HTTP 用 false + Lax，
 *     公网 HTTPS 建议 true + Lax（真跨站部署时才需要 None + Secure）；
 *   - Max-Age 必须和 JWT 的 expire-hours 对齐，否则 Cookie 会先于 token 失效。
 */
@Component
public class TokenCookie {

    private final String name;
    private final boolean secure;
    private final String sameSite;
    private final long maxAgeSeconds;

    public TokenCookie(
            @Value("${jira.auth.cookie-name:jira_token}") String name,
            @Value("${jira.auth.cookie-secure:false}") boolean secure,
            @Value("${jira.auth.cookie-same-site:Lax}") String sameSite,
            @Value("${jira.jwt.expire-hours:24}") long expireHours) {
        this.name = name;
        this.secure = secure;
        this.sameSite = sameSite;
        this.maxAgeSeconds = expireHours * 3600L;
    }

    /** 登录/注册成功后下发凭证 */
    public void write(HttpServletResponse response, String token) {
        response.addHeader(HttpHeaders.SET_COOKIE, build(token, maxAgeSeconds));
    }

    /** 退出登录：Max-Age=0，让浏览器立刻丢弃 */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, build("", 0));
    }

    /** 从请求里取凭证，没有或为空时返回 null */
    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value == null || value.isEmpty()) ? null : value;
            }
        }
        return null;
    }

    private String build(String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(Duration.ofSeconds(maxAge))
                .build()
                .toString();
    }
}
