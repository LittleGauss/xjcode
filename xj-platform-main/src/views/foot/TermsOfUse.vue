<template>
  <div class="terms-page">
    <!-- 头部组件 -->
    <header-component
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="terms-main">
      <div class="terms-container">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <div class="title-bar">
            <h2 class="title-text">系统使用条款</h2>
          </div>

          <!-- 使用条款内容 -->
          <el-card class="terms-card" shadow="hover">
            <div class="terms-content">
              <!-- 引言 -->
              <section class="terms-section">
                <h3 class="section-title">系统使用说明</h3>
                <p>
                  欢迎使用新疆维吾尔自治区纤维质量监测中心内部行政管理系统（以下简称"本系统"）。本系统为内部管理平台，包含法律合同管理、请销假管理、公车管理、行政公示、低值易耗品管理等模块。请在使用前仔细阅读以下使用条款。
                </p>
              </section>

              <!-- 使用权限 -->
              <section class="terms-section">
                <h3 class="section-title">一、使用权限</h3>
                <ul>
                  <li>
                    本系统仅限于新疆维吾尔自治区纤维质量监测中心内部员工使用
                  </li>
                  <li>用户应妥善保管账号密码，不得转借他人使用</li>
                  <li>不同岗位员工根据职责分配相应的系统操作权限</li>
                  <li>离职员工账号将由系统管理员及时注销</li>
                </ul>
              </section>

              <!-- 用户责任 -->
              <section class="terms-section">
                <h3 class="section-title">三、用户责任与义务</h3>
                <ul>
                  <li>确保提交信息的真实性、准确性和完整性</li>
                  <li>按照实际业务需求使用相应功能模块</li>
                  <li>遵守各项业务流程和审批程序</li>
                  <li>及时处理待办事项，不得无故拖延</li>
                  <li>保护系统数据安全，不得泄露敏感信息</li>
                  <li>发现系统问题及时向管理员报告</li>
                </ul>
              </section>

              <!-- 数据安全 -->
              <section class="terms-section">
                <h3 class="section-title">四、数据安全与保密</h3>
                <ul>
                  <li>严禁将系统数据外传或用于非工作用途</li>
                  <li>未经授权不得访问他人业务数据</li>
                  <li>重要业务操作需保留操作日志</li>
                  <li>定期修改登录密码，确保账号安全</li>
                  <li>不得在系统内存储与工作无关的信息</li>
                  <li>遵守国家网络安全相关法律法规</li>
                </ul>
              </section>

              <!-- 禁止行为 -->
              <section class="terms-section">
                <h3 class="section-title">五、禁止行为</h3>
                <p>用户不得从事以下行为：</p>
                <ul>
                  <li>冒用他人账号或权限操作系统</li>
                  <li>恶意篡改、删除系统数据</li>
                  <li>利用系统漏洞获取不正当利益</li>
                  <li>在系统内发布不当言论或非法信息</li>
                  <li>干扰系统正常运行或进行网络攻击</li>
                  <li>未经批准擅自导出系统数据</li>
                </ul>
              </section>

              <!-- 责任限制 -->
              <section class="terms-section">
                <h3 class="section-title">六、责任限制</h3>
                <ul>
                  <li>因用户自身原因导致的密码泄露，责任由用户承担</li>
                  <li>用户操作失误造成的数据错误，需及时报告修正</li>
                  <li>系统维护期间可能暂停服务，请合理安排工作</li>
                  <li>因不可抗力导致的系统故障，本中心不承担责任</li>
                </ul>
              </section>

              <!-- 违规处理 -->
              <section class="terms-section">
                <h3 class="section-title">七、违规处理</h3>
                <p>违反本使用条款的用户，将视情节严重程度给予以下处理：</p>
                <ul>
                  <li>警告提醒</li>
                  <li>暂停系统使用权限</li>
                  <li>追究相关责任</li>
                  <li>情节严重的，依法移交有关部门处理</li>
                </ul>
              </section>

              <!-- 条款更新 -->
              <section class="terms-section">
                <h3 class="section-title">八、条款更新</h3>
                <p>
                  本中心有权根据业务需要和管理要求，适时修订本使用条款。修订后的条款将在系统中公布，用户继续使用本系统即表示接受修订后的条款。
                </p>
              </section>

              <!-- 联系我们 -->
              <section class="terms-section">
                <h3 class="section-title">九、技术支持</h3>
                <p>如遇系统使用问题，请联系技术支持。</p>
              </section>
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
  name: "TermsOfUsePage",
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
      if (routeName === "TermsOfUsePage") {
        return; // 如果已经在使用条款页面，不进行跳转
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
.terms-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.terms-main {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.terms-container {
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

/* 使用条款卡片样式 */
.terms-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.terms-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.terms-content {
  padding: 20px;
}

/* 条款部分样式 */
.terms-section {
  margin-bottom: 30px;
}

.terms-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 18px;
  color: #333;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.terms-section p {
  margin-bottom: 12px;
  line-height: 1.7;
  color: #555;
  text-align: justify;
}

.terms-section ul {
  margin: 15px 0;
  padding-left: 20px;
}

.terms-section li {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #555;
}

.terms-section strong {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .terms-main {
    padding: 16px 0;
  }

  .terms-container {
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

  .terms-content {
    padding: 15px;
  }

  .terms-section {
    margin-bottom: 25px;
  }
}
</style>
