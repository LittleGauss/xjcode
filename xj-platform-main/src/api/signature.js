import request from "@/utils/request";

// 上传签名
export function uploadSignature(data) {
  return request({
    url: "/sign-record/upload",
    method: "post",
    data,
  });
}

// 删除签名
export function deleteSignature(id) {
  return request({
    url: `/sign-record/${id}`,
    method: "delete",
  });
}

// 查询签名列表
export function listSignatures(params) {
  return request({
    url: "/sign-record/listAll",
    method: "get",
    params,
  });
}

// 获取最新签名
export function getLatestSignature() {
  return request({
    url: "/sign-record/latest",
    method: "get",
  });
}

// 获取图片预签名URL
export function getImageUrl(filePath) {
  return request({
    url: "/sign-record/getImageUrl",
    method: "get",
    params: { filePath },
  });
}

// 默认导出整个模块作为 signatureApi
export default {
  uploadSignature,
  deleteSignature,
  listSignatures,
  getLatestSignature,
  getImageUrl,
};
