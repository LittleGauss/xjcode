<template>
  <div class="help-center-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="help-main">
      <div class="help-container">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <div class="title-bar">
            <h2 class="title-text">帮助中心</h2>
          </div>

          <!-- 帮助中心内容 -->
          <el-card class="help-card" shadow="hover">
            <div class="help-content">
              <!-- 常见问题部分 -->
              <div class="faq-section">
                <h3 class="section-title">常见问题</h3>

                <el-collapse v-model="activeNames" class="faq-list">
                  <el-collapse-item title="如何修改密码？" name="1">
                    <div class="faq-answer">
                      <p>
                        登录系统后，点击顶部导航栏右上角的用户名即可修改个人密码。
                      </p>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item title="忘记密码怎么办？" name="2">
                    <div class="faq-answer">
                      <p>
                        如果您忘记了密码，请联系系统管理员重置密码。重置后，请及时登录系统修改为您自己的密码。
                      </p>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item title="如何查看个人资料？" name="3">
                    <div class="faq-answer">
                      <p>
                        登录系统后，点击顶部导航栏右上角的用户名即可查看您的个人资料信息。
                      </p>
                    </div>
                  </el-collapse-item>

                  <el-collapse-item title="如何获取权限？" name="4">
                    <div class="faq-answer">
                      <p>
                        如果您遇到权限不足问题，可以联系管理员，管理员会根据您的身份信息和工作需求为您分配相应的权限
                      </p>
                    </div>
                  </el-collapse-item>
                </el-collapse>
              </div>
            </div>
          </el-card>
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
  name: "HelpCenterPage",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
      activeNames: ["1"], // 默认展开第一个问题
    };
  },
  methods: {
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "HelpCenterPage") {
        return; // 如果已经在帮助中心页面，不进行跳转
      }
      this.$router.push({ name: routeName });
    },

    handleLogout() {
      // 处理退出登录
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
.help-center-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.help-main {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.help-container {
  padding: 0 16px;
}

.content-wrapper {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}

/* 标题栏样式 */
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.title-text {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 帮助中心卡片样式 */
.help-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.help-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.help-content {
  padding: 20px;
}

/* 部分标题样式 */
.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

/* 常见问题列表样式 */
.faq-section {
  margin-bottom: 30px;
}

.faq-list {
  border: none;
}

.faq-list >>> .el-collapse-item__header {
  font-size: 15px;
  font-weight: 500;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.faq-list >>> .el-collapse-item__wrap {
  border-bottom: none;
}

.faq-list >>> .el-collapse-item__content {
  padding: 15px 0;
  color: #555;
  line-height: 1.6;
}

.faq-answer p {
  margin-bottom: 10px;
}

.faq-answer ul {
  margin: 10px 0;
  padding-left: 20px;
}

.faq-answer li {
  margin-bottom: 5px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .help-main {
    padding: 16px 0;
  }

  .help-container {
    padding: 0 12px;
  }

  .content-wrapper {
    padding: 16px;
  }

  .title-text {
    font-size: 18px;
  }

  .section-title {
    font-size: 16px;
  }

  .contact-item {
    flex-direction: column;
  }

  .contact-label {
    min-width: auto;
    margin-bottom: 5px;
  }
}
</style>
