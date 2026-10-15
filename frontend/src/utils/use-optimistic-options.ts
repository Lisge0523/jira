import { QueryKey, useQueryClient } from "react-query";
import { reorder } from "../utils/reorder";
import { Task } from "../types/task";

// 项目列表返回 { list, total }，其余列表接口返回数组，统一在这里兼容两种结构
const isPageResult = (data: any): data is { list: any[]; total: number } =>
  Boolean(data) && !Array.isArray(data) && Array.isArray(data.list);

const patchList = (old: any, updater: (list: any[]) => any[]) =>
  isPageResult(old) ? { ...old, list: updater(old.list) } : updater(old || []);

export const useConfig = (
  queryKey: QueryKey,
  callback: (target: any, old?: any) => any
) => {
  const queryClient = useQueryClient();
  return {
    onSuccess: () => queryClient.invalidateQueries(queryKey),
    async onMutate(target: any) {
      const previousItems = queryClient.getQueryData(queryKey);
      queryClient.setQueryData(queryKey, (old?: any[]) => {
        return callback(target, old);
      });
      return { previousItems };
    },
    onError(error: any, newItem: any, context: any) {
      queryClient.setQueryData(queryKey, context.previousItems);
    },
  };
};

export const useDeleteConfig = (queryKey: QueryKey) =>
  useConfig(queryKey, (target, old) =>
    patchList(old, (list) => list.filter((item) => item.id !== target.id))
  );
export const useEditConfig = (queryKey: QueryKey) =>
  useConfig(queryKey, (target, old) =>
    patchList(old, (list) =>
      list.map((item) =>
        item.id === target.id ? { ...item, ...target } : item
      )
    )
  );
export const useAddConfig = (queryKey: QueryKey) =>
  useConfig(queryKey, (target, old) =>
    patchList(old, (list) => [...list, target])
  );

export const useReorderKanbanConfig = (queryKey: QueryKey) =>
  useConfig(queryKey, (target, old) => reorder({ list: old, ...target }));

export const useReorderTaskConfig = (queryKey: QueryKey) =>
  useConfig(queryKey, (target, old) => {
    const orderedList = reorder({ list: old, ...target }) as Task[];
    return orderedList.map((item) =>
      item.id === target.fromId
        ? { ...item, kanbanId: target.toKanbanId }
        : item
    );
  });
