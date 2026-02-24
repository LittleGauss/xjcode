<template>
  <div class="header-wrapper">
    <header class="header">
      <div class="header-content">
        <div class="logo-section">
          <img
            src="@/assets/company-logo.png"
            alt="中心Logo"
            class="company-logo"
          />
          <a class="company-name" href="#" @click.prevent="handleHomeClick"
            >新疆维吾尔自治区纤维质量监测中心</a
          >
        </div>
        <nav class="nav-menu">
          <a
            href="#"
            @click.prevent="handleMenuClick('LEAVE:APPLY', 'Leave')"
            :class="[
              'nav-item',
              { active: isRouteActive('Leave') },
              { disabled: !$hasPermission('LEAVE:APPLY') },
            ]"
            >请销假管理</a
          >
          <a
            href="#"
            :class="[
              'nav-item',
              { active: isRouteActive('ConsumableManage') },
              { disabled: !$hasPermission('SUPPLY:APPLY') },
            ]"
            @click.prevent="handleMenuClick('SUPPLY:APPLY', 'ConsumableManage')"
            >低值易耗品管理</a
          >
          <a
            href="#"
            @click.prevent="handleMenuClick('NOTICE:VIEW', 'Notice')"
            :class="[
              'nav-item',
              { active: isRouteActive('Notice') },
              { disabled: !$hasPermission('NOTICE:VIEW') },
            ]"
            >行政公示</a
          >
          <a
            href="#"
            @click.prevent="handleMenuClick('VEHICLE:MANAGE', 'Vehicle')"
            :class="[
              'nav-item',
              { active: isRouteActive('Vehicle') },
              { disabled: !$hasPermission('VEHICLE:MANAGE') },
            ]"
            >公车管理</a
          >
          <a
            href="#"
            :class="[
              'nav-item',
              { active: isRouteActive('inspection') },
              {
                disabled: !$hasPermission([
                  'NOTICE:SUPERVISE',
                  'SUPERVISION:INSPECT',
                  'SUPERVISION:MANAGE',
                ]),
              },
            ]"
            @click.prevent="
              handleMenuClick(
                [
                  'NOTICE:SUPERVISE',
                  'SUPERVISION:INSPECT',
                  'SUPERVISION:MANAGE',
                ],
                'inspection'
              )
            "
            >日常监督检查</a
          >
          <a
            href="#"
            :class="[
              'nav-item',
              { active: isRouteActive('Contract') },
              {
                disabled: !$hasPermission([
                  'CONTRACT:UPLOAD',
                  'CONTRACT:VIEW_DEPT',
                  'CONTRACT:VIEW_ALL',
                ]),
              },
            ]"
            @click.prevent="handleMenuClick('CONTRACT:VIEW_DEPT', 'Contract')"
            >法律合同审核</a
          >
        </nav>
        <div class="user-info">
          <div class="user-details" v-if="login_user">
            <a
              href="#"
              @click.prevent="goToUserCenter"
              :class="['nav-item', { active: isRouteActive('UserCenter') }]"
            >
              <span class="user-name">{{ login_user.nickname }}</span>
            </a>
            <span class="role">
              ({{
                userRoles && userRoles.length > 0
                  ? userRoles[0].name
                  : "No Role"
              }})
              <el-button type="primary" size="small" @click="logout"
                >退出</el-button
              >
            </span>
          </div>
          <span class="welcome-message">
            欢迎使用，您是本站第<strong
              style="color: #f5e319d4; margin: 0 4px"
              >{{ userIndex }}</strong
            >位用户
          </span>
        </div>
      </div>
    </header>
    <div class="warning-bar">本平台为非涉密平台，禁止处理、传输国家秘密</div>
  </div>
</template>

