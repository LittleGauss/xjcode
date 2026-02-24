import request from "@/utils/request";

// 待办相关接口
export const pendingApi = {
  // 获取待办事件列表
  getPendingTasks(query) {
    return request({
      url: "/pending/tasks",
      method: "get",
      params: query,
    });
  },
};
