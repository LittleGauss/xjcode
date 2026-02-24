import { getToken, removeToken, removeUserToken } from "@/utils/auth";

const user = {
  state: {
    token: getToken(), // 从 Cookie 中读取 token
    permissions: [], // 新增：当前登录用户权限代码数组
  },

  mutations: {
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions || [];
    },
  },

  actions: {
    // 前端 登出
    FedLogOut() {
      return new Promise((resolve) => {
        console.log("FedLogOut:getToken()", getToken());
        if (!getToken()) {
          resolve();
          return;
        }
        removeToken();
        removeUserToken();
        resolve();
      });
    },
  },
};

export default user;
