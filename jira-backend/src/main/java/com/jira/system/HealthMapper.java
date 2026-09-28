package com.jira.system;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 骨架自检用的 Mapper：证明 MyBatis + MySQL 链路已打通。
 * 业务模块的 Mapper 请放在各自模块包下，并同样标注 @Mapper。
 */
@Mapper
public interface HealthMapper {

    @Select("SELECT COUNT(*) FROM sys_user")
    int countUsers();
}
