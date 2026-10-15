package com.jira.epic.mapper;

import com.jira.epic.entity.Epic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务组的增删查（E1/E2/E3 用的都在这）。没有改接口，前端就没做编辑功能。
 */
@Mapper
public interface EpicMapper {

    @Select("SELECT id, name, project_id, start_time, end_time FROM epic WHERE project_id = #{projectId} ORDER BY id")
    List<Epic> findByProject(Long projectId);

    @Select("SELECT id, name, project_id, start_time, end_time FROM epic WHERE id = #{id}")
    Epic findById(Long id);

    @Insert("INSERT INTO epic (name, project_id, start_time, end_time) VALUES (#{name}, #{projectId}, #{startTime}, #{endTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Epic epic);

    @Delete("DELETE FROM epic WHERE id = #{id}")
    int deleteById(Long id);
}
