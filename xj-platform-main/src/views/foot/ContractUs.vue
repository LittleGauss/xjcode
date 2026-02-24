<template>
  <div class="contact-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="contact-main">
      <div class="contact-container">
        <h1 class="page-title">联系我们</h1>

        <div class="contact-info">
          <div class="info-row">
            <span class="info-label">全称：</span>
            <span class="info-content">新疆维吾尔自治区纤维质量监测中心</span>
          </div>

          <div class="info-row">
            <span class="info-label">隶属：</span>
            <span class="info-content">新疆维吾尔自治区市场监督管理局</span>
          </div>

          <div class="info-row">
            <span class="info-label">地址：</span>
            <span class="info-content"
              >乌鲁木齐市新市区河北东路188号新疆质量技术监督检验检测研究基地</span
            >
          </div>

          <div class="info-row">
            <span class="info-label">邮编：</span>
            <span class="info-content">830026</span>
          </div>

          <div class="info-row">
            <span class="info-label">联系电话：</span>
            <span class="info-content"
              >0991-3191710、3191712、3191715（传真）</span
            >
          </div>

          <div class="info-row">
            <span class="info-label">监督举报电话：</span>
            <span class="info-content">0991-3191709</span>
          </div>
        </div>
      </div>
    </main>

    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "ContactPage",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
    };
  },
  methods: {
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "ContactPage") {
        return;
      }
      this.$router.push({ name: routeName });
    },

    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
  },
  created() {
    // 从会话存储获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
};
</script>

<style scoped>
.contact-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.contact-main {
  flex: 1;
  padding: 40px 0;
}

.contact-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
  text-align: center;
  margin-bottom: 30px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.contact-info {
  background: white;
  padding: 30px;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.info-row {
  display: flex;
  margin-bottom: 15px;
  line-height: 1.6;
}

.info-label {
  width: 120px;
  font-weight: bold;
  color: #333;
  flex-shrink: 0;
}

.info-content {
  color: #555;
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .contact-main {
    padding: 20px 0;
  }

  .contact-container {
    padding: 0 15px;
  }

  .page-title {
    font-size: 20px;
  }

  .contact-info {
    padding: 20px;
  }

  .info-row {
    flex-direction: column;
    margin-bottom: 20px;
  }

  .info-label {
    width: 100%;
    margin-bottom: 5px;
  }
}
</style>
