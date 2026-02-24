import request from "@/utils/request";

export const getInventoryList = () => request.get("/consumable-goods/list");
export const addInventory = (data) =>
  request.post("/consumable-goods/saveOrUpdate", data);

export const updateInventory = (data) =>
  request.post("/consumable-goods/saveOrUpdate", data); // 改为后端实际的接口路径
export const deleteInventory = (id) =>
  request.delete(`/consumable-goods/${id}`);

// 新增：批量导入物品接口（关键，解决batchAddInventory未定义）
export const batchAddInventory = (goodsList) =>
  request.post("/consumable-goods/batch-save", goodsList); // 对应后端批量接口

// ===== 分类接口 =====
export const getCategoryList = () => request.get("/api/category/list");
// 在 inventory.js 中添加分类管理接口
export const addCategory = (categoryName) =>
  request.post("/api/category/add", { categoryName });
export const deleteCategory = (id) =>
  request.delete(`/api/category/delete/${id}`);

// ===== 申请接口 =====
export const getApplications = () =>
  request.get("/api/consumable/application/all");
export const addApplication = (data) =>
  request.post("/api/applications/add", data);
export const updateApplication = (data) =>
  request.put("/api/applications/update", data);
export const deleteApplication = (id) =>
  request.delete(`/api/applications/delete/${id}`);

// 新增：提交耗材领用申请
export const submitConsumableApplication = (data) => {
  return request.post("/api/consumable/application/submit", data);
};

export const getApplicationById = (id) =>
  request.get(`/api/consumable/application/findbyId/${id}`);

// 新增：更新申请状态（用于审批）
export const updateApplicationStatus = (data) => {
  return request.post("/api/consumable/application/update-status", data);
};

// 新增：获取我的申请列表（根据申请人ID）
export const getMyApplications = (applicantId) => {
  return request.get(
    `/api/consumable/application/my?applicantId=${applicantId}`
  );
};

// 新增：获取待审批列表（根据审批人ID）
export const getPendingApprovalApplications = (userId) => {
  return request.get(`/api/consumable/application/todo-tasks?userId=${userId}`);
};

// 新增：撤回申请（通过删除接口）
export const withdrawApplication = (id) => {
  return request.delete(`/api/consumable/application/${id}`);
};

// 新增：批准申请（通过状态更新接口）
export const approveApplication = (requestData) => {
  return request.post(
    `/api/consumable/application/first-approval`,
    requestData
  );
};

// 新增：拒绝申请（通过状态更新接口）
export const rejectApplication = (requestData) => {
  return request.post(
    `/api/consumable/application/first-approval`,
    requestData
  );
};

export const approveFinalApplication = (requestData) => {
  return request.post(
    `/api/consumable/application/final-approval`,
    requestData
  );
};

// 新增：拒绝申请（通过状态更新接口）
export const rejectFinalApplication = (requestData) => {
  return request.post(
    `/api/consumable/application/final-approval`,
    requestData
  );
};

/**
 * 获取审批历史（根据审批人ID）
 */
export const getApprovalHistory = (approverId) => {
  return request.get(
    `/api/consumable/application/approval-history?approverId=${approverId}`
  );
};

// ===== 部门统计 =====
export const getDepartmentStats = () => request.get("/api/department/stats");

// ===== 超级管理员审批接口 =====
// 新增：一级批准申请（超级管理员专用）
export const firstApproveApplication = (id, approverName, comment = "") => {
  return request.post(`/api/consumable/application/admin/first-approve/${id}`, {
    approverName,
    comment,
  });
};

// 新增：一级拒绝申请（超级管理员专用）
export const firstRejectApplication = (id, approverName, comment = "") => {
  return request.post(`/api/consumable/application/admin/first-reject/${id}`, {
    approverName,
    comment,
  });
};

// 新增：二级批准申请（超级管理员专用）
export const finalApproveApplication = (
  id,
  approverName,
  comment = "",
  distributeQuantity = null
) => {
  return request.post(`/api/consumable/application/admin/final-approve/${id}`, {
    approverName,
    comment,
    distributeQuantity,
  });
};

// 新增：二级拒绝申请（超级管理员专用）
export const finalRejectApplication = (id, approverName, comment = "") => {
  return request.post(`/api/consumable/application/admin/final-reject/${id}`, {
    approverName,
    comment,
  });
};

// 新增：获取所有申请（超级管理员专用）
export const getAllApplications = () => {
  return request.get("/api/consumable/application/admin/all");
};

// 新增：按状态筛选申请（超级管理员专用）
export const getApplicationsByStatus = (status) => {
  return request.get(
    `/api/consumable/application/admin/by-status?status=${status}`
  );
};

// 获取出入库记录
export function getInOutRecords(params) {
  return request({
    url: "/consumable-goods/in-out-records/list",
    method: "get",
    params,
  });
}

// 导出出入库记录
export function exportInOutRecords(params) {
  return request({
    url: "/consumable-goods/in-out-records/export",
    method: "get",
    params,
    responseType: "blob", // 重要：告诉axios返回二进制数据
  });
}

// 获取出入库统计
export function getInOutStatistics(params) {
  return request({
    url: "/consumable-goods/in-out-records/statistics",
    method: "get",
    params,
  });
}

// 获取供货商列表（从现有数据中获取）
export function getSuppliers() {
  return request({
    url: "/consumable-goods/in-out-records/suppliers",
    method: "get",
  });
}

// 报废申请API
export const scrapApi = {
  /** 提交报废申请 */
  submitScrap(data) {
    return request.post("/consumable/scrap/submit", data);
  },

  /** 获取我的报废申请列表（后端会根据applyUserId返回） */
  getMyScrap() {
    return request.get("/consumable/scrap/my-list");
  },

  /** 获取待我审批的报废申请列表（无需传审批人ID，由后端根据当前登录用户判断） */
  getToApproveScrap() {
    return request.get("/consumable/scrap/to-approve-list");
  },

  /** 撤回报废申请（当前登录用户判断） */
  withdrawScrap(scrapId) {
    return request.post("/consumable/scrap/withdraw", null, {
      params: { scrapId }, // 用 params 传递，axios 会自动拼到URL后
    });
  },

  /** 后保部审核（审批人由后端自动获取当前登录用户） */
  reviewScrap(data) {
    return request.post("/consumable/scrap/review", data);
  },

  /** 分管领导审批（审批人由后端自动获取当前登录用户） */
  approveScrap(data) {
    return request.post("/consumable/scrap/approve", data);
  },

  /** 分页查询报废单列表 */
  getScrapList(params) {
    return request.get("/consumable/scrap/list", { params });
  },

  /** 获取报废申请详情（含明细） */
  getScrapDetail(scrapId) {
    return request.get(`/consumable/scrap/${scrapId}`);
  },

  /** 获取分管领导列表（用于选择下一级审批人） */
  getViceLeaders() {
    return request.get("/consumable/scrap/vice-leaders");
  },
};

// ===== 入库审批API =====
export const submitStockInApplication = (data) => {
  return request.post("/consumable/stock-in-approval/submit", data);
};

export const approveStockIn = (approvalId, remark) => {
  return request.post("/consumable/stock-in-approval/approve", null, {
    params: { approvalId, remark },
  });
};

export const rejectStockIn = (approvalId, rejectReason) => {
  return request.post("/consumable/stock-in-approval/reject", null, {
    params: { approvalId, rejectReason },
  });
};

export const getMyStockInApplications = () => {
  return request.get("/consumable/stock-in-approval/my-list");
};

export const getPendingStockInApprovals = () => {
  return request.get("/consumable/stock-in-approval/pending-list");
};
