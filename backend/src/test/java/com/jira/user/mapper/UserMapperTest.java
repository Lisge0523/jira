package com.jira.user.mapper;

import com.jira.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * UserMapper 的链路自检：证明「MyBatis → MySQL → Java 对象」能正确映射。
 * 测试类与被测类同包，便于访问。
 */
@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void selectUserByUsername_shouldReturnUser() {
        User user = userMapper.selectUserByUsername("gaoxiuwen");
        System.out.println(user);

        assertNotNull(user, "应该能查到 gaoxiuwen 用户");
        assertEquals("高修文", user.getName(), "姓名应该是高修文");
        assertNotNull(user.getPasswordHash(), "passwordHash 有值 = 下划线转驼峰映射生效");
    }
}