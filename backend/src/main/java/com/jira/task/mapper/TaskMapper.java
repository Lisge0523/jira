package com.jira.task.mapper;

import com.jira.task.entity.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务表的增删改查（T1~T6 用的都在这）。
 * T1 是多条件筛选，条件可开可关，所以用了 MyBatis 的 <script> 动态 SQL。
 * 规则：参数为空 = 不过滤（跟前端 cleanObject 的行为对齐，见风险 R10）。
 */
@Mapper
public interface TaskMapper {

    @Select("<script>" +
            "SELECT id, name, project_id, kanban_id, epic_id, type_id, processor_id, note, sort_order " +
            "FROM task WHERE project_id = #{projectId} " +
            "<if test='name != null and name != \"\"'> AND LOWER(name) LIKE CONCAT('%', LOWER(#{name}), '%')</if>" +
            "<if test='processorId != null'> AND processor_id = #{processorId}</if>" +
            "<if test='typeId != null'> AND type_id = #{typeId}</if>" +
            " ORDER BY kanban_id, sort_order" +
            "</script>")
    List<Task> findByProject(@Param("projectId") Long projectId,
                             @Param("name") String name,
                             @Param("processorId") Long processorId,
                             @Param("typeId") Long typeId);

    @Select("SELECT id, name, project_id, kanban_id, epic_id, type_id, processor_id, note, sort_order " +
            "FROM task WHERE id = #{id}")
    Task findById(Long id);

    /** 某一列里的任务，按列内顺序排好（拖拽重排要用） */
    @Select("SELECT id, name, project_id, kanban_id, epic_id, type_id, processor_id, note, sort_order " +
            "FROM task WHERE kanban_id = #{kanbanId} ORDER BY sort_order")
    List<Task> findByKanbanId(Long kanbanId);

    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM task WHERE kanban_id = #{kanbanId}")
    int maxSortOrder(Long kanbanId);

    @Insert("INSERT INTO task (name, project_id, kanban_id, epic_id, type_id, processor_id, note, sort_order) " +
            "VALUES (#{name}, #{projectId}, #{kanbanId}, #{epicId}, #{typeId}, #{processorId}, #{note}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Task task);

    /** PATCH 用：把合并后的实体整体写回去（不可编辑的列写回原值，无副作用） */
    @Update("UPDATE task SET name=#{name}, processor_id=#{processorId}, type_id=#{typeId}, " +
            "note=#{note}, epic_id=#{epicId}, kanban_id=#{kanbanId} WHERE id=#{id}")
    int updateAll(Task task);

    /** 拖拽挪列时顺手更新 sort_order（挪进来的那行两个一起改） */
    @Update("UPDATE task SET kanban_id=#{kanbanId}, sort_order=#{sortOrder} WHERE id=#{id}")
    int updateKanbanAndSortOrder(@Param("id") Long id,
                                 @Param("kanbanId") Long kanbanId,
                                 @Param("sortOrder") int sortOrder);

    @Update("UPDATE task SET sort_order=#{sortOrder} WHERE id=#{id}")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") int sortOrder);

    @Delete("DELETE FROM task WHERE id = #{id}")
    int deleteById(Long id);
}
