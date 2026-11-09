package com.jira.user.service;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.security.JwtUtil;
import com.jira.user.dto.AuthResult;
import com.jira.user.dto.LoginRequest;
import com.jira.user.dto.RegisterRequest;
import com.jira.user.dto.UserVO;
import com.jira.user.entity.User;
import com.jira.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public AuthResult login(LoginRequest request){
        User user = userMapper.selectUserByUsername(request.getUsername());
        // 用户不存在 与 密码错误 返回相同的提示，
        // 避免攻击者借此判断"某个用户名是否存在"
        if (user ==null||!passwordEncoder.matches(request.getPassword(),user.getPasswordHash())){
            throw new BusinessException(ErrorCode.UNAUTHORIZED,"用户名或密码错误");
        }
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        // token 交给 Controller 写进 HttpOnly Cookie，不进 VO
        return new AuthResult(vo, jwtUtil.generateToken(user.getId(), user.getUsername()));
    }
    public AuthResult register(RegisterRequest request){
        //1.先进行用户名查重
        if (userMapper.selectUserByUsername(request.getUsername())!=null){
            throw new BusinessException(ErrorCode.CONFLICT,"用户名已存在");
        }
        //2.组装用户对象，密码必须加密后存储
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getUsername());
        userMapper.insert(user);
        //3注册即登录，返回 token（同样只用于写 Cookie）
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        return new AuthResult(vo, jwtUtil.generateToken(user.getId(), user.getUsername()));
    }
    public UserVO me(Long userId){
    // userId 由拦截器从 Cookie 里的 token 解析后塞进请求属性，这里直接查用户
    User user = userMapper.selectById(userId);
    if (user ==null){
        throw new BusinessException(ErrorCode.UNAUTHORIZED,"用户不存在");
    }
    //转 VO 返回；凭证在 Cookie 里，不在响应体中重复下发
    UserVO vo = new UserVO();
    vo.setId(user.getId());
    vo.setName(user.getName());
    return vo;
    }
    public List<UserVO> listUsers(){
        return userMapper.selectAll().stream().map(u->{
            UserVO vo = new UserVO();
            vo.setId(u.getId());
            vo.setName(u.getName());
            vo.setEmail(u.getEmail());
            vo.setTitle(u.getTitle());
            vo.setOrganization(u.getOrganization());
            return vo;
        }).toList();
    }
}