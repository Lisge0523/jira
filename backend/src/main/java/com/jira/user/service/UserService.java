package com.jira.user.service;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.security.JwtUtil;
import com.jira.user.dto.LoginRequest;
import com.jira.user.dto.UserVO;
import com.jira.user.entity.User;
import com.jira.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务逻辑。
 *
 * 待实现：
 *   - 登录校验（BCrypt matches + 签发 JWT）
 *   - 注册（校验用户名唯一 + 密码加密存储）
 *   - 按 ID 查询用户
 */
@Service
@RequiredArgsConstructor      //为所有`final` 字段 自动生成构造器 ，Spring 用构造器注入依赖——所以你一行构造器都不用写
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    public UserVO login(LoginRequest request){
        User user = userMapper.selectUserByUsername(request.getUsername());

        // 用户不存在 与 密码错误 返回相同的提示，
        // 避免攻击者借此判断"某个用户名是否存在"
        if (user ==null||!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"用户名或密码错误");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        return vo;
    }
}