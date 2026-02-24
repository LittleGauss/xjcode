import request from "@/utils/request";

// 获所有的
export function getValidNotices(params) {
  return request.get("/consumableNotice/validNotices", { params });
}
// 获前三条有效的
export function validNewnotices() {
  return request.get("/consumableNotice/validNewnotices");
}
// 根据ID获取公示详情
export function getNoticeById(id) {
  return request.get(`/consumableNotice/${id}`);
}

// 创建入库公示
export function createInboundNotice(data) {
  return request.post("/consumableNotice/inbound", data);
}

// 创建报废公示
export function createScrapNotice(data) {
  return request.post("/consumableNotice/scrap", data);
}
// 获取部门列表（后端：GET /department/list）
export function getAllDepartments() {
  return request.get(`/department/list`);
}
// 创建领用统计公示
export function createStatisticalNotice(data) {
  return request.post("/consumableNotice/statistical", data);
}

// 删除所有过期公示
export function deleteExpiredNotices() {
  return request.delete("/consumableNotice/expired");
}

//  批准公示 (POST) 含修改标题
export function approveNotice(data) {
  return request.post("/consumableNotice/approve", data);
}

// 删除单条公示 (DELETE)
export function deleteNotice(id) {
  return request.delete(`/consumableNotice/${id}`);
}

// 新增：批量删除公示
export function batchDeleteNotices(data) {
  return request.post("/consumableNotice/batchDelete", data);
}
