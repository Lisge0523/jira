package com.jira.project.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMemberMapper {

    @Select("SELECT COUNT(*) FROM project_member WHERE project_id = #{projectId} AND user_id = #{userId}")
    int countMember(@Param("projectId") Long projectId,@Param("userId") Long userId);
    @Insert("INSERT INTO project_member (project_id, user_id) VALUES (#{projectId}, #{userId})")
    int insert(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
