package com.jira.tasktype.mapper;

import com.jira.tasktype.entity.TaskType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务类型的查询，就一个方法：全量查出来，按 id 排个序。
 * id 本身有固定含义，顺序稳定点，前端展示也稳。
 */
@Mapper
public interface TaskTypeMapper {

    @Select("SELECT id, name FROM task_type ORDER BY id")
    List<TaskType> selectAll();
}
