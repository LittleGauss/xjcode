import axios from "axios";
import { Message, MessageBox } from "element-ui";
import store from "@/store"; // 引入 Vuex Store
import { getToken } from "@/utils/auth";

// request.js 最开头
// 创建axios实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // 关键：绑定后端服务地址
  timeout: 15000, // 请求超时时间
});
export default service;

// request拦截器
service.interceptors.request.use(
  (config) => {
    console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`, {
      params: config.params,
      data: config.data,
    });
    if (getToken()) {
      console.log("store.getters.token");
      config.headers["Authorization"] = getToken(); // 让每个请求携带自定义token 请根据实际情况自行修改
    }
    return config;
  },
  (error) => {
    // Do something with request error
    console.error("[API Request Error]", error); // for debug
    return Promise.reject(error);
  }
);

// respone拦截器
service.interceptors.response.use(
  (response) => {
    // 在响应后添加日志
    console.log(
      `[API Response] ${response.config.method.toUpperCase()} ${
        response.config.url
      }`,
      {
        status: response.status,
        data: response.data,
      }
    );
    // 如果是文件下载（responseType=blob），直接返回原始响应，交由调用方处理
    if (
      response &&
      response.config &&
      response.config.responseType === "blob"
    ) {
      return response;
    }
    /**
     * code为非200是抛错 可结合自己业务进行修改
     */
    const res = response.data;

    // 关键修改：处理没有code字段的情况
    if (res && typeof res === "object" && res.code !== undefined) {
      // 如果有code字段，按照原有逻辑处理
      console.log("res.code:", res.code);

      if (res.code != 200) {
        Message({
          message: res.message,
          type: "error",
          duration: 1 * 1000,
        });

        // 401:无权访问或者未登录;
        if (res.code == 401) {
          MessageBox.confirm(
            "你已被登出，可以取消继续留在该页面，或者重新登录",
            "确定登出",
            {
              confirmButtonText: "重新登录",
              cancelButtonText: "取消",
              type: "warning",
            }
          )
            .then(() => {
              store.dispatch("FedLogOut").then(() => {
                location.reload();
              });
            })
            .catch(() => {
              console.log("用户取消登录");
            });
        }

        console.log("reject:", res);

        // 400:验证码问题; 不需要直接reject 也算正常流程
        if (res.code == 400) {
          return response.data;
        }
        return Promise.reject("error");
      } else {
        console.log("response:ok");
        return response.data;
      }
    } else {
      // 如果没有code字段（如直接返回数组），直接返回数据
      return response.data;
    }
  },
  (error) => {
    console.log("err" + error); // for debug
    // 针对文件下载失败但服务端返回 JSON 的场景，尝试解析 blob 错误信息
    const resp = error && error.response;
    if (
      resp &&
      resp.config &&
      resp.config.responseType === "blob" &&
      resp.data instanceof Blob
    ) {
      const ct = String(
        (resp.headers &&
          (resp.headers["content-type"] || resp.headers["Content-Type"])) ||
          ""
      ).toLowerCase();
      if (ct.includes("application/json")) {
        const reader = new FileReader();
        return new Promise((resolve, reject) => {
          reader.onload = () => {
            try {
              const obj = JSON.parse(reader.result);
              Message({
                message: obj.message || obj.msg || "下载失败",
                type: "error",
                duration: 3000,
              });
              reject(new Error(obj.message || obj.msg || "下载失败"));
            } catch (e) {
              Message({
                message: error.message,
                type: "error",
                duration: 3000,
              });
              reject(error);
            }
          };
          reader.onerror = () => {
            Message({ message: error.message, type: "error", duration: 3000 });
            reject(error);
          };
          reader.readAsText(resp.data, "utf-8");
        });
      }
    }
    Message({
      message: error.message,
      type: "error",
      duration: 3 * 1000,
    });
    return Promise.reject(error);
  }
);
