<template>
  <div class="home-container">
    <!-- 头部标题 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div id="app">
      <el-container style="height: 100vh">
        <el-header
          style="text-align: center; font-size: 20px; line-height: 60px"
        >
          日常监督检查系统
        </el-header>
        <el-container>
          <el-aside width="200px" style="background-color: #eef1f6">
            <el-menu
              :default-active="currentMenuIndex"
              class="el-menu-vertical-demo"
            >
              <el-menu-item
                index="1"
                @click="
                  $router.push('/inspection/task-monitor').catch((err) => {
                    if (err.name != 'NavigationDuplicated') throw err;
                  })
                "
              >
                <i class="el-icon-menu"></i>
                <span slot="title">任务监控</span>
              </el-menu-item>
              <el-menu-item
                index="2"
                @click="
                  $router.push('/inspection/initiate-task').catch((err) => {
                    if (err.name != 'NavigationDuplicated') throw err;
                  })
                "
              >
                <i class="el-icon-plus"></i>
                <span slot="title">发起任务</span>
              </el-menu-item>
              <el-menu-item index="3" @click="navigateToInspectorTask">
                <i class="el-icon-user"></i>
                <span slot="title">我的任务</span>
              </el-menu-item>
              <!-- 新增模板管理菜单 -->
              <el-menu-item index="4" @click="navigateToTemplateManager">
                <i class="el-icon-s-tools"></i>
                <span slot="title">模板管理</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <el-main>
            <router-view
              :login_user="login_user"
              :userRoles="userRoles"
              :userPermissions="userPermissions"
            />
          </el-main>
        </el-container>
      </el-container>
    </div>
    <!-- 底部区域 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import HeaderComponent from "@/components/HeaderComponent";
import FooterComponent from "@/components/FooterComponent";
export default {
  name: "DailySupervision",
  components: { FooterComponent, HeaderComponent },
  data() {
    return {
      login_user: null,
      userRoles: [],
      userPermissions: [],
      currentMenuIndex: "1",
    };
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles || [];
      this.userPermissions = storedUserInfo.permissions || [];
      this.setCurrentMenuIndex();
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
  //监听路由变化，实时更新选中的菜单
  watch: {
    $route: {
      immediate: true, // 立即执行一次（和created里的初始化可二选一，保险起见都加）
      handler() {
        this.setCurrentMenuIndex();
      },
    },
  },
  methods: {
    // 核心方法：根据当前路由路径匹配菜单index
    setCurrentMenuIndex() {
      // 建立「路由路径 → 菜单index」的映射表
      const routeToIndexMap = {
        "/inspection/task-monitor": "1", // 任务监控
        "/inspection/initiate-task": "2", // 发起任务
        "/inspection/inspector-task": "3", // 我的任务
        "/inspection/template-manager": "4", // 模板管理
      };
      // 获取当前路由的路径，匹配对应的index（默认选中"1"）
      this.currentMenuIndex = routeToIndexMap[this.$route.path] || "1";
    },
    handleNavigate(routeName) {
      this.$router.push({ name: routeName }).catch((err) => {
        // 忽略冗余导航错误
        if (err.name != "NavigationDuplicated") {
          throw err;
        }
      });
    },
    navigateToTemplateManager() {
      this.$router.push("/inspection/template-manager").catch((err) => {
        // 忽略冗余导航错误
        if (err.name != "NavigationDuplicated") {
          throw err;
        }
      });
    },
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    navigateToInspectorTask() {
      this.$router.push("/inspection/inspector-task").catch((err) => {
        // 忽略冗余导航错误
        if (err.name != "NavigationDuplicated") {
          throw err;
        }
      });
    },
  },
};
</script>

<style lang="scss" scoped>
$headHeight: 148px;
$bottomHeight: 161px;

.home-container {
  width: 100%;
  height: 100vh;
  background: #e5e5e5;
  .head {
    height: $headHeight;
    background-image: url("@/assets/home/top.png");
    &-operate {
      display: flex;
      justify-content: flex-end;
      padding: 65px;
      right: 10%;
      position: relative;
    }
  }
  /* 自定义滚动条样式 */
  ::-webkit-scrollbar {
    width: 8px;
    height: 8px;
  }
  ::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
  }
  ::-webkit-scrollbar-thumb {
    background: #888;
    border-radius: 10px;
  }
  ::-webkit-scrollbar-thumb:hover {
    background: #555;
  }
  .bottom {
    height: $bottomHeight;
    background: #397dcd;
    width: 100%;
    display: flex;
    &-left {
      width: 30%;
    }
    &-middle {
      width: 50%;
      padding-top: 70px;
      p {
        text-align: center;
        color: #fff;
      }
    }
    &-right {
      width: 30%;
    }
  }
}

#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
}

.el-header {
  background-color: #fff;
  color: #333;
  border-bottom: 1px solid #e6e6e6;
}

.el-aside {
  color: #333;
}

/* 像素风格卡片样式（原CSS保留） */
.pixel-card {
  border: 2px solid #333;
  box-shadow: 4px 4px 0 #000;
  transition: all 0.2s ease;
}
.pixel-card:hover {
  transform: translate(-2px, -2px);
  box-shadow: 6px 6px 0 #000;
}

/* 像素风格按钮样式（原CSS保留） */
.pixel-btn {
  border: 2px solid #333;
  box-shadow: 3px 3px 0 #000;
  transition: all 0.2s ease;
  position: relative; /* 添加相对定位 */
  overflow: hidden; /* 防止背景色溢出 */
}
.pixel-btn:hover {
  transform: translate(-1px, -1px);
  box-shadow: 4px 4px 0 #000;
}
.pixel-btn:active {
  transform: translate(1px, 1px);
  box-shadow: 2px 2px 0 #000;
}
/* 修复被选中菜单项的样式 */
.pixel-btn.bg-blue-500 {
  overflow: hidden;
  z-index: 1;
}
</style>
