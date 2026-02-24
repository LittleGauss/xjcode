<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <!-- 主内容区 -->
    <div class="main-content">
      <div class="content-wrapper">
        <!-- 页面标题 -->
        <div class="title-bar">
          <h2 class="title-text">个人信息</h2>
          <el-button type="primary" @click="goToSignatureManager">
            管理我的电子签名
          </el-button>
        </div>
        <!-- 个人信息卡片 -->
        <el-card class="user-info-card" shadow="hover">
          <div class="user-info">
            <el-form
              ref="userFormRef"
              :rules="userRules"
              :model="user"
              label-width="90px"
              size="medium"
              class="info-form"
            >
              <el-row :gutter="24">
                <!-- 左侧表单列（占2/3） -->
                <el-col :span="23">
                  <el-form-item label="用户名">
                    <el-input v-model="user.username" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="昵称">
                    <el-input v-model="user.nickname" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="手机号" prop="phone">
                    <el-input
                      v-model="user.phone"
                      placeholder="请输入手机号"
                      style="border-radius: 6px"
                    ></el-input>
                  </el-form-item>
                  <el-form-item label="邮箱" prop="email">
                    <el-input
                      v-model="user.email"
                      placeholder="请输入邮箱"
                      style="border-radius: 6px"
                    ></el-input>
                  </el-form-item>
                  <el-form-item label="职位">
                    <el-input v-model="user.jobTitle" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="角色">
                    <el-input v-model="name" disabled></el-input>
                  </el-form-item>
                  <el-form-item label="权限">
                    <el-input
                      type="textarea"
                      :rows="4"
                      v-model="permissionNamesStr"
                      disabled
                    ></el-input>
                  </el-form-item>
                  <!-- 保存信息按钮 -->
                  <el-form-item style="text-align: center; margin-top: 20px">
                    <el-button
                      type="primary"
                      @click="handleSaveUserInfo"
                      style="width: 180px; border-radius: 6px; padding: 10px 0"
                    >
                      保存修改
                    </el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </div>
        </el-card>
        <!-- 修改密码卡片（带样式） -->
        <el-card class="password-card" shadow="hover" style="margin-top: 0">
          <div slot="header" class="password-card-header">
            <h3 class="password-title">修改密码</h3>
          </div>
          <el-row :gutter="24">
            <el-col :span="23">
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="80px"
                size="small"
                @submit.prevent="handleChangePwd"
                class="pwd-form"
              >
                <el-form-item label="原密码" prop="oldPwd" class="form-item">
                  <el-input
                    v-model="pwdForm.oldPwd"
                    type="password"
                    placeholder="请输入原密码"
                    style="border-radius: 6px"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPwd" class="form-item">
                  <el-input
                    v-model="pwdForm.newPwd"
                    type="password"
                    placeholder="请输入新密码(密码至少8位，必须包含数字大小写字母和字符)"
                    style="border-radius: 6px"
                    show-password
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPwd">
                  <el-input
                    v-model="pwdForm.confirmPwd"
                    type="password"
                    placeholder="请再次输入新密码"
                    style="border-radius: 6px"
                    show-password
                  />
                </el-form-item>
                <el-form-item
                  class="form-item"
                  style="width: 100%; display: flex; justify-content: center"
                >
                  <el-button
                    type="primary"
                    @click="handleChangePwd"
                    style="width: 180px; border-radius: 6px; padding: 10px 0"
                  >
                    提交修改
                  </el-button>
                </el-form-item>
              </el-form>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </div>
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>
<script>
import FooterComponent from "@/components/FooterComponent";
import HeaderComponent from "@/components/HeaderComponent";
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import { encryptPassword } from "@/utils/encrypt";

