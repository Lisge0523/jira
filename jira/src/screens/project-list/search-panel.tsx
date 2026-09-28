import React, { useState, useEffect, useRef } from "react";
import { Form, Input } from "antd";
import { UserSelect } from "../../components/user-select";
import { Project } from "../../types/project";
import { User } from "../../types/user";

interface SearchPanelProps {
  users: User[];
  param: Partial<Pick<Project, "name" | "personId">>;
  setParam: (param: SearchPanelProps["param"]) => void;
}

export const SearchPanel = ({ users, param, setParam }: SearchPanelProps) => {
  const [localName, setLocalName] = useState(param.name || "");

  // 使用 ref 存储 param 和 setParam，避免 useEffect 依赖导致的重复执行
  const paramRef = useRef(param);
  const setParamRef = useRef(setParam);

  // 同步更新 ref
  useEffect(() => {
    paramRef.current = param;
    setParamRef.current = setParam;
  });

  // 当外部 param.name 变化时，同步到本地状态
  useEffect(() => {
    if (param.name !== localName) {
      setLocalName(param.name || "");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [param.name]);

  // 防抖处理：延迟更新 URL 参数
  useEffect(() => {
    const timer = setTimeout(() => {
      const currentParam = paramRef.current;
      const currentSetParam = setParamRef.current;
      
      if (localName !== currentParam.name) {
        currentSetParam({
          ...currentParam,
          name: localName,
        });
      }
    }, 300); // 300ms 防抖延迟

    return () => clearTimeout(timer);
  }, [localName]);

  return (
    <Form style={{ marginBottom: "2rem" }} layout={"inline"}>
      <Form.Item>
        <Input
          placeholder={"项目名"}
          type="text"
          value={localName}
          onChange={(evt) => setLocalName(evt.target.value)}
        />
      </Form.Item>
      <Form.Item>
        <UserSelect
          defaultOptionName={"负责人"}
          value={param.personId}
          onChange={(value) =>
            setParam({
              ...param,
              personId: value,
            })
          }
        />
      </Form.Item>
    </Form>
  );
};
