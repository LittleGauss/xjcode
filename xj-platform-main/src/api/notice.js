import request from "@/utils/request";

// 行政公示相关接口
export const noticeApi = {
  // 获取公示列表
  getNoticeList(params) {
    //  注册为 GET /administrative-notice
    return request.get("/administrative-notice", { params });
  },

  // 获取最新三条公示
  getCurrentThree() {
    return request.get("/administrative-notice/current_three");
  },

  // 获取单个公示
  getNoticeById(id) {
    return request.get(`/administrative-notice/${id}`);
  },

  // 标记已读
  markRead(id, userId) {
    return request.post(`/administrative-notice/${id}/read`, null, {
      params: { userId },
    });
  },

  // 获取某条公示已读数
  getReadCount(id) {
    return request.get(`/administrative-notice/${id}/read/count`);
  },

  // 获取未读用户列表
  getUnreadUsers(id) {
    return request.get(`/administrative-notice/${id}/read/unread-users`);
  },

  // 获取已读用户列表
  getReadUsers(id) {
    return request.get(`/administrative-notice/${id}/read/read-users`);
  },

  // 导出未读用户（返回 base64 CSV）
  exportUnread(id) {
    // 请求二进制流用于下载 xlsx
    return request.get(`/administrative-notice/${id}/read/export`, {
      responseType: "blob",
      validateStatus: () => true, // 接受所有状态，交给调用方处理
    });
  },

  // 新增或更新公示（使用 POST /administrative-notice）
  saveNotice(data) {
    return request.post("/administrative-notice", data);
  },

  // 删除公示
  deleteNotice(id) {
    return request.delete(`/administrative-notice/${id}`);
  },

  // 上传文件（接收 base64 JSON）
  uploadFile(payload) {
    // payload: either File or { file: File, name?: string }
    const file =
      payload && (payload.file || payload instanceof File)
        ? payload.file || payload
        : null;
    if (!file) {
      // fallback to previous behavior (/base64)
      return request.post("/upload", payload);
    }
    const form = new FormData();
    form.append("file", file, payload.name || file.name);
    // call real backend endpoint that persists to MinIO
    return request.post("/api/attachments/notice", form, {
      headers: { "Content-Type": "multipart/form-data" },
    });
  },

  // 获取已上传文件（按 id）
  getUploadById(id) {
    return request.get(`/uploads/${id}`);
  },

  // 查询 uploads：如果传 name，则会被  当作 ?name=xxx 处理；否则返回全部
  queryUploads(params) {
    // params 可以是 { name: 'xxx' } 或空
    return request.get("/uploads", { params });
  },
  // 获取当前用户未读公示数量（前端传 userId）
  getUnreadCount(userId) {
    return request.get(`/administrative-notice/read/unread-count`, {
      params: { userId },
    });
  },
};
