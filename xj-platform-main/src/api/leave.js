import http from "@/utils/request";

// 统一导出对象，便于组件以 leaveApi.xxx 调用
export const leaveApi = {
  // 提交请假申请：后端接收 LeaveProcessRequest 结构
  // requestBody 形如：{ leaveProcess: {...}, starter: "1", firstApprover: "2", secondApprover: "3" }
  submitLeave(requestBody) {
    return http({
      url: "/api/leave-process",
      method: "post",
      data: requestBody,
    });
  },

  // 获取个人请假记录
  getMyLeaves(userId) {
    return http.get(`/api/leave-process/my`, { params: { userId } });
  },

  // 获取我审批的记录
  getMyApprovals(userId) {
    return http.get(`/api/leave-process/my-approvals`, { params: { userId } });
  },

  // 完成任务并保存审批意见（含下一审批人）
  // 备注：若需传审批意见 ApprovalComment，请在 data 中传递
  approveLeave(taskId, approved, nextApprover, comment) {
    // 将布尔值统一序列化为字符串，避免某些服务端布尔绑定问题
    const approvedParam =
      approved === true || approved === "true" ? "true" : "false";
    const params = { approved: approvedParam };
    if (nextApprover != null && nextApprover !== "") {
      params.nextApprover = String(nextApprover);
    }
    // 额外稳健性：移除超出 32 位整型的 businessId，避免后端 JSON 绑定 400
    const payload = { ...(comment || {}) };
    if (
      payload.businessId != null &&
      (Number(payload.businessId) > 2147483647 ||
        Number(payload.businessId) < -2147483648)
    ) {
      delete payload.businessId;
    }
    if (
      payload.approverId != null &&
      !Number.isFinite(Number(payload.approverId))
    ) {
      delete payload.approverId;
    }
    // 调试日志（可按需注释）
    // console.debug('[approveLeave] params:', params, 'payload:', payload);
    return http({
      url: `/api/leave-process/complete/${taskId}`,
      method: "post",
      params,
      data: payload,
    });
  },

  // 获取当前用户任务
  getUserTasks() {
    return http({ url: "/api/leave-process/tasks", method: "get" });
  },
  // 别名：待办任务（供 Todo.vue 使用）
  getTodoTasks() {
    return http({ url: "/api/leave-process/tasks", method: "get" });
  },

  // 简单完成任务（若无需审批意见）
  completeTask(taskId) {
    return http({
      url: `/api/leave-process/complete/${taskId}`,
      method: "post",
    });
  },

  // 保存草稿（LeaveProcess）
  saveDraft(data) {
    return http.post("/api/leave-process/draft", data);
  },

  // 从草稿提交（查询参数：starter, firstApprover, secondApprover）
  submitFromDraft(draftId, params) {
    return http.post(`/api/leave-process/submit-from-draft/${draftId}`, null, {
      params,
    });
  },

  // 获取我的草稿（Apply.vue 调用别名）
  getDrafts(userId) {
    return http.get(`/api/leave-process/drafts`, { params: { userId } });
  },

  // 删除草稿（需要 draftId，可选 userId 校验）
  deleteDraft(draftId, params) {
    return http.delete(`/api/leave-process/draft/${draftId}`, { params });
  },

  // 审批意见列表
  getComments(leaveId) {
    return http.get(`/api/leave-process/comments/${leaveId}`);
  },

  // —— 附件相关（使用 AttachmentController 暴露的接口）——
  // 查询请假单附件列表
  getAttachments(leaveId) {
    return http.get(`/api/attachments/leave/${leaveId}`);
  },

  // 上传请假单附件（multipart/form-data）
  // 调用后端接口: POST /api/attachments/leave
  // 参数通过 FormData 传递: file (MultipartFile), leaveId (Long), uploadUserId (Long)
  // 留下灵活调用：accepts (leaveId, formData, uploadUserId)
  uploadAttachment(leaveId, formData, uploadUserId) {
    // 确保 formData 是 FormData 实例
    let fd = formData;
    if (!(fd instanceof FormData)) {
      fd = new FormData();
      if (formData && formData.file) fd.append("file", formData.file);
    }
    // 附加必要的 request params 为 form data 字段（后端使用 @RequestParam 接收）
    if (leaveId != null) fd.append("leaveId", String(leaveId));
    if (uploadUserId != null) fd.append("uploadUserId", String(uploadUserId));

    // 不显式设置 Content-Type，让 axios 自动设置 boundary
    return http.post(`/api/attachments/leave`, fd);
  },

  // 删除附件（需要 operatorId）
  deleteAttachment(id, params) {
    return http.delete(`/api/attachments/${id}`, { params });
  },

  // 提交销假申请
  submitReportBack(taskId, data) {
    return http.post(`/api/leave-process/report-back/${taskId}`, data);
  },

  // ==========================================
  // 【新增】报表统计相关接口
  // ==========================================

  /**
   * 获取部门年度请假统计
   * @param {Object} params { year: 2025, deptId: 101 }
   */
  getDepartmentStats(params) {
    return http.get("/api/leave-process/stats/department", { params });
  },

  /**
   * 获取工龄与年假天数对应规则表
   * 用于前端计算员工年假额度
   */
  getSeniorityTable() {
    return http.get("/api/leave-process/seniority-rules");
  },

  // 撤回请假申请
  withdrawLeave(leaveId, userId) {
    // POST /api/leave-process/withdraw/{id}?userId={userId}
    return http.post(`/api/leave-process/withdraw/${leaveId}`, null, {
      params: { userId },
    });
  },
};

// 兼容旧的具名导出（如有其他页面直接按函数名导入）
export const {
  submitLeave,
  getMyLeaves,
  getMyApprovals,
  approveLeave,
  getUserTasks,
  completeTask,
  saveDraft,
  submitFromDraft,
  getDrafts,
  getComments,
  getAttachments,
  deleteAttachment,
  deleteDraft,
  submitReportBack,
  // 新增导出
  getDepartmentStats,
  getSeniorityTable,
  withdrawLeave,
} = leaveApi;
