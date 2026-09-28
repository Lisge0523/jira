package com.jira.user.mapper;

import com.jira.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户数据访问接口。
 *
 * @Mapper 让 MyBatis 在启动时为本接口生成实现类，因此可以直接 @Autowired 注入。
 */
@Mapper
public interface UserMapper {
    /**
     * 按用户名查询用户。
     * 查不到时返回 null。
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectUserByUsername(String username);
}