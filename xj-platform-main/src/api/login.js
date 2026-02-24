import request from "@/utils/request";

// 获取验证码
export function getCaptchaImage() {
  console.log("API验证码响应getCaptchaImage"); // 添加调试日志
  return request({
    url: "/captcha/image",
    method: "get",
  });
}

// 用户注册
export function register(data) {
  return request({
    url: "/user/register",
    method: "post",
    data,
  });
}

// 用户登录
export function login(data, params) {
  return request({
    url: "/user/login",
    method: "post",
    data,
    params,
  });
}

// 获取用户列表
export function getUserList(params) {
  return request({
    url: "/api/user/list",
    method: "get",
    params,
  });
}

// 新增用户
export function addUser(data) {
  return request({
    url: "/api/user",
    method: "post",
    data,
  });
}

// 编辑用户
export function updateUser(id, data) {
  return request({
    url: `/api/user/${id}`,
    method: "put",
    data,
  });
}

// 删除用户
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: "delete",
  });
}

// 修改用户状态
export function updateUserStatus(id, data) {
  return request({
    url: `/api/user/status/${id}`,
    method: "put",
    data,
  });
}

// 导出用户
export function exportUsers() {
  return request({
    url: "/api/user/export",
    method: "get",
    responseType: "blob",
  });
}

// 导入用户
export function importUsers(data) {
  return request({
    url: "/api/user/import",
    method: "post",
    data,
  });
}
