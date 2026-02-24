import request from "@/utils/request";

// 车辆管理相关接口
export const vehicleApi = {
  // 获取车辆列表（ 使用 /api/vehicles）
  getVehicles(params) {
    return request.get("/api/vehicles", { params });
  },

  // 根据 id 获取车辆详情（ 没有单独的 detail 接口，但保留此方法便于后端对接）
  getVehicle(id) {
    return request.get(`/api/vehicle/${id}`);
  },

  // 根据车牌查询（有些后端会提供按 plateNumber 的查询）
  getByPlate(plateNumber) {
    return request.get("/api/vehicle", { params: { plateNumber } });
  },

  // 新增或更新车辆（ 使用 POST /api/vehicle）
  saveVehicle(data) {
    return request.post("/api/vehicle", data);
  },

  // 删除车辆（按 id）
  deleteVehicle(id) {
    return request.delete(`/api/vehicle/${id}`);
  },

  // 获取费用/记录列表（ 使用 /api/records）
  getRecords(params) {
    return request.get("/api/records", { params });
  },

  // 派车记录相关接口（后端真实接口）
  // 获取所有派车记录或分页
  getDispatchRecords(params) {
    return request.get("/api/dispatch-records", { params });
  },

  // 新增派车记录
  saveDispatchRecord(data) {
    return request.post("/api/dispatch-record", data);
  },

  // 更新派车记录（按 id）
  updateDispatchRecord(id, data) {
    return request.put(`/api/dispatch-record/${id}`, data);
  },

  // 删除派车记录（按 id）
  deleteDispatchRecord(id) {
    return request.delete(`/api/dispatch-record/${id}`);
  },

  // 新增/编辑费用记录（ 使用 /api/record）
  saveRecord(data) {
    return request.post("/api/record", data);
  },

  // 上传文件（与 noticeApi 保持一致）
  uploadFile(payload) {
    // payload: { name, base64 } 或 FormData，根据后端实现调整
    return request.post("/upload", payload);
  },

  // 上传派车记录附件到后端（后端会把文件保存到 MinIO）
  uploadDispatchAttachment(formData) {
    // formData 应包含 file 字段
    return request.post("/api/attachments/dispatch", formData);
  },

  // 删除派车记录附件（通过 filePath）
  deleteDispatchAttachment(filePath) {
    return request.delete(`/api/attachments/dispatch`, {
      params: {
        filePath,
      },
    });
  },

  // 查询上传记录（兼容  中 //uploads）
  queryUploads(params) {
    return request.get("/uploads", { params });
  },

  // 周期调整（保险费、年审费、备注）
  savePeriodAdjustments(data) {
    // 后端建议：POST /api/vehicle/period-adjustments
    return request.post("/api/vehicle/period-adjustments", data);
  },
  getPeriodAdjustments(params) {
    // 后端建议：GET /api/vehicle/period-adjustments?periodType=month&year=2025
    return request.get("/api/vehicle/period-adjustments", { params });
  },
};

export default vehicleApi;
