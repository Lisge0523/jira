package com.jira.security;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 查权限要用的两张表：project、project_member。
 * 这俩表归 A 管，我们只读；等 A 的 security 模块做出来，这块可以交给它的统一实现。
 */
@Mapper
public interface ProjectAccessMapper {

    @Select("SELECT COUNT(*) FROM project WHERE id = #{projectId}")
    int countProject(Long projectId);

    @Select("SELECT COUNT(*) FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    int countMember(Long projectId, Long userId);
}
