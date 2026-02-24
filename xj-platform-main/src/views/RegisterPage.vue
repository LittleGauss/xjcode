<template>
  <div class="register-page">
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
    <!-- 注册表单区域 -->
    <main class="main-content">
      <div class="register-container">
        <div class="register-card">
          <h2 class="register-title">用户注册</h2>
          <el-form
            :model="registerForm"
            :rules="registerRules"
            ref="registerForm"
            class="register-form"
            @submit.native.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                prefix-icon="el-icon-user"
                size="large"
              ></el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码（密码至少8位，需包含字母、数字和字符）"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请确认密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱"
                prefix-icon="el-icon-user"
                size="large"
              ></el-input>
            </el-form-item>
            <el-form-item prop="phoneNumber">
              <el-input
                v-model="registerForm.phoneNumber"
                placeholder="请输入手机号"
                prefix-icon="el-icon-user"
                size="large"
              ></el-input>
            </el-form-item>
            <!-- 部门选择（修复prop绑定） -->
            <el-form-item prop="department">
              <el-select
                v-model="registerForm.department"
                placeholder="请选择部门"
                size="large"
                style="width: 100%"
              >
                <el-option
                  v-for="dept in departments"
                  :key="dept.value"
                  :label="dept.label"
                  :value="dept.value"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleRegister"
                class="register-button"
                block
              >
                {{ loading ? "注册中..." : "注册" }}
              </el-button>
            </el-form-item>
          </el-form>
          <div class="register-footer">
            <router-link to="/login" class="login-link">
              已有账号？立即登录
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
import { register } from "@/api/login";
import { validatePassword } from "@/utils/validate";
import { userApi } from "@/api/usermag";
import { encryptPassword } from "@/utils/encrypt";

export default {
  name: "RegisterPage",
  data() {
    const validatePass = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请输入密码"));
      } else if (!validatePassword(value)) {
        callback(new Error("密码至少8位，必须包含数字大小写字母和字符"));
      } else {
        if (this.registerForm.confirmPassword !== "") {
          this.$refs.registerForm.validateField("confirmPassword");
        }
        callback();
      }
    };
    const validatePass2 = (rule, value, callback) => {
      if (value === "") {
        callback(new Error("请再次输入密码"));
      } else if (value !== this.registerForm.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };
    return {
      registerForm: {
        username: "",
        password: "",
        confirmPassword: "",
        email: "",
        phoneNumber: "",
        department: "", // 存储部门ID（dept.id）
      },
      departments: [], // 部门列表（从接口获取）
      registerRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          {
            min: 3,
            max: 20,
            message: "长度在 3 到 20 个字符",
            trigger: "blur",
          },
        ],
        password: [
          { required: true, validator: validatePass, trigger: "blur" },
        ],
        confirmPassword: [
          { required: true, validator: validatePass2, trigger: "blur" },
        ],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          {
            type: "email",
            message: "请输入正确的邮箱格式",
            trigger: ["blur", "change"],
          },
        ],
        phoneNumber: [
          { required: true, message: "请输入手机号", trigger: "blur" },
          {
            pattern: /^1[3-9]\d{9}$/,
            message: "请输入正确的手机号格式",
            trigger: ["blur", "change"],
          },
        ],
        department: [
          { required: true, message: "请选择部门", trigger: "change" },
        ], // 新增部门校验
      },
      loading: false,
    };
  },
  created() {
    this.loadDepartments();
  },
  methods: {
    async loadDepartments() {
      try {
        const res = await userApi.getDepartmentList();
        this.departments = (res.data || []).map((dept) => ({
          label: dept.name,
          value: dept.id, // value绑定部门ID
        }));
      } catch (error) {
        console.error("获取部门列表失败:", error);
        this.$message.error("获取部门列表失败，请刷新页面重试");
      }
    },
    // 处理注册（传递部门ID参数）
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.loading = true;
          try {
            const { password: encryptedPwd, isEncrypted } = encryptPassword(
              this.registerForm.password
            );
            const res = await register({
              username: this.registerForm.username.trim(),
              password: encryptedPwd,
              isEncrypted: isEncrypted,
              email: this.registerForm.email.trim(),
              phoneNumber: this.registerForm.phoneNumber.trim(),
              departmentId: this.registerForm.department, // 新增：传递部门ID
            });
            if (res.code == 200) {
              this.$message.success("注册成功");
              this.$router.push("/login");
            } else {
              this.$message.error(res.message || "注册失败");
            }
          } catch (error) {
            console.error("注册异常:", error);
            this.$message.error("注册过程中发生错误，请稍后重试");
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
.register-page {
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
.register-container {
  width: 100%;
  max-width: 450px;
}
.register-card {
  background: white;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
}
.register-title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 30px;
  font-weight: bold;
}
.register-form {
  margin-bottom: 20px;
}
.register-button {
  margin-top: 10px;
}
.register-footer {
  text-align: center;
  margin-top: 20px;
}
.login-link {
  color: #409eff;
  text-decoration: none;
  font-size: 14px;
}
.login-link:hover {
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
  .register-card {
    padding: 30px 20px;
    margin: 0 15px;
  }
  .footer-links {
    flex-direction: column;
    gap: 15px;
  }
}
</style>
