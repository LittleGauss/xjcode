import Vue from "vue";
import VueRouter from "vue-router";
import Contract from "../views/contract-management.vue";
import Leave from "../views/leave-management.vue";
import NoticeManagement from "@/views/NoticeManagement.vue";
import HighSpeedScanner from "@/views/HighSpeedScanner.vue";
import Consumable from "@/views/consumableManage/consumable.vue";
import consumNoticeManage from "@/views/consumableManage/consumNoticeManage.vue";
import UserCenter from "@/views/UserCenter.vue";
import LeaveApply from "@/views/leave/Apply.vue";
import LeaveTodo from "@/views/leave/Todo.vue";
import LeaveReport from "@/views/leave/Report.vue";
import LeaveHistory from "@/views/leave/History.vue";
import NewsManage from "@/views/news/NewsManage";
import PeningTask from "@/views/home/PeningTask.vue";
import { getToken, getUserToken, renewUserTokenExpiry } from "@/utils/auth";

Vue.use(VueRouter);

const originalPush = VueRouter.prototype.push;
const originalReplace = VueRouter.prototype.replace;

// 重写push
VueRouter.prototype.push = function push(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalPush.call(this, location, onResolve, onReject);
  }
  return originalPush.call(this, location).catch((err) => {
    // 只屏蔽重定向/导航取消错误
    if (
      err.message.includes("Redirected when going from") ||
      err.message.includes("Navigation cancelled")
    ) {
      return Promise.resolve(); // 吞掉错误，不抛出
    }
    return Promise.reject(err);
  });
};

// 重写replace
VueRouter.prototype.replace = function replace(location, onResolve, onReject) {
  if (onResolve || onReject) {
    return originalReplace.call(this, location, onResolve, onReject);
  }
  return originalReplace.call(this, location).catch((err) => {
    if (
      err.message.includes("Redirected when going from") ||
      err.message.includes("Navigation cancelled")
    ) {
      return Promise.resolve();
    }
    return Promise.reject(err);
  });
};

