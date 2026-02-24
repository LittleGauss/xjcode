import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import VueCompositionAPI from "@vue/composition-api";
import "@/styles/index.scss"; // global css
import "element-ui/lib/theme-chalk/index.css";
import "./assets/global.css";
import "./styles/input.css";
import "./assets/css/global.scss";
import request from "./utils/request";
import { permissionApi } from "@/api/permissions";
import PdfViewer from "@/views/PdfViewer.vue";
import { getUserToken } from "./utils/auth";

// ==================== 生产环境禁用 console ====================
if (process.env.NODE_ENV == "production") {
  // 保存原始的 error 和 warn 方法，用于生产环境错误追踪
  const originalError = console.error;
  const originalWarn = console.warn;

  // 创建一个空函数
  const noop = () => {};

  // 需要禁用的 console 方法列表
  const disabledMethods = [
    "log",
    "debug",
    "info",
    "table",
    "time",
    "timeEnd",
    "timeLog",
    "trace",
    "dir",
    "dirxml",
    "group",
    "groupCollapsed",
    "groupEnd",
    "clear",
    "count",
    "countReset",
    "assert",
    "profile",
    "profileEnd",
    "timeStamp",
  ];

  // 禁用指定方法
  disabledMethods.forEach((method) => {
    if (typeof console[method] === "function") {
      console[method] = noop;
    }
  });

  // 保留 error 和 warn，便于生产环境问题追踪
  console.error = originalError;
  console.warn = originalWarn;

  // 可选：覆盖全局的 console 对象，更彻底
  window.console = console;
}

Vue.config.productionTip = false;
Vue.use(ElementUI);
Vue.use(VueCompositionAPI); // 注册后才能使用 Composition API
//全局注册
Vue.prototype.request = request;
// main.js 中的全局权限方法
Vue.prototype.$hasPermission = function (permission) {
  // 1. 先从 Vuex store 获取
  let permissions = this.$store?.getters?.permissions || [];

  // 2. 如果 Vuex 中没有，尝试从 sessionStorage 获取并同步到 Vuex
  if (permissions.length === 0) {
    try {
      const storedUserInfo = getUserToken();
      permissions = storedUserInfo.permissions || [];

      // 如果从 sessionStorage 获取到权限，更新到 Vuex
      if (permissions.length > 0) {
        this.$store.commit("SET_PERMISSIONS", permissions);
        console.log("全局方法中同步权限到 Vuex:", permissions);
      }
    } catch (e) {
      console.warn("从 getUserToken 获取权限失败:", e);
    }
  }
  // 3. 核心改动：判断传入的 permission 是字符串还是数组
  if (typeof permission === "string") {
    // 3.1 如果是字符串（单个权限），使用 hasPermission
    return permissionApi.hasPermission(permissions, permission);
  } else if (Array.isArray(permission)) {
    // 3.2 如果是数组（多个权限），使用 hasAnyPermission（满足任意一个即可）
    return permissionApi.hasAnyPermission(permissions, permission);
  }
  // 如果传入的既不是字符串也不是数组，默认返回 false
  return false;
};

Vue.component("PdfViewer", PdfViewer);

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
