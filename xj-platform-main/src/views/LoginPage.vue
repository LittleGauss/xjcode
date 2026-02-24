<!-- src/views/LoginPage.vue -->
<template>
  <div class="login-page">
    <!-- 页头 -->
    <header class="header">
      <div class="header-content">
        <div class="logo-section">
          <img
            src="@/assets/company-logo.png"
            alt="中心Logo"
            class="company-logo"
          />
          <span class="company-name">新疆维吾尔自治区纤维质量监测中心</span>
        </div>
      </div>
    </header>

    <!-- 登录表单区域 -->
    <main class="main-content">
      <div class="login-container">
        <div class="login-card">
          <h2 class="login-title">用户登录</h2>
          <el-form
            :model="loginForm"
            :rules="loginRules"
            ref="loginForm"
            class="login-form"
            @submit.native.prevent="handleLogin"
            @keyup.enter.native="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
              ></el-input>
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="captcha" v-if="showCaptcha">
              <div class="captcha-container">
                <el-input
                  v-model="loginForm.captcha"
                  placeholder="请输入验证码"
                  prefix-icon="el-icon-key"
                  size="large"
                  class="captcha-input"
                ></el-input>
                <img
                  :src="captchaImage"
                  alt="验证码"
                  class="captcha-image"
                  @click="getCaptcha"
                />
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleLogin"
                class="login-button"
                block
              >
                {{ loading ? "登录中..." : "登录" }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <router-link to="/register" class="register-link">
              还没有账号？立即注册
            </router-link>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <a href="#" class="footer-link">关于我们</a>
          <a href="#" class="footer-link">联系我们</a>
          <a href="#" class="footer-link">帮助中心</a>
          <a href="#" class="footer-link">隐私政策</a>
          <a href="#" class="footer-link">使用条款</a>
        </div>
        <div class="copyright">
          <p>
            新疆维吾尔自治区纤维质量监测中心（地址：乌鲁木齐市新市区河北路188号）
          </p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { login, getCaptchaImage } from "@/api/login";
import { setToken, setUserToken } from "@/utils/auth";
import { encryptPassword } from "@/utils/encrypt"; // 导入加密工具
import { validatePassword } from "@/utils/validate";

export default {
  name: "LoginPage",
  data() {
    return {
      loginForm: {
        username: "",
        password: "",
        captcha: "",
      },
      loginRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        password: [
          { required: true, message: "请输入密码", trigger: "blur" },
          { min: 3, message: "密码长度不能少于3位", trigger: "blur" },
        ],
        captcha: [{ required: true, message: "请输入验证码", trigger: "blur" }],
      },
      loading: false,
      showCaptcha: true, // 默认显示验证码
      captchaKey: "",
      captchaImage: "",
    };
  },
  mounted() {
    this.getCaptcha();
  },
  methods: {
    // 获取验证码
    async getCaptcha() {
      try {
        const res = await getCaptchaImage();
        if (res.code == 200) {
          this.captchaKey = res.data.captchaKey;
          this.captchaImage = res.data.captchaImage;
          this.showCaptcha = true;
        }
      } catch (error) {
        console.error("获取验证码失败:", error);
        this.showCaptcha = false;
      }
    },

    // 处理登录
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          try {
            this.loading = true;
            // 1. 处理密码（自动判断是否加密）
            const { password: encryptedPwd, isEncrypted } = encryptPassword(
              this.loginForm.password
            );

            const loginData = {
              username: this.loginForm.username.trim(),
              password: encryptedPwd,
              isEncrypted: isEncrypted, // 告诉后端是否加密
            };

            // 验证码参数作为查询参数传递
            let params = {};
            if (this.showCaptcha && this.loginForm.captcha && this.captchaKey) {
              params = {
                captchaKey: this.captchaKey,
                captchaCode: this.loginForm.captcha,
              };
            }

            // 发起登录请求
            const res = await login(loginData, params);

            if (res.code == 200) {
              // 登录成功: 兼容后端返回 tokenHead + token 或直接返回完整 token
              const data = res.data || {};
              const tokenStr = data.token || "";
              // 存储裸 token 到 Cookie/localStorage
              setToken(tokenStr);
              console.log("保存的token:", tokenStr);
              // 存储用户信息
              setUserToken(res.data.userInfo);

              this.$message({
                message: "登录成功",
                type: "success",
                duration: 1000, // 设置为1秒后自动关闭
              });

              // 弱密码提示修改：使用与注册页一致的规则
              const isWeakPassword = !validatePassword(this.loginForm.password);
              if (isWeakPassword) {
                this.$message.warning("当前密码强度较弱，请先修改密码");
                this.$router.replace("/force-change-password");
              } else {
                // 跳转到主页
                this.$router.push("/home-first");
              }
            } else {
              // 登录失败，刷新验证码
              this.getCaptcha();
            }
          } catch (error) {
            console.error("登录异常:", error);
            this.$message.error("登录过程中发生错误，请稍后重试");
            this.getCaptcha();
          } finally {
            this.loading = false;
          }
        }
      });
    },
  },
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

/* 页头样式 */
.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 12px;
}

.company-logo {
  width: 40px;
  height: 40px;
  border-radius: 6px;
}

.company-name {
  font-size: 20px;
  font-weight: bold;
}

/* 主要内容样式 */
.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.login-container {
  width: 100%;
  max-width: 450px;
}

.login-card {
  background: white;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
}

.login-title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  font-weight: bold;
}

.login-form {
  margin-bottom: 20px;
}

.captcha-container {
  display: flex;
  gap: 10px;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 120px;
  height: 40px;
  cursor: pointer;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  object-fit: cover;
}

.login-button {
  margin-top: 10px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}

.register-link:hover {
  text-decoration: underline;
}

/* 页脚样式 */
.footer {
  background: #2c3e50;
  color: white;
  padding: 30px 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.footer-link {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-link:hover {
  color: white;
}

.copyright p {
  margin: 5px 0;
  color: #95a5a6;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
  }

  .login-card {
    padding: 30px 20px;
    margin: 0 15px;
  }

  .captcha-container {
    flex-direction: column;
    align-items: stretch;
  }

  .captcha-image {
    margin-top: 10px;
    width: 100%;
  }

  .footer-links {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