const routes = [
  {
    path: "/",
    redirect: "/login", // 默认重定向到登录页
  },
  {
    path: "/UserCenter",
    name: "UserCenter",
    component: UserCenter,
    meta: { requiresAuth: true }, // 需要登录才能访问
  },
  {
    path: "/login",
    name: "login",
    component: () => import("../views/LoginPage.vue"),
    meta: { requiresAuth: false }, // 登录页不需要认证
  },
  {
    path: "/register",
    name: "register",
    component: () => import("../views/RegisterPage.vue"),
    meta: { requiresAuth: false }, // 注册页不需要认证
  },
  {
    path: "/force-change-password",
    name: "force-change-password",
    component: () => import("../views/ForceChangePassword.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/home-first",
    name: "home-first",
    component: () => import("../views/home/first.vue"),
    meta: { requiresAuth: true }, // 主页需要认证
  },
  {
    path: "/user",
    name: "User",
    component: () => import("@/views/UserManagement.vue"),
    meta: { requiresAuth: true, title: "用户管理", roles: ["admin"] },
  },
  {
    path: "/consumable",
    name: "ConsumableManage",
    component: Consumable,
    meta: { requiresAuth: true },
  },
  {
    path: "/consumable/noticeManage",
    name: "consumNoticeManage",
    component: consumNoticeManage,
    meta: { requiresAuth: true },
  },
  {
    path: "/consumable/in-out-records",
    name: "InOutRecords",
    component: () => import("@/components/consumable/InOutRecords.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/consumable/scrap",
    name: "ScrapList",
    component: () => import("@/views/consumableManage/ScrapList.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/contract",
    name: "Contract",
    component: Contract,
    meta: { requiresAuth: true },
  },
  {
    path: "/leave",
    name: "Leave",
    component: Leave,
    meta: { requiresAuth: true },
    children: [
      {
        path: "apply",
        component: LeaveApply,
        name: "LeaveApply",
        meta: { requiresAuth: true },
      },
      {
        path: "todo",
        component: LeaveTodo,
        name: "LeaveTodo",
        meta: { requiresAuth: true },
      },
      {
        path: "history",
        component: LeaveHistory,
        name: "LeaveHistory",
        meta: { requiresAuth: true },
      },
      {
        path: "report",
        component: LeaveReport,
        name: "LeaveReport",
        meta: { requiresAuth: true },
      },
      {
        path: "",
        redirect: "apply",
      },
    ],
  },
  {
    path: "/vehicle",
    name: "Vehicle",
    component: () => import("@/views/VehicleManagement.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/notice",
    name: "Notice",
    component: NoticeManagement,
    meta: { requiresAuth: true },
  },
  {
    path: "/pdf-viewer/:id",
    name: "PdfViewer",
    component: () => import("@/views/PdfViewer.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/scanner",
    name: "Scanner",
    component: HighSpeedScanner,
    meta: { requiresAuth: true },
  },
  {
    path: "/supervision",
    name: "supervision",
    component: () => import("../views/supervision"),
  },
  {
    path: "/inspection",
    name: "inspection",
    redirect: "/inspection/task-monitor",
    component: () => import("@/views/inspection/DailySupervision"), // 用@别名
    meta: {
      title: "日常监督检查系统",
      requireAuth: true, // 根据实际需求配置
    },
    children: [
      {
        path: "task-monitor",
        name: "InspectionTaskMonitor",
        component: () => import("@/views/inspection/TaskMonitor"), // 子组件懒加载
        meta: {
          title: "任务监控",
        },
      },
      {
        path: "initiate-task",
        name: "InspectionInitiateTask",
        component: () => import("@/views/inspection/InitiateTask"),
        meta: {
          title: "发起任务",
        },
      },
      {
        path: "inspector-task",
        name: "InspectionInspectorTask",
        component: () => import("@/views/inspection/InspectorTask"),
        meta: {
          title: "我的任务",
        },
      },
      {
        path: "task-detail/:taskId",
        name: "InspectionTaskDetail",
        component: () => import("@/views/inspection/TaskDetail"),
        meta: {
          title: "任务详情",
        },
      },
      {
        path: "template-manager",
        name: "InspectionTemplateManager",
        component: () => import("@/views/inspection/TemplateManager"),
        meta: {
          title: "模板管理",
        },
      },
    ],
  },
  {
    path: "/ducha",
    name: "Ducha",
    component: () => import("@/views/Ducha.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/UserExamination",
    name: "UserExamination",
    component: () => import("../views/UserExamination.vue"),
    meta: { requiresAuth: true, roles: ["ROLE_ADMIN_OFFICE", "SUPER_ADMIN"] },
  },
  {
    path: "/oplog",
    name: "oplog",
    component: () => import("@/views/operlog/oplog.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/about-us",
    name: "AboutUs",
    component: () => import("@/views/foot/AboutUs.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/contract-us",
    name: "ContractUs",
    component: () => import("@/views/foot/ContractUs.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/help-center",
    name: "HelpCenter",
    component: () => import("@/views/foot/HelpCenter.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/privacy-policy",
    name: "PrivacyPolicy",
    component: () => import("@/views/foot/PrivacyPolicy.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/terms-of-use",
    name: "TermsOfUse",
    component: () => import("@/views/foot/TermsOfUse.vue"),
    meta: { requiresAuth: true },
  },
  {
    path: "/NewsManage",
    name: "NewsManage",
    component: NewsManage,
    meta: { requiresAuth: true },
  },
  {
    path: "/PeningTask",
    name: "PeningTask",
    component: PeningTask,
    meta: { requiresAuth: true },
  },
  {
    path: "/signature",
    name: "SignatureManager",
    component: () => import("@/views/signature/SignatureManager.vue"),
    meta: { requiresAuth: true },
  },
  // 通配符路由：匹配所有未定义的路由（必须放在最后！）
  {
    path: "*",
    redirect: "/", // 重定向到登录页 如果用户登录过，那就是主页
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

let isRedirecting = false;
// 全局前置守卫
router.beforeEach((to, from, next) => {
  if (getUserToken()) {
    renewUserTokenExpiry(); // 自动续期30分钟
  }
  // 如果正在重定向，直接终止当前导航
  if (isRedirecting) {
    return next(false); // 取消当前导航
  }
  console.log("导航：", from.path, "→", to.path);
  const isAuthenticated = !!getToken(); // 检查 Vuex 中是否有 token 是否已登录
  const isUserExist = !!getUserToken(); // 检查 local 中是否有用户信息
  try {
    if (to.meta.requiresAuth && (!isAuthenticated || !isUserExist)) {
      isRedirecting = true;
      // 如果目标页面需要认证且用户未登录，则跳转到登录页
      next({ name: "login", replace: true });
      // 延迟解锁，确保导航完成
      setTimeout(() => {
        isRedirecting = false;
      }, 100);
    } else if (to.name == "login" && isAuthenticated && isUserExist) {
      // 如果用户已登录但尝试访问登录页，则跳转到主页
      next({ name: "home-first" });
    } else {
      // 否则正常跳转
      next();
    }
  } catch (e) {
    console.error("导航守卫异常:", e);
    next(); // 异常时兜底放行
  } finally {
    setTimeout(() => {
      isRedirecting = false;
    }, 100);
  }
});

export default router;
