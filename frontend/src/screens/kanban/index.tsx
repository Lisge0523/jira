import React, { useCallback } from "react";
import { useDocumentTitle } from "../../utils";
import {
  useKanbanSearchParams,
  useKanbansQueryKey,
  useProjectInUrl,
  useTasksQueryKey,
  useTasksSearchParams,
} from "../../screens/kanban/util";
import { KanbanColumn } from "../../screens/kanban/kanban-column";
import styled from "@emotion/styled";
import { useKanbans, useReorderKanban } from "../../utils/kanban";
import type { SortProps } from "../../utils/kanban";
import { SearchPanel } from "../../screens/kanban/search-panel";
import { ScreenContainer } from "../../components/lib";
import { useReorderTask, useTasks } from "../../utils/task";  
import { Spin } from "antd";
import { CreateKanban } from "../../screens/kanban/create-kanban";
import { TaskModal } from "../../screens/kanban/task-modal";
import { DragDropContext, DropResult } from "react-beautiful-dnd";  
import { Drag, Drop, DropChild } from "../../components/drag-and-drop";
import { Button } from "antd";
import { buildInverse, useKanbanHistory } from "./use-kanban-undo";
import type { KanbanActionKind } from "./use-kanban-undo";

export const KanbanScreen = () => {
  useDocumentTitle("看板列表");

  const { data: currentProject } = useProjectInUrl();
  const { data: kanbans, isLoading: kanbanIsLoading } = useKanbans(
    useKanbanSearchParams()
  );
  const { isLoading: taskIsLoading } = useTasks(useTasksSearchParams());
  const isLoading = taskIsLoading || kanbanIsLoading;

  const { onDragEnd, undo, redo, canUndo, canRedo } = useDragEnd();
  return (
    <DragDropContext onDragEnd={onDragEnd}>
      <ScreenContainer>
        <h1>{currentProject?.name}看板</h1>
        <UndoBar>
          <Button size={"small"} disabled={!canUndo} onClick={undo}>
            撤销 (Ctrl+Z)
          </Button>
          <Button size={"small"} disabled={!canRedo} onClick={redo}>
            重做 (Ctrl+Shift+Z)
          </Button>
        </UndoBar>
        <SearchPanel />
        {isLoading ? (
          <Spin size={"large"} />
        ) : (
          <ColumnsContainer>
            <Drop
              type={"COLUMN"}
              direction={"horizontal"}
              droppableId={"kanban"}
            >
              <DropChild style={{ display: "flex" }}>
                {kanbans?.map((kanban, index) => (
                  <Drag
                    key={kanban.id}
                    draggableId={`kanban-${kanban.id}`}
                    index={index}
                  >
                    <KanbanColumn kanban={kanban} key={kanban.id} />
                  </Drag>
                ))}
              </DropChild>
            </Drop>
            <CreateKanban />
          </ColumnsContainer>
        )}
        <TaskModal />
      </ScreenContainer>
    </DragDropContext>
  );
};

export const useDragEnd = () => {
  const { data: kanbans = [] } = useKanbans(useKanbanSearchParams());
  const { data: allTasks = [] } = useTasks(useTasksSearchParams());
  const { mutate: reorderKanban } = useReorderKanban(useKanbansQueryKey());
  const { mutate: reorderTask } = useReorderTask(useTasksQueryKey());

  const apply = useCallback(
    (kind: KanbanActionKind, params: SortProps) => {
      if (kind === "kanban") {
        reorderKanban(params);
      } else {
        reorderTask(params);
      }
    },
    [reorderKanban, reorderTask]
  );

  const { push, undo, redo, canUndo, canRedo } = useKanbanHistory(apply);

  const onDragEnd = useCallback(
    ({ source, destination, type }: DropResult) => {
      if (!destination) {
        return;
      }
      // 看板排序
      if (type === "COLUMN") {
        const fromId = kanbans[source.index]?.id;
        const toId = kanbans[destination.index]?.id;
        if (!fromId || !toId || fromId === toId) {
          return;
        }
        const forward: SortProps = {
          fromId,
          referenceId: toId,
          type: destination.index > source.index ? "after" : "before",
        };
        const inverse: SortProps = {
          fromId,
          ...buildInverse(kanbans, fromId, source.index),
        };
        push({ kind: "kanban", label: `看板 ${fromId}`, forward, inverse });
        reorderKanban(forward);
        return;
      }
      if (type === "ROW") {
        const fromKanbanId = +source.droppableId;
        const toKanbanId = +destination.droppableId;
        const fromTasks = allTasks.filter(
          (task) => task.kanbanId === fromKanbanId
        );
        const toTasks = allTasks.filter(
          (task) => task.kanbanId === toKanbanId
        );
        const fromTask = fromTasks[source.index];
        const toTask = toTasks[destination.index];
        if (!fromTask || fromTask.id === toTask?.id) {
          return;
        }
        // 同列向下拖是插到参照物后面，其余（含跨列）都是插到前面
        const forward: SortProps = {
          fromId: fromTask.id,
          referenceId: toTask?.id ?? 0,
          fromKanbanId,
          toKanbanId,
          type:
            fromKanbanId === toKanbanId && destination.index > source.index
              ? "after"
              : "before",
        };
        const inverse: SortProps = {
          fromId: fromTask.id,
          fromKanbanId: toKanbanId,
          toKanbanId: fromKanbanId,
          ...buildInverse(fromTasks, fromTask.id, source.index),
        };
        push({ kind: "task", label: `任务 ${fromTask.id}`, forward, inverse });
        reorderTask(forward);
      }
    },
    [kanbans, allTasks, push, reorderKanban, reorderTask]
  );

  return { onDragEnd, undo, redo, canUndo, canRedo };
};
export const ColumnsContainer = styled("div")`
  display: flex;
  overflow-x: scroll;
  flex: 1;
`;

const UndoBar = styled("div")`
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
`;
