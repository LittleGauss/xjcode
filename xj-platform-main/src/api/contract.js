import request from "@/utils/request";

/**
 * 合同相关后端接口封装
 * 目标：集中管理 contract 接口，便于维护与单元测试
 */
const base = "/contract-review";

const contractApi = {
  // 获取合同列表（支持分页/查询参数）
  async getContractList(params) {
    return request({ url: base, method: "get", params });
  },

  // 获取合同详情
  async getContractDetail(id) {
    return request({ url: `${base}/${id}`, method: "get" });
  },

  // 创建/提交合同
  async createContract(data) {
    return request({ url: base, method: "post", data });
  },

  // 更新合同（退回修改场景）
  async updateContract(id, data) {
    return request({ url: `${base}/${id}`, method: "put", data });
  },

  // 删除合同
  async deleteContract(id) {
    return request({ url: `${base}/${id}`, method: "delete" });
  },

  // 合同统计（可传 department 参数）
  async getStatistics(params) {
    return request({ url: `${base}/statistics`, method: "get", params });
  },

  // 上传合同附件（multipart/form-data）
  async uploadAttachment(contractId, formData) {
    return request({
      url: `${base}/${contractId}/attachment`,
      method: "post",
      data: formData,
    });
  },

  // 获取附件临时下载URL（后端返回 { id, name, url }）
  async getAttachmentDownloadUrl(contractId, attId, expireSeconds = 600) {
    return request({
      url: `${base}/${contractId}/attachment/${attId}/download`,
      method: "get",
      params: { expire: expireSeconds },
    });
  },

  // 获取合同审批意见
  async getApprovalComments(contractId) {
    return request({ url: `${base}/comments/${contractId}`, method: "get" });
  },
  // 启动合同审批流程
  async startContractProcess(data) {
    return request({ url: "/api/contract-process", method: "post", data });
  },

  // 【新增】2. 根据合同ID获取当前用户的待办任务ID
  // 对应后端: GET /api/contract-process/task-by-contract/{contractId}
  async getMyTask(contractId, userId) {
    return request({
      url: `/api/contract-process/task-by-contract/${contractId}`,
      method: "get",
      params: { userId },
    });
  },

  // 【新增】3. 提交审批结果 (通用接口：通过/驳回)
  // 对应后端: POST /api/contract-process/complete
  // data 结构: { taskId, contractId, approved, comment }
  async completeTask(data) {
    return request({
      url: "/api/contract-process/complete",
      method: "post",
      data,
    });
  },
};
export const permissionApi = {
  // 获取权限列表
  getPermissionList() {
    return request.get("/permission/list");
  },

  // 分配权限
  assignPermissions(roleId, permissions) {
    return request.post(`/permission/assign/${roleId}`, { permissions });
  },

  // 新增角色
  addPermission(data) {
    return request.post("/permission", data);
  },
};

export default contractApi;
