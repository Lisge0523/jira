import React from "react";
import { useNavigate } from "react-router-dom";
import { Navigate, Route, Routes, useLocation, useParams } from "react-router";
import { KanbanScreen } from "../kanban";
import { EpicScreen } from "../epic";
import styled from "@emotion/styled";
import { Menu } from "antd";

const useRouteType = () => {
  const units = useLocation().pathname.split("/");
  return units[units.length - 1];
};

export const ProjectScreen = () => {
  const routeType = useRouteType();
  const { projectId } = useParams();
  const navigate = useNavigate();

  const handleMenuClick = (path: string) => {
    navigate(`/projects/${projectId}/${path}`);
  };

  return (
    <Container>
      <Aside>
        <Menu 
          mode={"inline"} 
          selectedKeys={[routeType]} 
          onClick={({ key }) => handleMenuClick(key)}
          items={[
            {
              key: 'kanban',
              label: '看板'
            },
            {
              key: 'epic',
              label: '任务组'
            }
          ]} 
        />
      </Aside>
      <Main>
        <Routes>
          {/*projects/:projectId/kanban*/}
          <Route path={"kanban"} element={<KanbanScreen />} />
          {/*projects/:projectId/epic*/}
          <Route path={"epic"} element={<EpicScreen />} />
          <Route path={"*"} element={<Navigate to="kanban" replace />} />
        </Routes>
      </Main>
    </Container>
  );
};

const Aside = styled.aside`
  background-color: rgb(244, 245, 247);
  display: flex;
`;

const Main = styled.div`
  box-shadow: -5px 0 5px -5px rgba(0, 0, 0, 0.1);
  display: flex;
  overflow: hidden;
`;

const Container = styled.div`
  display: grid;
  grid-template-columns: 16rem 1fr;
  width: 100%;
`;
