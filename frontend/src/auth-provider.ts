// 在真实环境中，如果使用firebase这种第三方auth服务的话，本文件不需要开发者开发
import { User } from "./types/user";

const apiUrl = process.env.REACT_APP_API_URL;

// 登录凭证由后端写进 HttpOnly Cookie，前端读不到、也不需要读。
// 所有请求都必须带 credentials: "include"，浏览器才会自动携带 Cookie。
export const handleUserResponse = ({ user }: { user: User }) => user;

export const login = (data: { username: string; password: string }) => {
  return fetch(`${apiUrl}/login`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  }).then(async (response) => {
    const body = await response.json();
    if (response.ok) {
      return handleUserResponse(body.data);
    } else {
      return Promise.reject(body);
    }
  });
};
export const register = (data: { username: string; password: string }) => {
  return fetch(`${apiUrl}/register`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  }).then(async (response) => {
    const body = await response.json();
    if (response.ok) {
      return handleUserResponse(body.data);
    } else {
      return Promise.reject(body);
    }
  });
};
// HttpOnly Cookie 前端删不掉，只能让后端用 Max-Age=0 清
export const logout = async () => {
  await fetch(`${apiUrl}/logout`, {
    method: "POST",
    credentials: "include",
  }).catch(() => undefined);
};
