import { useLocation } from "react-router";
import { useProject } from "../../utils/project";
import { useUrlQueryParam } from "../../utils/url";
import { useCallback, useMemo } from "react";
import { useTask } from "../../utils/task";

export const useProjectIdInUrl = () => {
  const { pathname } = useLocation();
  const id = pathname.match(/projects\/(\d+)/)?.[1];
  return Number(id);
};

export const useProjectInUrl = () => useProject(useProjectIdInUrl());

export const useKanbanSearchParams = () => ({ projectId: useProjectIdInUrl() });

export const useKanbansQueryKey = () => ["kanbans", useKanbanSearchParams()];

export const useTasksSearchParams = () => {
  const [param] = useUrlQueryParam([
    "name",
    "typeId",
    "processorId",
    "tagId",
  ]);
  const projectId = useProjectIdInUrl();
  // 移除 useDebounce，防抖只在 SearchPanel 组件中处理
  return useMemo(
    () => ({
      projectId,
      // 当 typeId 为 0（类型/全部）或无效值时，返回 undefined 表示不筛选
      typeId: (Number(param.typeId) === 0 || isNaN(Number(param.typeId))) ? undefined : Number(param.typeId),
      processorId: Number(param.processorId) || undefined,
      tagId: Number(param.tagId) || undefined,
      name: param.name,  // 直接使用 param.name，不做防抖处理
    }),
    [projectId, param]  // 移除 debouncedName 依赖
  );
};

export const useTasksQueryKey = () => ["tasks", useTasksSearchParams()];

export const useTasksModal = () => {
  const [{ editingTaskId }, setEditingTaskId] = useUrlQueryParam([
    "editingTaskId",
  ]);
  const { data: editingTask, isLoading } = useTask(Number(editingTaskId));
  const startEdit = useCallback(
    (id: number) => {
      setEditingTaskId({ editingTaskId: id });
    },
    [setEditingTaskId]
  );
  const close = useCallback(() => {
    setEditingTaskId({ editingTaskId: "" });
  }, [setEditingTaskId]);
  return {
    editingTaskId,
    editingTask,
    startEdit,
    close,
    isLoading,
  };
};
