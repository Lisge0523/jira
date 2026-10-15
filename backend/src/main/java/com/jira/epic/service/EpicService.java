package com.jira.epic.service;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.epic.dto.EpicRequest;
import com.jira.epic.entity.Epic;
import com.jira.epic.mapper.EpicMapper;
import com.jira.security.ProjectAccessGuard;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 任务组的业务逻辑（E1/E2/E3）。
 *
 * 权限还是老规矩：进门先过 ProjectAccessGuard，列表没给 projectId 就直接空数组。
 * 有个提前打点的坑：删任务组【不能把任务一起删了】——task.epic_id 的外键是 ON DELETE SET NULL，
 * 任务本身留在看板上，只是不再属于哪个组（文档 4.2 定的策略，schema 里已经配好了）。
 */
@Service
public class EpicService {

    private final EpicMapper epicMapper;
    private final ProjectAccessGuard accessGuard;

    public EpicService(EpicMapper epicMapper, ProjectAccessGuard accessGuard) {
        this.epicMapper = epicMapper;
        this.accessGuard = accessGuard;
    }

    /** E1：按项目查任务组。没传 projectId 给空数组，别把别的项目的数据漏出去 */
    public List<Epic> list(Long projectId) {
        if (projectId == null) {
            return Collections.emptyList();
        }
        accessGuard.requireProjectAccess(projectId);
        return epicMapper.findByProject(projectId);
    }

    /** E2：新建任务组。start/end 前端现在根本不传（表单只有名称），可空就完事了 */
    public Epic create(EpicRequest request) {
        accessGuard.requireProjectAccess(request.getProjectId());
        Epic epic = new Epic();
        epic.setName(request.getName().trim());
        epic.setProjectId(request.getProjectId());
        epic.setStartTime(request.getStart());
        epic.setEndTime(request.getEnd());
        epicMapper.insert(epic);
        return epicMapper.findById(epic.getId());
    }

    /**
     * E3：删任务组。组下的任务不受影响，epic_id 会被数据库自动置空（ON DELETE SET NULL）。
     * 返回被删的对象——老规矩，前端会 await response.json()，空 body 它会报错。
     */
    public Epic delete(Long id) {
        Epic epic = epicMapper.findById(id);
        if (epic == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务组不存在");
        }
        accessGuard.requireProjectAccess(epic.getProjectId());
        epicMapper.deleteById(id);
        return epic;
    }
}
