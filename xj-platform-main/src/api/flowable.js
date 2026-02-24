import request from "@/utils/request";
export const pendingApi = {
  // 获取流程图 原来
  getProcessDiagramSvg(processInstanceId) {
    return request.get(`/flowable/diagram/${processInstanceId}`);
  },

  // 获取流程图SVG 新的
  getProcessDiagram(processInstanceId) {
    return request({
      url: `/flowable/svg/${processInstanceId}`,
      method: "get",
      // 重要：指定响应类型为文本
      responseType: "text",
      // 设置Accept头，告诉后端我们想要SVG
      headers: {
        Accept: "image/svg+xml",
      },
    });
  },

  getFlowSvg(processInstanceId) {
    return request.get(`/flowable/diagram/svg/${processInstanceId}`);
  },

  getActiveNodes(processInstanceId) {
    return request.get(`/flowable/active-nodes/${processInstanceId}`);
  },
};
