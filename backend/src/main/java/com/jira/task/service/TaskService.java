package com.jira.task.service;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.kanban.entity.Kanban;
import com.jira.kanban.mapper.KanbanMapper;
import com.jira.security.ProjectAccessGuard;
import com.jira.task.dto.TaskPatchRequest;
import com.jira.task.dto.TaskReorderRequest;
import com.jira.task.dto.TaskRequest;
import com.jira.task.entity.Task;
import com.jira.task.mapper.TaskMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 任务的业务逻辑（T1~T6）。
 *
 * 权限：还是那个规矩，进门先过 ProjectAccessGuard（项目得存在、用户得是成员），
 * projectId 一律从任务记录里拿，不信任前端传的。
 * 拖拽挪列是重头戏：一个事务里改 kanban_id + 把涉及的列全部重新编号，
 * 算法跟前端 reorder.ts 逐位对齐，拖完重查不会跳位（同 K4 的套路，只是多了跨列）。
 */
@Service
public class TaskService {

    private static final String TYPE_BEFORE = "before";
    private static final String TYPE_AFTER = "after";

    private final TaskMapper taskMapper;
    private final KanbanMapper kanbanMapper;
    private final ProjectAccessGuard accessGuard;

    public TaskService(TaskMapper taskMapper, KanbanMapper kanbanMapper, ProjectAccessGuard accessGuard) {
        this.taskMapper = taskMapper;
        this.kanbanMapper = kanbanMapper;
        this.accessGuard = accessGuard;
    }

    /**
     * T1：按项目查任务，支持 name 模糊（不分大小写）、processorId、typeId 过滤，不分页（看板要一次拿全）。
     * 没传 projectId 给空数组——不然得跨项目查，权限没法过滤。
     * 前端 URL 上还有个 tagId，功能已决定砍掉（R6），参数传过来就当没看见。
     */
    public List<Task> list(Long projectId, String name, Long processorId, Long typeId) {
        if (projectId == null) {
            return Collections.emptyList();
        }
        accessGuard.requireProjectAccess(projectId);
        return taskMapper.findByProject(projectId, trimToNull(name), zeroToNull(processorId), zeroToNull(typeId));
    }

    /** T2：单查一条，编辑弹窗回显用 */
    public Task detail(Long id) {
        Task task = requireTask(id);
        accessGuard.requireProjectAccess(task.getProjectId());
        return task;
    }

