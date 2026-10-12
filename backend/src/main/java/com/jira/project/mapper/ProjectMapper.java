package com.jira.project.mapper;

import com.jira.project.dto.ProjectQuery;
import com.jira.project.entity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProjectMapper {
    @Select("SELECT * FROM project WHERE id = #{id}")
    Project selectById(Long id);
    List<Project> selectPage(ProjectQuery projectQuery);
    long countByQuery(ProjectQuery projectQuery);
    @Options(useGeneratedKeys = true,keyProperty = "id")
    @Insert("INSERT INTO project (name, person_id, organization, created, pin) " +
            "VALUES (#{name}, #{personId}, #{organization}, #{created}, #{pin})")
        int insert(Project project);
    int update(Project project);

    @Delete("DELETE FROM project WHERE id = #{id}")
    int deleteById(Long id);
}