export default {
  name: "UserCenter",
  components: { HeaderComponent, FooterComponent },
  data() {
    // 手机号校验规则
    const validatePhone = (rule, value, callback) => {
      if (!value || value.trim() === "") {
        callback(new Error("请输入手机号"));
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        callback(new Error("请输入正确的11位手机号"));
      } else {
        callback();
      }
    };

    // 邮箱校验规则
    const validateEmail = (rule, value, callback) => {
      if (!value || value.trim() === "") {
        callback(new Error("请输入邮箱"));
      } else if (
        !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)
      ) {
        callback(new Error("请输入正确的邮箱格式"));
      } else {
        callback();
      }
    };
    const validateOldPwd = (rule, value, callback) => {
      this.oldPwdError = ""; // 清除后端错误提示
      if (!value || value.trim() === "") {
        callback(new Error("请输入原密码"));
      } else {
        callback();
      }
    };

    // 新密码校验规则（前端）
    const validateNewPwd = (rule, value, callback) => {
      if (!value || value.trim() === "") {
        callback(new Error("请输入新密码"));
      } else if (value.length < 8) {
        callback(new Error("新密码长度不能小于8位"));
      } else {
        // 拆分校验：分别检查数字、大写字母、小写字母、特殊字符
        const hasNumber = /\d/.test(value); // 数字
        const hasUpperLetter = /[A-Z]/.test(value); // 大写字母
        const hasLowerLetter = /[a-z]/.test(value); // 小写字母
        const hasSpecial = /[!@#$%^&*(),.?":{}|<>]/.test(value); // 特殊字符

        // 必须同时满足四种类型
        if (!hasNumber) {
          callback(new Error("新密码必须包含数字"));
        } else if (!hasUpperLetter) {
          callback(new Error("新密码必须包含大写字母"));
        } else if (!hasLowerLetter) {
          callback(new Error("新密码必须包含小写字母"));
        } else if (!hasSpecial) {
          callback(new Error('新密码必须包含特殊字符（!@#$%^&*(),.?":{}|<>）'));
        } else if (value === this.pwdForm.oldPwd) {
          callback(new Error("新密码不能与原密码一致"));
        } else {
          callback();
        }
      }
    };

    // 确认密码校验规则（前端）
    const validateConfirmPwd = (rule, value, callback) => {
      if (!value || value.trim() === "") {
        callback(new Error("请确认新密码"));
      } else if (value !== this.pwdForm.newPwd) {
        callback(new Error("两次输入的新密码不一致"));
      } else {
        callback();
      }
    };
    return {
      user: {},
      name: "",
      roleDesc: "",
      permissionNamesStr: "",
      pwdForm: {
        oldPwd: "",
        newPwd: "",
        confirmPwd: "",
      },
      // 用户信息表单校验规则
      userRules: {
        phone: [{ validator: validatePhone, trigger: "blur" }],
        email: [{ validator: validateEmail, trigger: "blur" }],
      },
      pwdRules: {
        // 表单校验规则
        oldPwd: [{ validator: validateOldPwd, trigger: "blur" }],
        newPwd: [{ validator: validateNewPwd, trigger: "blur" }],
        confirmPwd: [{ validator: validateConfirmPwd, trigger: "blur" }],
      },
      oldPwdError: "",
    };
  },
  methods: {
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      if (routeName == "UserCenter") {
        return;
      }
      this.$router.push({ name: routeName });
    },
    goToSignatureManager() {
      this.$router.push("/signature"); // 根据实际路由路径调整
    },
    // 保存手机号和邮箱信息
    handleSaveUserInfo() {
      if (!this.request) {
        this.$message.error("请求实例未初始化，请刷新页面重试");
        return;
      }

      // 表单校验
      this.$refs.userFormRef.validate((valid) => {
        if (!valid) return;

        // 准备提交数据
        const submitData = {
          phone: this.user.phone,
          email: this.user.email,
          username: this.user.username, // 用于后端定位用户
        };

        // 发起修改请求
        this.request
          .post("/user/update-info", submitData)
          .then((res) => {
            if (res.code === "200") {
              this.$message({
                message: "个人信息修改成功！",
                type: "success",
                center: true,
              });
              // 可以更新本地存储的用户信息
              const storedUserInfo = getUserToken();
              if (storedUserInfo && storedUserInfo.user) {
                storedUserInfo.user.phone = this.user.phone;
                storedUserInfo.user.email = this.user.email;
                // 这里需要根据你的auth工具类更新存储，示例：
                // setUserToken(storedUserInfo);
              }
            } else {
              this.$message.error(res.msg || "修改失败，请稍后重试");
            }
          })
          .catch((err) => {
            let errMsg = "修改信息失败，请稍后重试";
            if (err.response) {
              errMsg = err.response.data?.msg || errMsg;
              if (err.response.status === 401) {
                errMsg = "登录已过期，请重新登录";
                // 跳转到登录页
                setTimeout(() => {
                  this.handleLogout();
                }, 1500);
              } else if (err.response.status === 500) {
                errMsg = "服务器异常，请联系管理员";
              }
            } else if (err.request) {
              errMsg = "网络异常，请检查服务器连接";
            }
            this.$message.error(errMsg);
            console.error("修改个人信息接口错误详情：", err);
          });
      });
    },
    handleChangePwd() {
      if (!this.request) {
        this.$message.error("请求实例未初始化，请刷新页面重试");
        return;
      }
      this.$refs.pwdFormRef.validate((valid) => {
        if (!valid) return; // 前端校验失败则终止

        // 前端校验通过，发起接口请求
        const { oldPwd, newPwd } = this.pwdForm;
        const { password: encryptedOldPwd, isEncrypted } =
          encryptPassword(oldPwd);
        const { password: encryptedNewPwd } = encryptPassword(newPwd);

        this.request
          .post("/user/change-password", {
            oldPassword: encryptedOldPwd,
            newPassword: encryptedNewPwd,
            isEncrypted: isEncrypted,
          })
          .then((res) => {
            const responseMsg = res.msg || "";
            if (res.code === "200") {
              // 修改成功：绿色弹窗
              this.$message({
                message: "密码修改成功，请重新登录！",
                type: "success",
                center: true,
                duration: 1500,
                customClass: "success-popup",
              });
              this.pwdForm = { oldPwd: "", newPwd: "", confirmPwd: "" };
              setTimeout(() => {
                removeUserToken();
                this.login_user = null;
                removeToken();
                this.$router.push("/login");
              }, 1500);
            } else if (res.code === "400") {
              if (
                responseMsg.includes("原密码") ||
                responseMsg.includes("旧密码")
              ) {
                // 原密码错误：红色弹窗
                this.$message.error({
                  message: responseMsg || "原密码输入错误，请重新输入",
                });
              } else {
                // 其他后端错误（如参数异常）：红色弹窗
                this.$message.error({
                  message: responseMsg || "修改失败，请检查输入信息",
                });
              }
            } else {
              this.$message.error({
                message: "请求异常，请稍后重试",
              });
            }
          })
          .catch((err) => {
            let errMsg = "修改密码失败，请稍后重试";
            if (err.response) {
              errMsg = err.response.data?.msg || errMsg;
              // 后端返回原密码错误：红色弹窗
              if (errMsg.includes("原密码") || errMsg.includes("旧密码")) {
                this.$message.error({ message: errMsg });
                return;
              }
              // 其他状态码错误：红色弹窗
              if (err.response.status === 401) {
                errMsg = "登录已过期，请重新登录";
              } else if (err.response.status === 500) {
                errMsg = "服务器异常，请联系管理员";
              }
            } else if (err.request) {
              errMsg = "网络异常，请检查服务器连接";
            }

            // 非原密码错误：红色弹窗
            this.$message.error({
              message: errMsg,
            });
            console.error("密码修改接口错误详情：", err);
          });
      });
    },
  },
  created() {
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    // 1. 从会话存储获取完整用户信息
    const loginUser = getUserToken();
    // 2. 提取嵌套在loginUser内的user对象
    this.user = loginUser?.user || {};
    // 3. 获取username（路径为loginUser.user.username）
    const username = this.user?.username;
    // 4. 校验username是否存在
    if (!username) {
      this.$message.error("当前无法获取用户信息");
      return;
    }
    // 5. 请求后端接口
    this.request
      .get(`/user/username/${username}`)
      .then((res) => {
        console.log("用户详情接口返回：", res);
        // 可将接口返回的用户信息赋值给this.user，用于页面渲染
        this.user = res.data;
      })
      .catch((err) => {
        this.$message.error("获取用户详情失败");
        console.error("接口请求错误：", err);
      });
    // 2. 提取roles数组
    const roles = loginUser?.roles || [];
    const firstRole = roles[0] || {}; // 取第一个角色对象

    // 3. 获取角色名
    this.name = firstRole.name || "无角色";
    // 描述字段已替换为权限显示，不再使用角色描述
    const name = firstRole.name;
    if (!name) {
      this.$message.error("当前无法获取角色信息");
      return;
    }
    // 获取权限名称列表
    this.request
      .get(`/user/permissions/names`)
      .then((res) => {
        if (res.code === "200" && Array.isArray(res.data)) {
          this.permissionNamesStr = res.data.join(", ");
        } else {
          this.permissionNamesStr = "无权限";
        }
      })
      .catch((err) => {
        this.permissionNamesStr = "权限获取失败";
        console.error("权限名称获取错误：", err);
      });
  },
};
</script>
<style lang="scss" scoped>
$headHeight: 148px;
$bottomHeight: 161px;
// 主内容区样式
.main-content {
  padding: 24px 16px;
  .content-wrapper {
    max-width: 1200px;
    width: 100%;
    margin: 0 auto;
    background-color: #fff;
    border-radius: 12px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    padding: 24px;
  }
}

