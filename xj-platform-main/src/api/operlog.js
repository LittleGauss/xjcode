import request from "@/utils/request";

// 查询操作日志列表
export function list(query) {
  return request({
    url: "/operlog/list",
    method: "get",
    params: query,
  });
}

// 删除操作日志
export function delOperlog(operId) {
  return request({
    // 这里的 operId 如果是数组 [1,2,3] 会自动拼接到url后面变成 /operlog/1,2,3
    url: "/operlog/" + operId,
    method: "delete",
  });
}
// 查询总登录用户数量
export function getUserIndex() {
  return request({
    url: "/operlog/userIndex",
    method: "get",
  });
}
