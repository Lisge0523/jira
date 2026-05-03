import React, { useState, useEffect, useRef } from "react";
import { useTasksSearchParams } from "../../screens/kanban/util";
import { useSetUrlSearchParam } from "../../utils/url";
import { Row } from "../../components/lib";
import { Button, Input } from "antd";
import { UserSelect } from "../../components/user-select";
import { TaskTypeSelect } from "../../components/task-type-select";

export const SearchPanel = () => {
  const searchParams = useTasksSearchParams();
  const setSearchParams = useSetUrlSearchParam();
  
  // 本地状态管理，立即响应用户输入
  const [localName, setLocalName] = useState(searchParams.name || "");

  // 使用 ref 存储 searchParams 和 setSearchParams，避免 useEffect 依赖导致的重复执行
  const searchParamsRef = useRef(searchParams);
  const setSearchParamsRef = useRef(setSearchParams);

  // 同步更新 ref
  useEffect(() => {
    searchParamsRef.current = searchParams;
    setSearchParamsRef.current = setSearchParams;
  });

  // 当外部 searchParams.name 变化时，同步到本地状态
  useEffect(() => {
    if (searchParams.name !== localName) {
      setLocalName(searchParams.name || "");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams.name]);

  // 防抖处理：延迟更新 URL 参数
  useEffect(() => {
    const timer = setTimeout(() => {
      const currentSearchParams = searchParamsRef.current;
      const currentSetSearchParams = setSearchParamsRef.current;
      
      if (localName !== currentSearchParams.name) {
        currentSetSearchParams({
          ...currentSearchParams,
          name: localName,
        });
      }
    }, 300); // 300ms 防抖延迟

    return () => clearTimeout(timer);
  }, [localName]);

  const reset = () => {
    setLocalName(""); // 同时清空本地状态
    setSearchParams({
      typeId: undefined,
      processorId: undefined,
      tagId: undefined,
      name: undefined,
    });
  };

  return (
    <Row marginBottom={4} gap={true}>
      <Input
        style={{ width: "20rem" }}
        placeholder={"任务名"}
        value={localName}
        onChange={(evt) => setLocalName(evt.target.value)}
      />
      <UserSelect
        defaultOptionName={"经办人"}
        value={searchParams.processorId}
        onChange={(value) => setSearchParams({ processorId: value })}
      />
      <TaskTypeSelect
        value={searchParams.typeId}
        onChange={(value) => setSearchParams({ typeId: value })}
      />
      <Button onClick={reset}>清除筛选器</Button>
    </Row>
  );
};
