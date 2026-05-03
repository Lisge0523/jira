import "./wdyr"
import React from 'react'
import ReactDOM from 'react-dom/client'
import 'antd/dist/antd.less'  // 导入 Ant Design 的 Less 样式文件以支持主题定制
import App from './App'
import reportWebVitals from './reportWebVitals'
import { AppProviders } from "./context";

// 过滤 react-beautiful-dnd 的 defaultProps 警告
const originalConsoleError = console.error;
console.error = (...args) => {
  if (
    typeof args[0] === 'string' &&
    args[0].includes('defaultProps will be removed from memo components')
  ) {
    return;
  }
  originalConsoleError(...args);
};

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
  <React.StrictMode>
    <AppProviders>
        <App />
      </AppProviders>
  </React.StrictMode>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
