package com.jira.project.mapper;

import com.jira.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProjectMapper {
    @Select("SELECT * FROM project WHERE id = #{id}")
    Project selectById(Long id);
}
