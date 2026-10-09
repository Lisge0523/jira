package com.jira.user.mapper;

import com.jira.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
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
    /**
     * 新增用户。
     *
     * @Options (useGeneratedKeys = true, keyProperty = "id")
     *   让 MyBatis 把数据库生成的自增主键回填到 user 对象的 id 字段上。
     *   没有这行，insert 之后 user.getId() 就是 null。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO sys_user (username, password_hash, name, email, title, organization) " +
            "VALUES (#{username}, #{passwordHash}, #{name}, #{email}, #{title}, #{organization})")
    int insert (User user) ;
    /**
     加一个按 ID 查的方法
     */
    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User selectById(Long id);
}