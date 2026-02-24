<!-- src/views/ForceChangePassword.vue -->
<template>
  <div class="force-change-page">
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

    <main class="main-content">
      <div class="container">
        <div class="card">
          <h2 class="title">密码强度较弱</h2>
          <p class="desc">为保障账号安全，请先修改密码后再继续使用系统。</p>

          <el-form
            :model="pwdForm"
            :rules="rules"
            ref="pwdFormRef"
            class="pwd-form"
            @submit.native.prevent="handleChangePwd"
            @keyup.enter.native="handleChangePwd"
          >
            <el-form-item prop="oldPwd">
              <el-input
                v-model="pwdForm.oldPwd"
                type="password"
                placeholder="请输入当前密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>

            <el-form-item prop="newPwd">
              <el-input
                v-model="pwdForm.newPwd"
                type="password"
                placeholder="请输入新密码（密码至少8位，需包含字母、数字和字符）"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>

            <el-form-item prop="confirmPwd">
              <el-input
                v-model="pwdForm.confirmPwd"
                type="password"
                placeholder="请确认新密码"
                prefix-icon="el-icon-lock"
                size="large"
                show-password
              ></el-input>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="handleChangePwd"
                class="submit-btn"
                block
              >
                {{ loading ? "提交中..." : "修改密码" }}
              </el-button>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                @click="intoFistPage"
                class="submit-btn"
                block
                style="float: right"
              >
                稍后再改
              </el-button>
            </el-form-item>
          </el-form>

          <div class="tips">
            建议：密码至少 8 位，且包含数字/字母/特殊字符。
          </div>
        </div>
      </div>
    </main>

    <footer class="footer">
      <div class="footer-content">
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
import request from "@/utils/request";
import { encryptPassword } from "@/utils/encrypt";
import { validatePassword } from "@/utils/validate";
import { removeToken, removeUserToken } from "@/utils/auth";

export default {
  name: "ForceChangePassword",
  data() {
    const validateNewPwd = (rule, value, callback) => {
      if (!value) return callback(new Error("请输入新密码"));
      if (!validatePassword(value)) {
        return callback(new Error("密码至少8位，需包含字母、数字和字符"));
      }
      if (this.pwdForm.confirmPwd) {
        this.$refs.pwdFormRef.validateField("confirmPwd");
      }
      callback();
    };

    const validateConfirmPwd = (rule, value, callback) => {
      if (!value) return callback(new Error("请确认新密码"));
      if (value !== this.pwdForm.newPwd) {
        return callback(new Error("两次输入密码不一致!"));
      }
      callback();
    };

    return {
      loading: false,
      pwdForm: {
        oldPwd: "",
        newPwd: "",
        confirmPwd: "",
      },
      rules: {
        oldPwd: [
          { required: true, message: "请输入当前密码", trigger: "blur" },
        ],
        newPwd: [
          { required: true, validator: validateNewPwd, trigger: "blur" },
        ],
        confirmPwd: [
          { required: true, validator: validateConfirmPwd, trigger: "blur" },
        ],
      },
    };
  },
  methods: {
    handleChangePwd() {
      this.$refs.pwdFormRef.validate(async (valid) => {
        if (!valid) return;

        this.loading = true;
        try {
          const { password: encryptedOldPwd, isEncrypted } = encryptPassword(
            this.pwdForm.oldPwd
          );
          const { password: encryptedNewPwd } = encryptPassword(
            this.pwdForm.newPwd
          );

          const res = await request.post("/user/change-password", {
            oldPassword: encryptedOldPwd,
            newPassword: encryptedNewPwd,
            isEncrypted: isEncrypted,
          });

          const ok = res.code === 200 || res.code === "200";
          if (ok) {
            this.$message({
              message: "密码修改成功，请重新登录！",
              type: "success",
              duration: 1500,
            });

            setTimeout(() => {
              removeUserToken();
              removeToken();
              this.$router.replace("/login");
            }, 1500);
            return;
          }

          const responseMsg = res.msg || res.message || "修改失败，请稍后重试";
          this.$message.error(responseMsg);
        } catch (err) {
          let errMsg = "修改密码失败，请稍后重试";
          if (err && err.response) {
            errMsg = err.response.data?.msg || errMsg;
            if (errMsg.includes("原密码") || errMsg.includes("旧密码")) {
              this.$message.error({ message: errMsg });
              return;
            }
            if (err.response.status === 401) errMsg = "登录已过期，请重新登录";
            if (err.response.status === 500)
              errMsg = "服务器异常，请联系管理员";
          } else if (err && err.request) {
            errMsg = "网络异常，请检查服务器连接";
          }
          this.$message.error({ message: errMsg });
          // eslint-disable-next-line no-console
          console.error("密码修改接口错误详情：", err);
        } finally {
          this.loading = false;
        }
      });
    },
    intoFistPage() {
      this.$router.push("/home-first");
    },
  },
};
</script>

<style scoped>
.force-change-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

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

.main-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.container {
  width: 100%;
  max-width: 480px;
}

.card {
  background: white;
  border-radius: 12px;
  padding: 40px 30px;
  box-shadow: 0 15px 35px rgba(50, 50, 93, 0.1), 0 5px 15px rgba(0, 0, 0, 0.07);
}

.title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
  font-weight: bold;
}

.desc {
  text-align: center;
  color: #666;
  margin-bottom: 20px;
}

.pwd-form {
  margin-top: 10px;
}

.submit-btn {
  margin-top: 10px;
}

.tips {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.footer {
  background: #2c3e50;
  color: white;
  padding: 20px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.copyright p {
  margin: 5px 0;
  color: #95a5a6;
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
  }

  .card {
    padding: 30px 20px;
    margin: 0 15px;
  }
}
</style>
