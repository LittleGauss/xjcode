<template>
  <div class="privacy-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />

    <!-- 主要内容 -->
    <main class="privacy-main">
      <div class="privacy-container">
        <div class="content-wrapper">
          <!-- 页面标题 -->
          <div class="title-bar">
            <h2 class="title-text">隐私政策</h2>
            <p class="title-subtitle">新疆维吾尔自治区纤维质量监测中心</p>
          </div>

          <!-- 隐私政策内容 -->
          <el-card class="privacy-card" shadow="hover">
            <div class="privacy-content">
              <!-- 引言 -->
              <section class="policy-section">
                <h3 class="section-title">1. 引言</h3>
                <p>
                  新疆维吾尔自治区纤维质量监测中心（以下简称"本中心"）高度重视员工个人信息的保护。本隐私政策详细说明了我们在运营内部行政管理系统（以下简称"本系统"）过程中如何收集、使用、存储、保护和共享您的个人信息，以及您对这些信息享有的权利。
                </p>
                <p>
                  本系统包含法律合同管理、请销假管理、公车管理、行政公示、低值易耗品管理等模块，为保障系统正常运行和提供相关服务，我们需要处理您的部分个人信息。请您在使用本系统前，仔细阅读并理解本隐私政策的全部内容。
                </p>
              </section>

              <!-- 信息收集 -->
              <section class="policy-section">
                <h3 class="section-title">2. 信息的收集</h3>
                <p>
                  我们仅会出于本政策所述的以下目的，收集和使用您的个人信息：
                </p>

                <h4 class="subsection-title">2.1 账户信息</h4>
                <ul>
                  <li>
                    <strong>身份信息：</strong>姓名、工号、职位、部门、职级等
                  </li>
                  <li>
                    <strong>联系方式：</strong>工作电话、工作邮箱、办公地址等
                  </li>
                  <li><strong>账户凭证：</strong>用户名、密码（加密存储）</li>
                </ul>

                <h4 class="subsection-title">2.2 业务信息</h4>
                <ul>
                  <li>
                    <strong>请销假信息：</strong
                    >请假类型、时间、事由、审批记录等
                  </li>
                  <li>
                    <strong>公车使用信息：</strong
                    >用车事由、时间、目的地、里程等
                  </li>
                  <li><strong>物品申领信息：</strong>申领物品、数量、用途等</li>
                  <li>
                    <strong>合同管理信息：</strong
                    >与您相关的合同审批、签署记录等
                  </li>
                </ul>

                <h4 class="subsection-title">2.3 系统使用信息</h4>
                <ul>
                  <li>
                    <strong>登录信息：</strong>登录时间、IP地址、登录设备等
                  </li>
                  <li>
                    <strong>操作日志：</strong>功能使用记录、数据修改记录等
                  </li>
                  <li><strong>设备信息：</strong>浏览器类型、操作系统版本等</li>
                </ul>
              </section>

              <!-- 信息使用 -->
              <section class="policy-section">
                <h3 class="section-title">3. 信息的使用</h3>
                <p>我们仅会在以下情况下使用您的个人信息：</p>
                <ul>
                  <li>
                    <strong>系统运营：</strong>提供、维护和改进本系统的各项功能
                  </li>
                  <li><strong>身份验证：</strong>验证您的身份，确保系统安全</li>
                  <li>
                    <strong>业务流程：</strong
                    >处理请销假、公车使用、物品申领等业务流程
                  </li>
                  <li>
                 
                    <strong>安全监控：</strong>检测和防止欺诈、滥用和安全事件
                  </li>
                  <li><strong>合规要求：</strong>满足法律法规和监管要求</li>
                </ul>
                <p>
                  我们承诺不会将您的个人信息用于本政策未说明的其他目的，除非获得您的明确同意或法律法规要求。
                </p>
              </section>

              <!-- 信息共享 -->
              <section class="policy-section">
                <h3 class="section-title">4. 信息的共享与披露</h3>
                <p>
                  我们承诺对您的个人信息予以严格保密，仅在以下情况下共享您的信息：
                </p>

                <h4 class="subsection-title">4.1 内部共享</h4>
                <ul>
                  <li>
                    在中心内部，根据工作需要和权限设置，相关管理人员可以访问其管辖范围内的员工信息
                  </li>
                  <li>行政部门可以访问相关业务信息用于行政管理工作</li>
                </ul>

                <h4 class="subsection-title">4.2 外部共享</h4>
                <p>原则上，我们不会与任何外部第三方共享您的个人信息，除非：</p>
                <ul>
                  <li>获得您的明确授权</li>
                  <li>根据法律法规规定或政府主管部门的强制性要求</li>
                  <li>为维护社会公共利益或保护本中心合法权益所必需</li>
                  <li>涉及系统运维服务商，且已与其签订严格的数据保护协议</li>
                </ul>
              </section>

              <!-- 信息安全 -->
              <section class="policy-section">
                <h3 class="section-title">5. 信息安全保护</h3>
                <p>
                  我们采取业界标准的安全措施来保护您的个人信息，防止其遭到未经授权的访问、披露、使用、修改、损坏或丢失：
                </p>

                <h4 class="subsection-title">5.1 技术措施</h4>
                <ul>
                  <li>采用加密技术对数据传输进行加密</li>
                  <li>对敏感信息进行加密存储</li>
                  <li>实施严格的访问控制和权限管理</li>
                  <li>部署防火墙、入侵检测等网络安全设备</li>
                  <li>定期进行安全漏洞扫描和渗透测试</li>
                </ul>

                <h4 class="subsection-title">5.2 管理措施</h4>
                <ul>
                  <li>建立完善的信息安全管理制度</li>
                  <li>对员工进行隐私保护和信息安全培训</li>
                  <li>实施最小权限原则，限制数据访问范围</li>
                  <li>定期进行安全审计和风险评估</li>
                  <li>建立信息安全事件应急响应机制</li>
                </ul>
              </section>

              <!-- 用户权利 -->
              <section class="policy-section">
                <h3 class="section-title">6. 您的权利</h3>
                <p>您对自己的个人信息享有以下权利：</p>
                <ul>
                  <li>
                    <strong>知情权：</strong>有权了解我们如何处理您的个人信息
                  </li>
                  <li>
                    <strong>访问权：</strong>有权访问我们持有的关于您的个人信息
                  </li>
                  <li>
                    <strong>更正权：</strong
                    >有权要求更正不准确或不完整的个人信息
                  </li>
                  <li>
                    <strong>删除权：</strong
                    >在特定情况下，有权要求删除您的个人信息
                  </li>
                  <li>
                    <strong>撤回同意权：</strong>有权随时撤回您已给予的同意
                  </li>
                  <li>
                    <strong>投诉权：</strong
                    >如果您认为我们对您个人信息的处理违反了相关法律法规，有权向监管机构投诉
                  </li>
                </ul>
                <p>
                  如您希望行使上述权利，请联系系统管理员或通过本政策末尾提供的联系方式与我们联系。
                </p>
              </section>

              <!-- Cookie和追踪技术 -->
              <section class="policy-section">
                <h3 class="section-title">7. Cookie和类似技术</h3>
                <p>
                  为使您获得更轻松便捷的访问体验，我们会在您的计算机或移动设备上存储Cookie、Flash
                  Cookie，或浏览器（或关联应用程序）提供的其他通常包含标识符、站点名称以及一些号码和字符的本地存储（统称"Cookie"）。
                </p>
                <p>我们使用Cookie用于以下目的：</p>
                <ul>
                  <li>
                    <strong>身份验证：</strong>记住您的登录状态，避免重复登录
                  </li>
                  <li><strong>安全性：</strong>支持安全功能，检测恶意活动</li>
                  <li><strong>偏好设置：</strong>记住您的系统设置和偏好</li>
                  <li><strong>性能分析：</strong>分析系统性能，优化用户体验</li>
                </ul>
                <p>
                  您可以通过浏览器设置拒绝或管理Cookie，但请注意，如果停用Cookie，您可能无法享受最佳的服务体验，某些系统功能也可能无法正常使用。
                </p>
              </section>

              <!-- 政策更新 -->
              <section class="policy-section">
                <h3 class="section-title">8. 政策更新</h3>
                <p>
                  我们可能会不时更新本隐私政策。当政策发生变更时，我们会通过系统公告或其他适当方式通知您。对于重大变更，我们还会提供更为显著的通知。
                </p>
                <p>本政策所指的重大变更包括但不限于：</p>
                <ul>
                  <li>我们的服务模式发生重大变化</li>
                  <li>个人信息共享、转让或公开披露的主要对象发生变化</li>
                  <li>您参与个人信息处理方面的权利及其行使方式发生重大变化</li>
                  <li>我们的联系方式及投诉渠道发生变化</li>
                  <li>个人信息安全影响评估报告表明存在高风险时</li>
                </ul>
                <p>建议您定期查阅本政策，了解我们如何保护您的信息。</p>
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
  name: "PrivacyPolicyPage",
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
      if (routeName === "PrivacyPolicyPage") {
        return; // 如果已经在隐私政策页面，不进行跳转
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
.privacy-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.privacy-main {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.privacy-container {
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
  text-align: center;
  margin-bottom: 24px;
}

.title-text {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.title-subtitle {
  font-size: 14px;
  color: #666;
}

/* 隐私政策卡片样式 */
.privacy-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
}

.privacy-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.privacy-content {
  padding: 30px;
}

/* 政策部分样式 */
.policy-section {
  margin-bottom: 35px;
}

.policy-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 18px;
  color: #2c3e50;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #eaeaea;
  font-weight: 600;
}

.subsection-title {
  font-size: 16px;
  color: #34495e;
  margin: 20px 0 12px 0;
  font-weight: 500;
}

.policy-section p {
  margin-bottom: 15px;
  line-height: 1.7;
  color: #555;
  text-align: justify;
}

.policy-section ul {
  margin: 15px 0;
  padding-left: 24px;
}

.policy-section li {
  margin-bottom: 10px;
  line-height: 1.6;
  color: #555;
  position: relative;
}

.policy-section li:before {
  content: "•";
  color: #3498db;
  font-weight: bold;
  display: inline-block;
  width: 1em;
  margin-left: -1em;
}

.policy-section strong {
  color: #333;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .privacy-main {
    padding: 16px 0;
  }

  .privacy-container {
    padding: 0 12px;
  }

  .content-wrapper {
    padding: 16px;
  }

  .privacy-content {
    padding: 20px;
  }

  .title-text {
    font-size: 20px;
  }

  .section-title {
    font-size: 17px;
  }

  .subsection-title {
    font-size: 15px;
  }

  .policy-section {
    margin-bottom: 25px;
  }
}

@media (max-width: 480px) {
  .privacy-content {
    padding: 15px;
  }
}
</style>
