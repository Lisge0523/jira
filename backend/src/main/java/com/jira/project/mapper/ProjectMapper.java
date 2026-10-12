package com.jira.project.mapper;

import com.jira.project.dto.ProjectQuery;
import com.jira.project.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMapper {
    @Select("SELECT * FROM project WHERE id = #{id}")
    Project selectById(Long id);
    List<Project> selectPage(ProjectQuery projectQuery);
    long countByQuery(ProjectQuery projectQuery);
}