// 标题栏样式
.title-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  .title-text {
    font-size: 20px;
    font-weight: 600;
    color: #333;
  }
}

// 个人信息卡片样式
.user-info-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  &:hover {
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  }
  .user-info {
    padding-top: 20px;
  }
}

// 密码卡片样式
.password-card {
  border-radius: 12px;
  border: none;
  box-shadow: none;
  .password-card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .pwd-form {
    padding-top: 16px;
  }
}
// 表单样式统一
.info-form,
.pwd-form {
  .el-form-item {
    margin-bottom: 16px;
    // 错误提示样式（输入框下方红色文字）
    .el-form-item__error {
      color: #f56c6c !important; // Element UI 标准错误红
      font-size: 12px;
      margin-top: 4px;
      line-height: 1.4;
    }
  }
  .el-input {
    border-radius: 6px;
    border-color: #dcdfe6;
    &:focus {
      border-color: #409eff;
      box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
    }
  }
}
// 成功弹框自定义样式（绿色更醒目）
::v-deep .success-popup {
  font-size: 14px;
  padding: 18px 24px;
  background-color: #f0f9eb !important;
  border-left: 4px solid #67c23a !important;
  color: #198914 !important;
}

.home-container {
  width: 100vw;
  height: 150vh;
  .head {
    height: $headHeight;
    //background-image: url("@/assets/home/top.png");
    &-operate {
      display: flex;
      justify-content: flex-end;
      padding: 65px;
      right: 10%;
      position: relative;
      z-index: 9999; /* 设置一个较大的层级值 */
    }
  }

  .sidebar-item.active {
    background-color: #3b82f6;
    color: white;
  }

  .sidebar-item:hover:not(.active) {
    background-color: #e5e7eb;
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
</style>
