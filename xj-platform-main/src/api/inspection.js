import request from "@/utils/request";

// 检查任务核心API
export const inspectionApi = {
  // 发起检查任务
  initiateTask(formData) {
    return request({
      url: "/inspection/initiate",
      method: "post",
      data: formData,
    });
  },

  // 检查员接收任务 任务ID  执行人ID
  receiveTask(assignmentId, inspectorId) {
    return request({
      url: `/inspection/receive/${assignmentId}`,
      method: "post",
      params: { inspectorId },
    });
  },

  // 检查员完成任务
  completeTask(formData) {
    return request({
      url: `/inspection/complete`,
      method: "post",
      data: formData,
    });
  },

  // 负责人审核任务（通过/驳回）
  auditTask(assignmentId, initiatorId, auditResult, rejectReason) {
    return request({
      url: `/inspection/audit/${assignmentId}`,
      method: "post",
      params: {
        initiatorId,
        auditResult,
        rejectReason,
      },
    });
  },
  // 检查员提交重做任务 直接使用检查员完成任务亦可
  // completeRedoTask(formData) {
  //   return request({
  //     url: "/inspection/redo/complete",
  //     method: "post",
  //     data: formData,
  //   });
  // },

  // 动态调整检查人员
  adjustInspectors(taskId, addInspectorIds, removeInspectorIds) {
    return request({
      url: `/inspection/adjust/${taskId}`,
      method: "post",
      params: {
        addInspectorIds: addInspectorIds?.join(","),
        removeInspectorIds: removeInspectorIds?.join(","),
      },
    });
  },

  // 查询任务详情
  getTaskDetail(taskId) {
    return request.get(`/inspection/detail/${taskId}`);
  },

  // 查询检查员的待办任务
  getTodoTasks(inspectorId) {
    return request.get(`/inspection/todo/${inspectorId}`);
  },

  // 计算任务完成率
  calculateCompletionRate(taskId) {
    return request.get(`/inspection/completionRate/${taskId}`);
  },

  // 终止任务
  terminateTask(taskId, reason, initiatorId) {
    return request({
      url: `/inspection/terminate/${taskId}`,
      method: "post",
      params: { reason, initiatorId },
    });
  },

  // 获取检查员任务列表
  getInspectorTasks(params) {
    return request({
      url: "/inspection/inspector/tasks",
      method: "get",
      params,
    });
  },

  // 查询流程状态  只有COMPLETED 或RUNNING 或者UNKNOWN
  getProcessStatus(taskId) {
    return request.get(`/inspection/process/status/${taskId}`);
  },

  // 根据发起人ID查询任务列表
  getTaskListByInitiatorId(initiatorId) {
    return request.get(`/inspection/list`, {
      params: { initiatorId },
    });
  },

  // 根据任务ID查询分配列表
  getAssignmentsByTaskId(taskId) {
    return request.get(`/inspection/assignments/${taskId}`);
  },

  // 手动触发逾期更新（测试用）
  updateOverdueStatus() {
    return request.get(`/inspection/updateOverdue`);
  },

  /**
   * 从完整路径中提取文件名
   * @param {string} path - 完整文件路径
   * @param {number} position - 文件在路径中的位置（从0开始）
   * @returns {string} 文件名
   */
  extractFileName(path, position = 3) {
    if (!path) return "";
    const parts = path.split("/");
    return parts[position] || "";
  },
  // 新增方法专门处理去掉时间戳
  extractFileNameWithoutTimestamp(path, position = 3) {
    if (!path) return "";
    const parts = path.split("/");
    const fullFileName = parts[position] || "";

    // 如果包含多个下划线，只去掉第一个下划线前面的部分
    const splitResult = fullFileName.split("_");
    if (splitResult.length > 1) {
      // 从第二个元素开始拼接（去掉第一个时间戳部分）
      return splitResult.slice(1).join("_");
    }

    return fullFileName;
  },
};

// 模板相关API
export const templateApi = {
  // 上传模板文件到MinIO
  uploadTemplate(formData) {
    return request({
      url: "/inspection/template/upload",
      method: "post",
      data: formData,
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  // 获取模板文件列表
  getTemplateList() {
    return request.get("/inspection/template/list");
  },

  // 下载模板文件
  downloadTemplate(templateId) {
    return request({
      url: `/inspection/template/download/${templateId}`,
      method: "get",
    });
  },

  // 下载批量导入用户的目标文件
  downloadUserTemplate() {
    return request({
      url: `/inspection/template/downloadUserTemplate`,
      method: "get",
    });
  },

  // 下载模板文件
  downloadTemplateWithPath(inspectionFormUrl) {
    return request({
      url: `/inspection/template/downloadwith`,
      method: "post",
      data: {
        inspectionFormUrl: inspectionFormUrl,
      },
    });
  },

  // 删除模板文件
  deleteTemplate(templateId) {
    return request.delete(`/inspection/template/delete/${templateId}`);
  },
};
