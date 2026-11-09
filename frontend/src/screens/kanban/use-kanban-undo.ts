import { useCallback, useEffect } from "react";
import { message } from "antd";
import { useUndo } from "../../utils/use-undo";
import type { SortProps } from "../../utils/kanban";

export type KanbanActionKind = "kanban" | "task";

/**
 * 一次拖拽动作：forward 是用户刚做的移动，inverse 是把它挪回原位的反向请求。
 * 两个都是 /kanbans/reorder 或 /tasks/reorder 的入参，撤销/重做时直接复用。
 */
export interface KanbanAction {
  kind: KanbanActionKind;
  label: string;
  forward: SortProps;
  inverse: SortProps;
}

/**
 * 计算「把 itemId 挪回 targetIndex」需要传给后端的参照物。
 * 先把它从列表里摘掉，再看目标下标上现在是谁：有人就插到它前面；
 * 目标下标越界（原本在最后）就传 referenceId=0，后端按「追加到列尾」处理。
 */
export const buildInverse = (
  list: { id: number }[],
  itemId: number,
  targetIndex: number
): { referenceId: number; type: "before" | "after" } => {
  const rest = list.filter((item) => item.id !== itemId);
  const neighbor = rest[targetIndex];
  return neighbor
    ? { referenceId: neighbor.id, type: "before" }
    : { referenceId: 0, type: "after" };
};

export type ApplyKanbanAction = (kind: KanbanActionKind, params: SortProps) => void;

/**
 * 看板的撤销 / 重做历史。
 *
 * 之所以不直接拿快照回写 React Query 缓存，是因为后端只提供「单条 reorder」，
 * 快照回写会造成 UI 与数据库不一致；这里改成记录「反向 reorder 请求」，
 * 撤销时真的发一次请求落库，再由 mutation 的 invalidate 拉到最终真相。
 */
export const useKanbanHistory = (apply: ApplyKanbanAction) => {
  const [rawState, { set, undo, redo }] = useUndo<KanbanAction[]>([]);
  // useUndo 的 reducer 是泛型箭头函数，useReducer 推断不出具体类型，这里显式收敛
  const state = rawState as {
    past: KanbanAction[][];
    present: KanbanAction[];
    future: KanbanAction[][];
  };

  const canUndo = state.present.length > 0;
  const canRedo = state.future.length > 0;

  const push = useCallback(
    (action: KanbanAction) => {
      set([...state.present, action]);
    },
    [set, state.present]
  );

  const undoDrag = useCallback(() => {
    const action = state.present[state.present.length - 1];
    if (!action) {
      return;
    }
    apply(action.kind, action.inverse);
    undo();
    message.info(`已撤销：${action.label}`);
  }, [apply, state.present, undo]);

  const redoDrag = useCallback(() => {
    const snapshot = state.future[0];
    const action = snapshot?.[snapshot.length - 1];
    if (!action) {
      return;
    }
    apply(action.kind, action.forward);
    redo();
    message.info(`已重做：${action.label}`);
  }, [apply, state.future, redo]);

  // Ctrl/Cmd+Z 撤销，Ctrl/Cmd+Shift+Z（或 Ctrl+Y）重做；输入框里不拦截
  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (!event.ctrlKey && !event.metaKey) {
        return;
      }
      const target = event.target as HTMLElement | null;
      const tagName = target?.tagName?.toLowerCase();
      if (tagName === "input" || tagName === "textarea" || target?.isContentEditable) {
        return;
      }
      const key = event.key.toLowerCase();
      if (key === "z") {
        event.preventDefault();
        if (event.shiftKey) {
          redoDrag();
        } else {
          undoDrag();
        }
      } else if (key === "y") {
        event.preventDefault();
        redoDrag();
      }
    };
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [undoDrag, redoDrag]);

  return { push, undo: undoDrag, redo: redoDrag, canUndo, canRedo };
};
