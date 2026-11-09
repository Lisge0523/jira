import qs from "qs";
import * as auth from "../auth-provider";
import { useCallback } from "react";

const apiUrl = process.env.REACT_APP_API_URL;

interface Config extends RequestInit {
  data?: object;
  /**
   * 启动时探测登录态用：401 只当作"未登录"，
   * 不触发 logout + reload，否则页面会无限刷新。
   */
  skipAuthRedirect?: boolean;
}

export const http = async (
  endpoint: string,
  { data, skipAuthRedirect, headers, ...customConfig }: Config = {}
) => {
  const config = {
    method: "GET",
    // 凭证在 HttpOnly Cookie 里，必须带 credentials 浏览器才会自动发送
    credentials: "include" as RequestCredentials,
    headers: {
      "Content-Type": data ? "application/json" : "",
    },
    ...customConfig,
  };

  if (config.method.toUpperCase() === "GET") {
    endpoint += `?${qs.stringify(data)}`;
  } else {
    config.body = JSON.stringify(data || {});
  }

  // axios 和 fetch 的表现不一样，axios可以直接在返回状态不为2xx的时候抛出异常
  return window
    .fetch(`${apiUrl}/${endpoint}`, config)
    .then(async (response) => {
      if (response.status === 401) {
        if (!skipAuthRedirect) {
          await auth.logout();
          window.location.reload();
        }
        return Promise.reject({ message: "请重新登录" });
      }
      const body = await response.json();
      if (response.ok) {
        return body.data;
      } else {
        return Promise.reject(body);
      }
    });
};

// JS 中的typeof，是在runtime时运行的
// return typeof 1 === 'number'

// TS 中的typeof，是在静态环境运行的
// return (...[endpoint, config]: Parameters<typeof http>) =>
export const useHttp = () => {
  // 凭证由浏览器自动携带，这里不再需要从 user 里取 token
  return useCallback(
    (...[endpoint, config]: Parameters<typeof http>) => http(endpoint, config),
    []
  );
};
