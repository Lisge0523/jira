package com.jira.kanban.mapper;

import com.jira.kanban.entity.Kanban;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 看板列的增删改查 SQL（K1~K4 用的都在这）。
 * 表字段是下划线（project_id），Java 是驼峰（projectId），靠全局的 map-underscore-to-camel-case 自动转。
 */
@Mapper
public interface KanbanMapper {

    @Select("SELECT id, name, project_id, sort_order FROM kanban " +
            "WHERE project_id = #{projectId} ORDER BY sort_order")
    List<Kanban> findByProjectId(Long projectId);

    @Select("SELECT id, name, project_id, sort_order FROM kanban WHERE id = #{id}")
    Kanban findById(Long id);

    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM kanban WHERE project_id = #{projectId}")
    int maxSortOrder(Long projectId);

    @Insert("INSERT INTO kanban (name, project_id, sort_order) " +
            "VALUES (#{name}, #{projectId}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Kanban kanban);

    @Delete("DELETE FROM kanban WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE kanban SET sort_order = #{sortOrder} WHERE id = #{id}")
    int updateSortOrder(Long id, int sortOrder);
}
