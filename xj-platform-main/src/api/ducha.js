import request from "@/utils/request";

// 督察模块 API（占位实现，路径可按后端约定调整）
export const duchaApi = {
  // 列表查询
  listTasks(params) {
    // 后端实现为 GET /supervision/tasks?page=...&size=...&q=...
    const p = {
      page: params.page || 1,
      size: params.size || 10,
      q: params.q || params.q,
    };
    return request.get("/supervision/tasks", { params: p });
  },

  // 获取单条任务
  getTask(id) {
    return request.get(`/supervision/tasks/${id}`);
  },

  // 获取部门列表（后端：GET /department/list）
  getDepartments() {
    return request.get(`/department/list`);
  },

  // 获取用户列表（后端：GET /api/user/list），支持分页和用户名筛选
  getUsers(params) {
    const p = {
      page: params.page || 1,
      size: params.size || 100,
      username: params.username || "",
    };
    return request.get(`/api/user/list`, { params: p });
  },

  // 创建任务
  createTask(data) {
    // 如果包含 uploadIds，则通过查询参数传递（后端 Controller 接受 request param）
    let qs = "";
    if (
      data &&
      data.uploadIds &&
      Array.isArray(data.uploadIds) &&
      data.uploadIds.length
    ) {
      qs = "?" + data.uploadIds.map((id) => `uploadIds=${id}`).join("&");
    }
    // 去掉 body 中的 uploadIds 字段，实际任务数据保存在 body
    const body = Object.assign({}, data);
    if (body.uploadIds) delete body.uploadIds;
    return request.post(`/supervision/tasks${qs}`, body);
  },

  // 提交反馈（办结等）
  submitFeedback(taskId, payload) {
    let qs = "";
    if (
      payload &&
      payload.uploadIds &&
      Array.isArray(payload.uploadIds) &&
      payload.uploadIds.length
    ) {
      qs = "?" + payload.uploadIds.map((id) => `uploadIds=${id}`).join("&");
    }
    const body = Object.assign({}, payload);
    if (body.uploadIds) delete body.uploadIds;
    return request.post(`/supervision/tasks/${taskId}/feedback${qs}`, body);
  },

  // 导出任务（示例，返回 blob）
  exportTask(id) {
    return request.get(`/supervision/tasks/${id}/export`, {
      responseType: "blob",
    });
  },

  // 流程引擎：列出当前用户的流程任务（待办）
  listMyProcessTasks() {
    return request.get(`/supervision/process/tasks`);
  },

  // 根据业务 id 查询该业务关联的活动流程任务
  getProcessTasksByBusiness(bizId) {
    return request.get(`/supervision/process/tasks/by-business/${bizId}`);
  },

  // 完成流程任务（taskId 为 Flowable 任务 id），body 为流程变量（例如 { feedback: '...', finishDate: '2025-11-20' }）
  completeProcessTask(taskId, body) {
    return request.post(
      `/supervision/process/tasks/${taskId}/complete`,
      body || {}
    );
  },
  // 更新任务（编辑）
  updateTask(id, data) {
    return request.put(`/supervision/tasks/${id}`, data);
  },
  // 撤销任务（优先调用后端专用撤销接口，后端应同步处理流程待办）
  revokeTask(id) {
    return request.put(`/supervision/tasks/${id}/revoke`);
  },
};
