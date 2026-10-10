package com.jira.kanban.service;

import com.jira.common.BusinessException;
import com.jira.common.ErrorCode;
import com.jira.kanban.dto.KanbanRequest;
import com.jira.kanban.dto.ReorderRequest;
import com.jira.kanban.entity.Kanban;
import com.jira.kanban.mapper.KanbanMapper;
import com.jira.security.ProjectAccessGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 看板列的业务逻辑（K1~K4）。
 *
 * 权限：进门先过 ProjectAccessGuard（项目得存在、用户得是成员）。
 * 排序：sort_order 在一个项目里从 1 开始连着编；拖完在一个事务里整个重排，不然并发拖拽会乱。
 */
@Service
public class KanbanService {

    private static final String TYPE_BEFORE = "before";
    private static final String TYPE_AFTER = "after";

    private final KanbanMapper kanbanMapper;
    private final ProjectAccessGuard accessGuard;

    public KanbanService(KanbanMapper kanbanMapper, ProjectAccessGuard accessGuard) {
        this.kanbanMapper = kanbanMapper;
        this.accessGuard = accessGuard;
    }

    /** K1：按项目查列，按 sort_order 排好。没传 projectId 就给空数组——别把别的项目的数据漏出去 */
    public List<Kanban> list(Long projectId) {
        if (projectId == null) {
            return Collections.emptyList();
        }
        accessGuard.requireProjectAccess(projectId);
        return kanbanMapper.findByProjectId(projectId);
    }

    /** K2：新建一列，直接排到项目最后 */
    public Kanban create(KanbanRequest request) {
        accessGuard.requireProjectAccess(request.getProjectId());
        Kanban kanban = new Kanban();
        kanban.setName(request.getName().trim());
        kanban.setProjectId(request.getProjectId());
        kanban.setSortOrder(kanbanMapper.maxSortOrder(request.getProjectId()) + 1);
        kanbanMapper.insert(kanban);
        return kanbanMapper.findById(kanban.getId());
    }

    /**
     * K3：删列。列里的任务会被数据库级联删掉（ON DELETE CASCADE，文档定的策略）。
     * 删完把被删的对象返回：前端会对结果 await response.json()，空 body 它会报错。
     */
    public Kanban delete(Long id) {
        Kanban kanban = requireKanban(id);
        accessGuard.requireProjectAccess(kanban.getProjectId());
        kanbanMapper.deleteById(id);
        return kanban;
    }

    /**
     * K4：拖列。一个事务里干两件事：挪位置 + 重新编号。
     * 挪的算法跟前端 reorder.ts 一模一样（先摘出来，再插到参照物前/后），
     * 这样前端乐观更新和服务端结果一致，拖完不会跳位。
     * referenceId 是 0 / 没传就是拖到最后（Mock 这儿会 400，我们修好了）。
     */
    @Transactional
    public List<Kanban> reorder(ReorderRequest request) {
        if (!TYPE_BEFORE.equals(request.getType()) && !TYPE_AFTER.equals(request.getType())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "type 只支持 before / after");
        }
        Kanban from = requireKanban(request.getFromId());
        accessGuard.requireProjectAccess(from.getProjectId());

        boolean moveToTail = request.getReferenceId() == null || request.getReferenceId() == 0;
        Kanban reference = null;
        if (!moveToTail) {
            reference = requireKanban(request.getReferenceId());
            if (!Objects.equals(reference.getProjectId(), from.getProjectId())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "只允许在同一项目内调整列顺序");
            }
        }

        // 先把数据库里的顺序查出来，在内存里挪一下（跟前端本地算的结果对齐）
        List<Kanban> ordered = new ArrayList<>(kanbanMapper.findByProjectId(from.getProjectId()));
        Kanban moved = removeById(ordered, from.getId());

        if (moveToTail) {
            ordered.add(moved);
        } else {
            int refIndex = indexOf(ordered, reference.getId());
            int insertAt = TYPE_AFTER.equals(request.getType()) ? refIndex + 1 : refIndex;
            ordered.add(insertAt, moved);
        }

        // 挪完从 1 重新编号，只改真正变了的行，省点写库
        for (int i = 0; i < ordered.size(); i++) {
            Kanban k = ordered.get(i);
            int newOrder = i + 1;
            if (!Objects.equals(k.getSortOrder(), newOrder)) {
                kanbanMapper.updateSortOrder(k.getId(), newOrder);
                k.setSortOrder(newOrder);
            }
        }
        return ordered;
    }

    private Kanban requireKanban(Long id) {
        Kanban kanban = id == null ? null : kanbanMapper.findById(id);
        if (kanban == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "看板不存在");
        }
        return kanban;
    }

    /** 从列表里把指定 id 的元素摘出来并返回 */
    private Kanban removeById(List<Kanban> list, Long id) {
        Kanban moved = null;
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), id)) {
                moved = list.remove(i);
                break;
            }
        }
        if (moved == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "看板不存在");
        }
        return moved;
    }

    private int indexOf(List<Kanban> list, Long id) {
        for (int i = 0; i < list.size(); i++) {
            if (Objects.equals(list.get(i).getId(), id)) {
                return i;
            }
        }
        throw new BusinessException(ErrorCode.NOT_FOUND, "看板不存在");
    }
}