    /**
     * T3：新建任务。要验三样：名字必填、项目在、kanban 确实属于这个项目
     * （文档 4.3-6：不能只信前端传的 projectId/kanbanId 组合）。新任务排到列尾。
     */
    public Task create(TaskRequest request) {
        accessGuard.requireProjectAccess(request.getProjectId());
        Kanban kanban = kanbanMapper.findById(request.getKanbanId());
        if (kanban == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "看板列不存在");
        }
        if (!Objects.equals(kanban.getProjectId(), request.getProjectId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "看板列不属于该项目");
        }
        Task task = new Task();
        task.setName(request.getName().trim());
        task.setProjectId(request.getProjectId());
        task.setKanbanId(request.getKanbanId());
        task.setProcessorId(zeroToNull(request.getProcessorId()));
        task.setTypeId(zeroToNull(request.getTypeId()));
        task.setEpicId(zeroToNull(request.getEpicId()));
        task.setNote(request.getNote());
        task.setSortOrder(taskMapper.maxSortOrder(request.getKanbanId()) + 1);
        taskMapper.insert(task);
        return taskMapper.findById(task.getId());
    }

    /**
     * T4：编辑任务（部分更新）。只认 name/processorId/typeId/note 四个字段，
     * null 或 0 都算「这次不改」（前端 IdSelect 用 0 当占位选项），其他列原值写回。
     */
    public Task patch(Long id, TaskPatchRequest request) {
        Task task = requireTask(id);
        accessGuard.requireProjectAccess(task.getProjectId());

        String name = trimToNull(request.getName());
        if (name != null) {
            task.setName(name);
        }
        Long processorId = zeroToNull(request.getProcessorId());
        if (processorId != null) {
            task.setProcessorId(processorId);
        }
        Long typeId = zeroToNull(request.getTypeId());
        if (typeId != null) {
            task.setTypeId(typeId);
        }
        if (request.getNote() != null) {
            task.setNote(request.getNote());
        }
        taskMapper.updateAll(task);
        return taskMapper.findById(id);
    }

    /** T5：删任务。返回被删的对象——老规矩，前端会 await response.json()，空 body 它报错 */
    public Task delete(Long id) {
        Task task = requireTask(id);
        accessGuard.requireProjectAccess(task.getProjectId());
        taskMapper.deleteById(id);
        return task;
    }

    /**
     * T6：任务跨列拖拽。一个事务干完：
     *   1. 目标列：算好「摘出来 → 插到参照物前/后（没参照物就追尾）」的最终顺序；
     *   2. 落库：目标列从 1 重新编号，挪进来的那个顺带改 kanban_id；
     *   3. 源列（跨列才有）：剩下的任务补洞，也是从 1 连续编号；
     *   4. 返回整个项目的任务列表（排好序），前端 invalidate 后拿到的就是最终态。
     * 参照任务不存在或不在目标列 → 按「追加到末尾」兜底
     * （前端 reorder.ts 的 !referenceId 分支本来就是追尾，保持一致；Mock 这儿行为不一致，已修）。
     */
    @Transactional
    public List<Task> reorder(TaskReorderRequest request) {
        Task moved = requireTask(request.getFromId());
        accessGuard.requireProjectAccess(moved.getProjectId());

        Long toKanbanId = request.getToKanbanId();
        Kanban targetKanban = kanbanMapper.findById(toKanbanId);
        if (targetKanban == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "目标看板列不存在");
        }
        if (!Objects.equals(targetKanban.getProjectId(), moved.getProjectId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不允许跨项目挪任务");
        }

        Long fromKanbanId = moved.getKanbanId();
        boolean crossKanban = !Objects.equals(fromKanbanId, toKanbanId);
        boolean moveToTail = request.getReferenceId() == null || request.getReferenceId() == 0;
        if (!moveToTail && !TYPE_BEFORE.equals(request.getType()) && !TYPE_AFTER.equals(request.getType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "type 只支持 before / after");
        }

        // 先在内存里算目标列的最终顺序（跟前端本地算的结果对齐）
        List<Task> targetList = new ArrayList<>(taskMapper.findByKanbanId(toKanbanId));
        if (!crossKanban) {
            // 同列（理论上前端不会发这种请求，兜底支持）：先摘出来再找位置
            removeById(targetList, moved.getId());
        }
        if (moveToTail) {
            targetList.add(moved);
        } else {
            int refIndex = indexOf(targetList, request.getReferenceId());
            if (refIndex < 0) {
                // 参照物不在这一列，按追尾兜底
                targetList.add(moved);
            } else {
                int insertAt = TYPE_AFTER.equals(request.getType()) ? refIndex + 1 : refIndex;
                targetList.add(Math.min(insertAt, targetList.size()), moved);
            }
        }

        // 落库：目标列整体从 1 编号；挪进来的那个连 kanban_id 一起改，其余只动变了顺序的
        for (int i = 0; i < targetList.size(); i++) {
            Task t = targetList.get(i);
            int newOrder = i + 1;
            if (Objects.equals(t.getId(), moved.getId())) {
                taskMapper.updateKanbanAndSortOrder(t.getId(), toKanbanId, newOrder);
                t.setKanbanId(toKanbanId);
                t.setSortOrder(newOrder);
            } else if (!Objects.equals(t.getSortOrder(), newOrder)) {
                taskMapper.updateSortOrder(t.getId(), newOrder);
                t.setSortOrder(newOrder);
            }
        }

        // 跨列的话，源列摘走一个，剩下的把洞补上（1..n 连续）
        if (crossKanban) {
            List<Task> sourceList = taskMapper.findByKanbanId(fromKanbanId);
            for (int i = 0; i < sourceList.size(); i++) {
                Task t = sourceList.get(i);
                int newOrder = i + 1;
                if (!Objects.equals(t.getSortOrder(), newOrder)) {
                    taskMapper.updateSortOrder(t.getId(), newOrder);
                }
            }
        }

        return taskMapper.findByProject(moved.getProjectId(), null, null, null);
    }

    private Task requireTask(Long id) {
        Task task = id == null ? null : taskMapper.findById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在");
        }
        return task;
    }

    /** 0 当「没选」处理（前端 IdSelect/UserSelect 用 0 占位，见 R10） */
    private Long zeroToNull(Long value) {
        return value == null || value == 0L ? null : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void removeById(List<Task> list, Long id) {
        list.removeIf(t -> Objects.equals(t.getId(), id));
    }

    /** 参照任务的位置；不在这一列（或压根不存在）返回 -1，调用方按追尾兜底 */
    private int indexOf(List<Task> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), id)) {
                return i;
            }
        }
        return -1;
    }
}
