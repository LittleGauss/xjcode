<template>
  <div class="about-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="about-main">
      <div class="about-container">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <div class="title-bar">
            <h2 class="title-text">关于我们</h2>
          </div>

          <!-- 关于我们内容卡片 -->
          <el-card class="about-card" shadow="hover">
            <div class="about-content">
              <h3 class="organization-name">
                新疆维吾尔自治区纤维质量监测中心
              </h3>

              <div class="about-text">
                <p>
                  "新疆维吾尔自治区纤维质量监测中心"前身为"新疆维吾尔自治区纤维检验局"，属参公管理单位。随着机构改革的深入，根据《关于自治区市场监督管理局所属事业单位机构编制调整的通知》新党编【2019】37号文件精神，"自治区纤维质量检验局"于2019年8月变更为"自治区纤维质量监测中心"，属公益一类事业单位，经费实行全额预算管理，机构规格为县（处）级。
                </p>

                <p>
                  自治区纤维质量监测中心主要职责：承担全区纤维及纤维制品的检验检测、纤维公证检验、监督抽检、监测等工作;承担纤维、纺织品检验检测、技术、标准、设备研究开发等工作。
                </p>
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
  name: "AboutPage",
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
      if (routeName === "AboutPage") {
        return; // 如果已经在关于我们页面，不进行跳转
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
.about-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.about-main {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.about-container {
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

/* 关于我们卡片样式 */
.about-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.about-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.about-content {
  padding: 20px;
}

.organization-name {
  font-size: 18px;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.about-text p {
  margin-bottom: 16px;
  line-height: 1.8;
  color: #555;
  text-align: justify;
  text-indent: 2em;
}

.about-text p:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .about-main {
    padding: 16px 0;
  }

  .about-container {
    padding: 0 12px;
  }

  .content-wrapper {
    padding: 16px;
  }

  .title-text {
    font-size: 18px;
  }

  .organization-name {
    font-size: 16px;
  }

  .about-text p {
    text-indent: 1.5em;
    font-size: 14px;
  }
}
</style>