<script>
import { getUserIndex } from "@/api/operlog";
export default {
  name: "HeaderComponent",
  props: {
    login_user: {
      type: Object,
      default: null,
    },
    userRoles: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      userIndex: 123, // 在组件内部维护状态
    };
  },
  computed: {
    // 获取当前活跃的路由名称
    currentRouteName() {
      // 确保组件可以访问到 Vue Router 实例
      return this.$route ? this.$route.name : null;
    },
  },
  methods: {
    loadUserIndex() {
      getUserIndex()
        .then((response) => {
          // 只有在这里，response 才是最终的数据对象
          // 后端返回的数据结构是：{ code: 200, data: 123, msg: "" }
          if (response && response.code == 200) {
            this.userIndex = response.data + 1;
            console.log("获取用户序号:", this.userIndex);
          } else {
            console.error(
              "获取用户序号失败:",
              response ? response.msg : "请求失败"
            );
          }
        })
        .catch((error) => {
          console.error("API 请求出错:", error);
          this.userIndex = "未知";
        });
    },
    handleMenuClick(perMis, path) {
      // 判断是否有权限
      const hasPermission = this.$hasPermission(perMis);
      if (!hasPermission) {
        this.handleNoPermission();
        return; // 无权限时直接返回，不执行跳转
      }
      // 2. 判断是否为当前路由，避免重复导航
      if (this.currentRouteName == path) {
        return;
      }
      this.$emit("navigate", path);
    },
    handleNoPermission() {
      this.$message({
        message: "您暂无权限访问该功能，请联系管理员",
        type: "warning",
        duration: 1500,
      });
    },
    logout() {
      this.$emit("logout");
    },
    isRouteActive(routeName) {
      // 检查当前路由名称是否等于传入的路由名称
      return this.currentRouteName == routeName;
    },
    goToleavemanagement() {
      this.$emit("navigate", "Leave");
    },
    goToConsumable() {
      this.$emit("navigate", "ConsumableManage");
    },
    goToNotice() {
      this.$emit("navigate", "Notice");
    },
    goToVehicle() {
      this.$emit("navigate", "Vehicle");
    },
    goTosupervision() {
      this.$emit("navigate", "supervision");
    },
    goToContract() {
      this.$emit("navigate", "Contract");
    },
    handleHomeClick() {
      this.$emit("navigate", "home-first");
    },
    goToUserCenter() {
      this.$emit("navigate", "UserCenter");
    },
  },
  watch: {
    // 监听 login_user 的变化，确保登录后加载数据
    login_user: {
      immediate: true, // 组件初始化时也执行一次
      handler(newVal) {
        if (newVal) {
          this.loadUserIndex();
          this.$forceUpdate();
        }
      },
    },
    // 添加对路由变化的监听，确保权限变化时能刷新界面
    $route: {
      handler() {
        this.$nextTick(() => {
          this.$forceUpdate();
        });
      },
    },
  },
};
</script>

<style scoped>
/* 新增外层容器 */
.header-wrapper {
  position: sticky;
  top: 0;
  z-index: 100;
}
/* 警示条样式 - 占满整个页面宽度 */
.warning-bar {
  color: red; /* 红色文字 */
  font-weight: bold; /* 加粗 */
  text-align: right; /* 文字靠右 */
  padding: 6px 20px; /* 调整内边距 */
  font-size: 16px;
  width: 100%;
  background-color: transparent; /* 背景透明 */
  box-shadow: none; /* 去掉阴影 */
  position: relative;
  z-index: 101;
  margin-top: 0; /* 确保紧贴 header 下方 */
}

/* 基础样式：保证背景和定位 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 15px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  min-height: 85px;
  height: auto;
  overflow: hidden; /* 防止内容溢出 */
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%; /* 占满页面宽度 */
  padding: 10px 0;
  gap: 10px;
}

/* Logo区域：缩小内部间距，放大字体 */
.logo-section {
  display: flex;
  align-items: center;
  gap: 8px; /* 缩小Logo与文字间距（原8px） */
  flex-shrink: 0;
  min-width: 260px;
}

.company-logo {
  width: 36px;
  height: 36px;
  border-radius: 3px;
}

.company-name {
  font-size: 18px; /* 放大字体（原15px） */
  font-weight: bold;
  white-space: nowrap;
}

/* 导航区域：缩小导航项间距，放大字体 */
.nav-menu {
  display: flex;
  justify-content: space-around;
  align-items: center;
  flex: 1;
  gap: 6px; /* 缩小导航项间距（原8px） */
  padding: 0 10px;
  flex-wrap: nowrap;
}

.nav-item {
  color: white;
  text-decoration: none;
  padding: 6px 9px;
  border-radius: 4px;
  display: flex; /* 配合align-items实现垂直居中 */
  transition: background-color 0.3s;
  white-space: nowrap;
  max-width: fit-content; /* 确保背景不超文字范围，仅按padding延伸少量 */
  justify-content: center; /* 文字水平居中，背景与文字对齐更美观 */
  font-size: 14px; /* 放大字体（原13px） */
  flex: 1; /* 每个导航项自动平分导航栏宽度 */
  text-align: center; /* 文字居中，避免错位 */
  min-height: 32px; /* 固定最小高度，让选中背景更规整 */
  max-width: 120px; /* 每个导航项最多宽120px，足够容纳所有文字+padding */
}

.nav-item:hover,
.nav-item.active {
  background-color: rgba(255, 255, 255, 0.2);
}
/* 禁用状态（无权限） */
.nav-item.disabled {
  color: #ccc;
  cursor: not-allowed;
  opacity: 0.6; /* 透明度降低，视觉上区分 */
}
/* 禁用状态hover无效果 */
.nav-item.disabled:hover {
  background: transparent;
}

/* 用户信息：缩小间距，放大字体，确保完整显示 */
.user-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px; /* 缩小用户名与欢迎语间距（原2px） */
  flex-shrink: 0;
  min-width: 200px; /* 适配放大后的字体（原180px） */
  font-size: 13px; /* 放大字体（原12px） */
  padding-right: 20px;
}

.user-details {
  display: flex;
  align-items: center;
  gap: 5px;
}

.welcome-message {
  color: #f5e319d4;
  font-size: 14px;
  margin: 3px;
}

/* 小屏幕适配 */
@media (max-width: 1200px) {
  .header-content {
    flex-wrap: wrap;
  }
  .nav-menu {
    width: 100%;
    justify-content: center;
    gap: 5px;
    margin: 5px 0;
    flex: none;
  }
}
</style>
