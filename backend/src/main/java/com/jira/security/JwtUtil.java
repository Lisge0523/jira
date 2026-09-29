package com.jira.security;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expireMillis;
    public JwtUtil(@Value("{$jira.jwt.secret}") String secret,
                   @Value("{jira.jwt.expire-hours}")long expireHours){
     this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
     this.expireMillis = expireHours *3600_000L;
    }
    /** 签发 token，把 userId 放进 subject */
    public String generateToken (Long userId, String username) {
        Date now = new Date ();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim( "username" , username)
            .issuedAt(now)
            .expiration( new Date (now.getTime() + expireMillis))
            .signWith(key)
            .compact();
    }
    /** 从 token 解析出 userId；无效或过期时抛 401 */
    public Long parseUserId (String token) {
        try {
            String subject = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject(); return Long.valueOf(subject);
    } catch (JwtException | IllegalArgumentException e) { throw new BusinessException (ErrorCode.UNAUTHORIZED, "登录已过期，请重新登录" );
    }
    }
}
