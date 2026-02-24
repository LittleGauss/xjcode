<template>
  <div class="oa-homepage">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="card-container">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>用户管理</span>
              <div style="float: right">
                <el-button
                  type="primary"
                  @click="showPermissionListDialog = true"
                >
                  权限列表
                </el-button>
                <el-button
                  type="primary"
                  @click="showDepartmentListDialog = true"
                >
                  部门列表
                </el-button>
                <el-button
                  type="primary"
                  @click="showAddDepartmentDialog = true"
                  icon="el-icon-plus"
                >
                  新增部门
                </el-button>
                <el-button
                  type="primary"
                  @click="openAddUserDialog"
                  icon="el-icon-plus"
                >
                  新增用户
                </el-button>
                <el-button
                  type="success"
                  @click="exportUsers"
                  icon="el-icon-download"
                >
                  导出用户
                </el-button>
                <el-button
                  type="warning"
                  @click="showImportDialog = true"
                  icon="el-icon-upload2"
                >
                  批量导入
                </el-button>
              </div>
            </div>
          </template>

          <!-- 搜索条件 -->
          <el-form :inline="true" :model="searchForm" class="demo-form-inline">
            <el-form-item label="用户名">
              <el-input
                v-model="searchForm.username"
                placeholder="用户名"
              ></el-input>
            </el-form-item>
            <el-form-item label="角色">
              <el-select
                v-model="searchForm.role"
                placeholder="请选择角色"
                clearable
              >
                <!-- 动态渲染角色列表，替代硬编码 -->
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="role.name"
                  :value="role.code"
                ></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.status"
                placeholder="请选择状态"
                clearable
              >
                <!-- 关键修改1：将选项值从0改为2，标签保持“注册待审核” -->
                <el-option label="注册待审核" value="2"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchUsers">查询</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </el-form-item>
          </el-form>

          <!-- 用户列表 -->
          <el-table :data="userList" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="username" label="用户名">
              <template #default="scope">
                <el-link type="primary" @click="viewUserDetails(scope.row)">
                  {{ scope.row.username }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱"></el-table-column>
            <el-table-column prop="nickname" label="姓名"></el-table-column>
            <el-table-column prop="status" label="状态">
              <template #default="scope">
                <!-- 补充：适配status=2的显示文案 -->
                <el-tag
                  :type="
                    scope.row.status == 1
                      ? 'success'
                      : scope.row.status == 2
                      ? 'warning'
                      : 'danger'
                  "
                >
                  {{
                    scope.row.status == 1
                      ? "启用"
                      : scope.row.status == 2
                      ? "注册待审核"
                      : "禁用"
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column
              prop="createdAt"
              label="入职时间"
            ></el-table-column>
            <el-table-column label="操作" width="320">
              <template #default="scope">
                <el-button size="mini" @click="editUser(scope.row)"
                  >编辑</el-button
                >
                <el-button
                  size="mini"
                  type="danger"
                  @click="deleteUser(scope.row.id)"
                >
                  删除
                </el-button>
                <el-button
                  size="mini"
                  :type="scope.row.status == 1 ? 'warning' : 'success'"
                  @click="toggleUserStatus(scope.row)"
                >
                  {{ scope.row.status == 1 ? "禁用" : "启用" }}
                </el-button>
                <el-button
                  size="mini"
                  type="info"
                  icon="el-icon-key"
                  @click="directResetPassword(scope.row)"
                >
                  重置密码
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pagination.pageSize"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
          ></el-pagination>
        </el-card>
        <!-- 用户对话框 -->
        <UserDialog
          :visible.sync="showAddUserDialog"
          :edit-mode="editMode"
          :user-form="userForm"
          :department-list="departmentList"
          :role-list="roleList"
          @save="handleSaveUser"
          @update:visible="handleDialogClose"
        />

        <!-- 批量导入对话框 -->
        <el-dialog
          title="批量导入用户"
          :visible.sync="showImportDialog"
          :close-on-click-modal="true"
          width="500px"
        >
          <el-form>
            <el-form-item label="下载模板">
              <el-button type="primary" @click="downloadTemplate"
                >下载模板</el-button
              >
            </el-form-item>
            <el-form-item label="上传文件">
              <el-upload
                ref="uploadRef"
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :file-list="fileList"
                :limit="1"
                accept=".xlsx,.xls"
                class="upload-demo"
              >
                <el-button size="small" type="primary">选取文件</el-button>
                <div slot="tip" class="el-upload__tip">
                  只能上传xlsx/xls文件，且不超过10MB
                </div>
              </el-upload>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="showImportDialog = false">取 消</el-button>
            <el-button
              type="primary"
              @click="submitUpload"
              :loading="uploadLoading"
            >
              上 传
            </el-button>
          </template>
        </el-dialog>

        <!-- 用户详情对话框 -->
        <el-dialog
          title="用户详情"
          :visible.sync="showUserDetailsDialog"
          :close-on-click-modal="true"
          width="600px"
        >
          <div v-if="userDetails.user">
            <el-descriptions :column="2" border>
              <!-- 用户基本信息 -->
              <el-descriptions-item label="用户名">
                {{ userDetails.user.username }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ userDetails.user.email }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ userDetails.user.phone }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <!-- 补充：详情页适配status=2的显示 -->
                <el-tag
                  :type="
                    userDetails.user.status == 1
                      ? 'success'
                      : userDetails.user.status == 2
                      ? 'warning'
                      : 'danger'
                  "
                >
                  {{
                    userDetails.user.status == 1
                      ? "启用"
                      : userDetails.user.status == 2
                      ? "注册待审核"
                      : "禁用"
                  }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="入职时间">
                {{ userDetails.user.createdAt }}
              </el-descriptions-item>

              <!-- 角色信息 -->
              <el-descriptions-item label="角色">
                <el-tag
                  v-for="(role, index) in userDetails.roles"
                  :key="index"
                  :type="getRoleType(role.code)"
                  style="margin-right: 5px"
                >
                  {{ role.name }}
                </el-tag>
              </el-descriptions-item>

              <!-- 权限信息 -->
              <el-descriptions-item label="权限">
                <el-tag
                  v-for="(permission, index) in userDetails.permissions"
                  :key="index"
                  type="info"
                  style="margin-right: 5px"
                >
                  {{ permission }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else>
            <el-skeleton :rows="6" animated />
          </div>
          <template #footer>
            <el-button @click="showUserDetailsDialog = false">关闭</el-button>
          </template>
        </el-dialog>
      </div>
      <!-- 角色管理 -->
      <div class="card-container">
        <el-card>
          <div class="card-header">
            <span>角色管理</span>
            <div style="float: right">
              <el-button
                type="primary"
                @click="showAddRoleDialog = true"
                icon="el-icon-plus"
              >
                新增角色
              </el-button>
              <el-button
                type="success"
                @click="exportRoles"
                icon="el-icon-download"
              >
                导出角色
              </el-button>
            </div>
          </div>
          <!-- 角色列表 -->
          <el-table :data="roleList" style="width: 100%" v-loading="loading">
            <el-table-column prop="id" label="ID" width="80"></el-table-column>
            <el-table-column prop="code" label="编码"></el-table-column>
            <el-table-column prop="name" label="名称"></el-table-column>
            <el-table-column prop="description" label="描述"></el-table-column>
            <el-table-column
              prop="createdAt"
              label="创建时间"
            ></el-table-column>
            <el-table-column
              prop="updatedAt"
              label="更新时间"
            ></el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button size="mini" @click="editRole(scope.row)"
                  >编辑</el-button
                >
                <el-button
                  size="mini"
                  type="danger"
                  @click="deleteRole(scope.row.id)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </div>
      <!-- 角色对话框 -->
      <RoleDialog
        :visible.sync="showAddRoleDialog"
        :role-form="roleForm"
        :permission-list="permissionList"
        @save="handleSaveRole"
      />
      <!-- 权限对话框 -->
      <PermissionDialog
        :visible.sync="showAddPermissionDialog"
        :permission-form="permissionForm"
        @save="handleSavePermission"
      />
      <!-- 部门对话框 -->
      <DepartmentDialog
        :visible.sync="showAddDepartmentDialog"
        :department-form="departmentForm"
        :department-list="departmentList"
        :manager-list="managerList"
        @save="handleSaveDepartment"
      />
      <!-- 权限列表对话框 -->
      <PermissionListDialog :visible.sync="showPermissionListDialog" />

      <DepartmentListDialog
        :visible.sync="showDepartmentListDialog"
        :department-list="departmentList"
        :manager-list="managerList"
        @delete-department="handleDeleteDepartment"
      />
    </main>

    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { roleApi, userApi, permissionApi } from "@/api/usermag"; // 引入用户相关接口
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";

import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import UserDialog from "@/components/manage/UserDialog.vue";
import RoleDialog from "@/components/manage/RoleDialog.vue";
import PermissionDialog from "@/components/manage/PermissionDialog.vue";
import DepartmentDialog from "@/components/manage/DepartmentDialog.vue";
import DepartmentListDialog from "@/components/manage/DepartmentListDialog.vue";
import PermissionListDialog from "@/components/manage/PermissionListDialog.vue";
import { templateApi } from "@/api/inspection";

export default {
  name: "UserManagement",
  components: {
    HeaderComponent,
    FooterComponent,
    UserDialog,
    RoleDialog,
    PermissionDialog,
    DepartmentDialog,
    PermissionListDialog,
    DepartmentListDialog,
  },
  data() {
    // 密码确认验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (!this.editMode && value !== this.userForm.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };

    return {
      loading: false,
      login_user: null, //当前登录的用户
      userRoles: [], //当前登录的用户角色
      userPermissions: [], //当前登录的用户权限
      showUserDetailsDialog: false, // 控制用户详情对话框的显示
      showPermissionListDialog: false,
      showDepartmentListDialog: false,
      permissionLoading: false,
      managerList: [], // 部门主管列表
      showResetPwdDialog: false, // 重置密码弹窗显示状态
      resetPwdLoading: false, // 重置密码加载状态
      resetPwdForm: {
        userId: "", // 要重置密码的用户ID
        username: "", // 要重置密码的用户名
        newPassword: "", // 新密码
        confirmPassword: "", // 确认新密码
      },
      userDetails: {
        user: null,
        roles: [],
        permissions: [],
      }, // 存储用户详情数据
      searchForm: {
        username: "",
        role: "",
        // 关键修改2：初始化status为空，避免默认筛选
        status: "",
      },
      userList: [],
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      showAddUserDialog: false,
      showImportDialog: false,
      // 上传文件列表
      fileList: [],
      uploadLoading: false, // 新增：上传按钮loading状态
      editMode: false,
      userForm: {
        username: "",
        password: "",
        email: "",
        phone: "",
        departmentId: null,
        roleId: [],
        createdAt: "", // 新增：入职时间字段
      },
      userRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
        ],
        role: [{ required: true, message: "请选择角色", trigger: "change" }],
        password: [
          {
            required: true,
            message: "请输入密码",
            trigger: "blur",
            validator: (rule, value, callback) => {
              if (!this.editMode && !value) {
                callback(new Error("请输入密码"));
              } else {
                callback();
              }
            },
          },
          {
            min: 6,
            message: "密码长度不能少于6位",
            trigger: "blur",
            validator: (rule, value, callback) => {
              if (!this.editMode && value && value.length < 6) {
                callback(new Error("密码长度不能少于6位"));
              } else {
                callback();
              }
            },
          },
        ],
        confirmPassword: [
          {
            required: true,
            message: "请确认密码",
            trigger: "blur",
            validator: (rule, value, callback) => {
              if (!this.editMode && !value) {
                callback(new Error("请确认密码"));
              } else {
                callback();
              }
            },
          },
          { validator: validateConfirmPassword, trigger: "blur" },
        ],
      },
      showAddRoleDialog: false,
      roleForm: {
        name: "",
        code: "",
        permissionIds: [],
      },
      roleRules: {
        name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
      },
      permissionList: [], // 权限列表
      departmentList: [], // 部门列表
      roleList: [], // 角色列表
      showAddPermissionDialog: false,
      permissionForm: {
        name: "",
        code: "",
        resourceType: "",
        resourcePath: "",
      },
      permissionRules: {
        name: [{ required: true, message: "请输入权限名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入权限编码", trigger: "blur" }],
      },
      showAddDepartmentDialog: false,
      departmentForm: {
        name: "",
        managerId: null, // 修改字段名为 managerId
        parentId: null,
      },
      departmentRules: {
        name: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
        managerId: [
          { required: true, message: "请选择部门负责人", trigger: "change" },
        ], // 更新验证规则
      },
    };
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles || [];
      this.userPermissions = storedUserInfo.permissions || [];
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    this.loadUsers();
    this.loadRoles();
    this.loadPermissions();
    this.loadDepartments();
  },
  methods: {
    // 加载用户列表
    async loadUsers() {
      this.loading = true;
      try {
        const params = {
          page: this.pagination.currentPage,
          size: this.pagination.pageSize,
          username: this.searchForm.username, // 单独传入用户名
          status: this.searchForm.status, // 单独传入状态
          roleCode: this.searchForm.role,
        };

        // 移除空值参数
        Object.keys(params).forEach((key) => {
          if (
            params[key] == "" ||
            params[key] == null ||
            params[key] == undefined
          ) {
            delete params[key];
          }
        });

        // 关键修改3：确保status参数传递为数字类型（避免后端接收字符串2导致筛选失效）
        if (params.status) {
          params.status = Number(params.status);
        }

        const res = await userApi.getUserList(params);

        if (res.code == 200) {
          this.userList = Array.isArray(res.data.records)
            ? res.data.records
            : [];
          this.pagination.total = res.data.total || 0;
        } else {
          this.$message.error(res.message || "获取用户列表失败");
        }
      } catch (error) {
        this.$message.error("获取用户列表失败: " + (error.message || error));
      } finally {
        this.loading = false;
      }
    },
    // 退出登录
    /**
     * 用户登出功能
     *
     * 执行以下操作：
     * 1. 清除Vuex中的token状态
     * 2. 移除sessionStorage中的登录用户信息
     * 3. 清空当前组件的用户数据
     * 4. 移除token
     * 5. 重定向到登录页面
     */
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      if (routeName == "User") {
        return;
      }
      this.$router.push({ name: routeName });
    },
    // 加载权限列表
    async loadPermissionList() {
      this.permissionLoading = true;
      try {
        const res = await permissionApi.getPermissionList();
        if (res.code == 200) {
          this.permissionList = Array.isArray(res.data) ? res.data : [];
        } else {
          this.$message.error(res.message || "获取权限列表失败");
        }
      } catch (error) {
        this.$message.error("获取权限列表失败: " + (error.message || error));
      } finally {
        this.permissionLoading = false;
      }
    },

    // 搜索用户
    searchUsers() {
      this.pagination.currentPage = 1;
      this.loadUsers();
    },
    // 查看用户详情
    async viewUserDetails(user) {
      try {
        this.loading = true;
        const res = await userApi.getUserDetails(user.id); // 假设后端提供获取用户详情的接口
        if (res.code == 200) {
          this.userDetails = res.data; // 将用户详情数据赋值给 userDetails
          this.showUserDetailsDialog = true; // 显示用户详情对话框
        } else {
          this.$message.error(res.message || "获取用户详情失败");
        }
      } catch (error) {
        this.$message.error("获取用户详情失败: " + (error.message || error));
      } finally {
        this.loading = false;
      }
    },

    // 重置搜索
    resetSearch() {
      this.searchForm = {
        username: "",
        role: "",
        // 关键修改4：重置时清空status筛选条件
        status: "",
      };
      this.pagination.currentPage = 1;
      this.loadUsers();
    },

    // 分页相关
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.pagination.currentPage = 1;
      this.loadUsers();
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.loadUsers();
    },

    // 编辑用户：需要同时展示手机号、部门和角色
    async editUser(user) {
      this.editMode = true;
      try {
        // 拉取用户详情，包含角色与权限
        const res = await userApi.getUserDetails(user.id);
        if (res.code == 200) {
          const detail = res.data || {};
          const u = detail.user || user || {};
          const roles = Array.isArray(detail.roles) ? detail.roles : [];
          this.userForm = {
            id: u.id,
            username: u.username,
            nickname: u.nickname,
            email: u.email,
            phone: u.phone,
            departmentId: u.departmentId || null,
            roleId: roles ? roles.map((role) => role.id) : [],
            password: "",
            confirmPassword: "",
            status: u.status,
            createdAt: u.createdAt,
          };
        } else {
          // 回退到仅使用表格行数据，尽量展示已有字段
          this.userForm = {
            id: user.id,
            username: user.username,
            nickname: user.nickname,
            email: user.email,
            phone: user.phone,
            departmentId: user.departmentId || null,
            roleId: [],
            password: "",
            confirmPassword: "",
            status: user.status,
            createdAt: user.createdAt,
          };
          this.$message.warning(
            res.message || "获取用户详情失败，已回退为基础信息"
          );
        }
      } catch (e) {
        // 网络或接口异常时的回退
        this.userForm = {
          id: user.id,
          username: user.username,
          nickname: user.nickname,
          email: user.email,
          phone: user.phone,
          departmentId: user.departmentId || null,
          roleId: [],
          password: "",
          confirmPassword: "",
          status: user.status,
          createdAt: user.createdAt,
        };
        this.$message.error("获取用户详情失败: " + (e.message || e));
      }
      this.showAddUserDialog = true;
    },

    // 删除用户
    deleteUser(id) {
      this.$confirm("确定要删除该用户吗?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            const res = await userApi.deleteUser(id);
            if (res.code == 200) {
              this.$message.success("删除成功");
              this.loadUsers();
            } else {
              this.$message.error(res.message || "删除失败");
            }
          } catch (error) {
            this.$message.error("删除失败: " + (error.message || error));
          }
        })
        .catch(() => {
          // 用户取消删除
        });
    },
    // 处理保存用户（子组件已校验，接收表单数据）
    handleSaveUser(form) {
      this.saveUser(form);
    },

    // 处理保存角色（接收子组件已校验通过的表单）
    async handleSaveRole(form) {
      try {
        const payload = {
          name: form.name,
          code: form.code,
          description: form.description,
          permissionIds: Array.isArray(form.permissionIds)
            ? form.permissionIds
            : [],
        };

        let res;
        if (form.id) {
          res = await roleApi.updateRole(form.id, payload);
        } else {
          res = await roleApi.addRole(payload);
        }

        if (res && res.code == 200) {
          // 若为编辑场景，同步分配权限以持久化到数据库
          if (form.id) {
            try {
              const assignRes = await permissionApi.assignPermissions(
                form.id,
                payload.permissionIds
              );
              if (!(assignRes && assignRes.code == 200)) {
                this.$message.warning(
                  (assignRes && assignRes.message) || "权限分配可能未生效"
                );
              }
            } catch (e) {
              this.$message.error("权限分配失败: " + (e.message || e));
            }
          }

          this.$message.success(form.id ? "编辑角色成功" : "新增角色成功");
          this.showAddRoleDialog = false;
          await this.loadRoles();
        } else {
          this.$message.error((res && res.message) || "保存角色失败");
        }
      } catch (error) {
        this.$message.error("保存角色失败: " + (error.message || error));
      }
    },

    // 处理保存权限
    handleSavePermission() {
      this.savePermission();
    },

    // 处理保存部门
    handleSaveDepartment(form) {
      this.saveDepartment(form);
    },

    // 切换用户状态
    async toggleUserStatus(user) {
      try {
        const newStatus = user.status == 1 ? 0 : 1;
        const res = await userApi.toggleUserStatus(user.id, newStatus);
        if (res.code == 200) {
          this.$message.success("操作成功");
          this.loadUsers();
        } else {
          this.$message.error(res.message || "操作失败");
        }
      } catch (error) {
        this.$message.error("操作失败: " + (error.message || error));
      }
    },
    async loadDepartments() {
      const res = await userApi.getDepartmentList();
      this.departmentList = res.data || [];
      this.loadManagers(); // 同时加载部门主管列表
    },
    // 获取部门主管列表
    async loadManagers() {
      try {
        const res = await userApi.getUsersByRole(["ROLE_DEPT_MANAGER"]); // 假设角色编码为 DEPARTMENT_MANAGER
        if (res.code == 200) {
          this.managerList = res.data || [];
        } else {
          this.$message.error(res.message || "获取部门主管列表失败");
        }
      } catch (error) {
        this.$message.error(
          "获取部门主管列表失败: " + (error.message || error)
        );
      }
    },
    async loadRoles() {
      const res = await roleApi.getRoleList();
      this.roleList = res.data || [];
    },
    // 保存用户（子组件已经做过校验）
    async saveUser(form) {
      try {
        if (this.editMode) {
          // 更新基本信息
          const basicRes = await userApi.updateUser(form.id, {
            nickname: form.nickname,
            email: form.email,
            phone: form.phone,
            departmentId: form.departmentId,
            status: form.status,
            createdAt: form.createdAt,
          });
          if (basicRes.code != 200) {
            this.$message.error(basicRes.message || "编辑失败");
            return;
          }
          // 更新角色（覆盖）
          const roleId = form.roleId;
          const roleRes = await userApi.updateUserRoles(
            form.id,
            roleId ? [roleId] : []
          );
          if (roleRes.code != 200) {
            this.$message.warning(roleRes.message || "角色更新失败");
          }
          this.$message.success("编辑成功");
          this.showAddUserDialog = false;
          this.loadUsers();
        } else {
          // 新增用户
          const addRes = await userApi.addUser(form);
          if (addRes.code == 200) {
            // 新增后如果需要立刻分配角色，可以在后端返回新ID后再调用 updateUserRoles
            this.$message.success("新增成功");
            this.showAddUserDialog = false;
            this.loadUsers();
          } else {
            this.$message.error(addRes.message || "新增失败");
          }
        }
      } catch (error) {
        this.$message.error(
          (this.editMode ? "编辑失败" : "新增失败") +
            ": " +
            (error.message || error)
        );
      }
    },
    // 导出用户
    async exportUsers() {
      try {
        const response = await userApi.exportUsers();
        console.log("response.code:", response.code);
        if (response.code == 200) {
          console.log("downloadUrl:", response.data.downloadUrl);
          // 直接跳转到下载链接
          window.open(response.data.downloadUrl, "_blank");
        } else {
          this.$message.error("导出失败1");
        }
      } catch (error) {
        this.$message.error("导出失败2: " + (error.message || error));
      }
    },
    // 导出角色
    async exportRoles() {
      try {
        const response = await roleApi.exportRole();
        console.log("response.code:", response.code);
        if (response.code == 200) {
          console.log("downloadUrl:", response.data.downloadUrl);
          // 直接跳转到下载链接
          window.open(response.data.downloadUrl, "_blank");
        } else {
          this.$message.error("导出失败1");
        }
      } catch (error) {
        this.$message.error("导出失败2: " + (error.message || error));
      }
    },

    // 下载模板
    async downloadTemplate() {
      try {
        const res = await templateApi.downloadUserTemplate();
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error(
              "文件找不到，请确认是否上传user-template.xlsx文件."
            );
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = "user-template.xlsx";
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },

    async submitUpload() {
      if (this.fileList.length == 0) {
        this.$message.warning("请先选择文件");
        return;
      }
      this.uploadLoading = true; // 显示加载状态
      // 2. 构建FormData（必须用FormData传递文件）
      const file = this.fileList[0].raw;
      const formData = new FormData();
      formData.append("file", file);

      try {
        // 3. 发送上传请求
        const res = await userApi.importUsers(formData);
        // 4. 处理响应结果（根据后端Result格式调整）
        if (res.code == "200" || res.code == 200) {
          this.$message.success("用户批量导入成功！");
          this.showImportDialog = false;
          this.fileList = [];
          this.$refs.uploadRef.clearFiles(); // 清空上传组件的文件列表
          this.loadUsers(); // 直接刷新用户列表（替代无效的$emit）
        } else {
          this.$message.error(`导入失败：${res.data || "未知错误"}`);
        }
      } catch (error) {
        // 5. 异常处理
        console.error("导入请求失败：", error);
        this.$message.error(error);
      } finally {
        this.uploadLoading = false; // 关闭加载状态
      }
    },

    handleFileChange(file, fileList) {
      if (fileList.length == 0) {
        this.$message.warning("请先选择文件");
        return;
      }
      // 只保留最新选择的文件
      this.fileList = [file];

      // 校验文件类型（加强版）
      const fileName = file.name;
      const suffix = fileName
        .substring(fileName.lastIndexOf("."))
        .toLowerCase();
      if (suffix !== ".xlsx" && suffix !== ".xls") {
        this.$message.error("只能上传xlsx/xls格式的Excel文件！");
        this.fileList = [];
        return;
      }

      // 校验文件大小（10MB）
      const fileSize = file.raw.size / 1024 / 1024; // 转为MB
      if (fileSize > 10) {
        this.$message.error("上传文件大小不能超过10MB！");
        this.fileList = [];
      }
    },

    // 获取角色类型
    getRoleType(role) {
      const roleMap = {
        ROLE_ADMIN: "danger",
        ROLE_USER: "info",
        ROLE_AUDITOR: "warning",
      };
      return roleMap[role] || "info";
    },

    // 获取角色名称
    getRoleName(role) {
      const roleMap = {
        admin: "管理员",
        user: "普通用户",
        auditor: "审核员",
      };
      return roleMap[role] || role;
    },
    // 对话框关闭
    handleDialogClose() {
      this.$refs.userFormRef?.clearValidate();
      this.resetUserForm();
    },

    // 重置用户表单
    resetUserForm() {
      this.userForm = {
        id: null,
        username: "",
        email: "",
        role: "",
        // 关键修改：新增默认密码
        password: "123456Ab.",
        confirmPassword: "123456Ab.",
        status: 1,
        createdAt: "",
      };
      this.editMode = false;
    },
    async loadPermissions() {
      const res = await permissionApi.getPermissionList();
      this.permissionList = res.data || [];
    },
    // 编辑角色
    editRole(role) {
      this.roleForm = {
        id: role.id,
        name: role.name,
        code: role.code,
        description: role.description,
        permissionIds: role.permissions
          ? role.permissions.map((p) => p.id)
          : [],
      };
      this.showAddRoleDialog = true;
    },

    // 删除角色
    deleteRole(id) {
      this.$confirm("确定要删除该角色吗? 删除后将无法恢复!", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            const res = await roleApi.deleteRole(id);
            if (res.code == 200) {
              this.$message.success("删除成功");
              this.loadRoles();
            } else {
              this.$message.error(res.message || "删除失败");
            }
          } catch (error) {
            this.$message.error("删除失败: " + (error.message || error));
          }
        })
        .catch(() => {
          // 用户取消删除
        });
    },
    savePermission() {
      this.$refs.permissionFormRef.validate(async (valid) => {
        if (valid) {
          try {
            const res = await permissionApi.addPermission(this.permissionForm);
            if (res.code === 200) {
              this.$message.success("新增权限成功");
              this.showAddPermissionDialog = false;
              this.$emit("reload");
            } else {
              this.$message.error(res.message || "新增权限失败");
            }
          } catch (error) {
            this.$message.error("新增权限失败: " + (error.message || error));
          }
        }
      });
    },
    openAddUserDialog() {
      this.resetUserForm();
      this.showAddUserDialog = true;
    },

    async saveDepartment(form) {
      try {
        const res = await userApi.addDepartment(form);
        if (res.code == 200) {
          this.$message.success("新增部门成功");
          this.showAddDepartmentDialog = false;
          this.loadDepartments(); // 重新加载部门列表
        } else {
          this.$message.error(res.message || "新增部门失败");
        }
      } catch (error) {
        this.$message.error("新增部门失败: " + (error.message || error));
      }
    },
    async handleDeleteDepartment(id) {
      try {
        await this.$confirm("确定要删除该部门吗? 删除后将无法恢复!", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        });

        const res = await userApi.deleteDepartment(id);
        if (res.code == 200) {
          this.$message.success("删除成功");
          this.loadDepartments(); // 重新加载部门列表
        } else {
          this.$message.error(res.message || "删除失败");
        }
      } catch (error) {
        if (error !== "cancel") {
          this.$message.error("删除失败: " + (error.message || error));
        }
      }
    },
    // 直接重置密码为后端默认值（无弹框）
    async directResetPassword(row) {
      // 先弹出确认提示，防止误操作（可选，可删除该确认逻辑）
      try {
        await this.$confirm(
          "确定要将该用户密码重置为默认值吗？\n默认密码：123456Ab.",
          "提示",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
            confirmButtonClass: "el-button--danger",
          }
        );

        // 发起重置密码请求（不传passwordInfo，后端自动使用默认密码）
        const res = await userApi.resetUserPassword(row.id, {});
        if (res.code == 200 || res.code === "200") {
          this.$message.success("密码重置成功！默认密码：123456Ab.");
          // 可选：刷新用户列表（无需刷新，密码重置不影响列表展示）
          // this.loadUsers();
        } else {
          this.$message.error(res.message || "密码重置失败");
        }
      } catch (error) {
        // 用户点击取消时，不提示错误
        if (error !== "cancel") {
          this.$message.error("密码重置失败：" + (error.message || "网络异常"));
        }
      }
    },
  },
};
</script>

<style scoped>
.oa-homepage {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 30px 20px;
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.el-pagination {
  margin-top: 20px;
  text-align: right;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
    gap: 15px;
  }

  .nav-menu {
    margin: 15px 0;
  }

  .footer-links {
    flex-direction: column;
    gap: 15px;
  }

  .card-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .card-header > div {
    float: none !important;
  }
}
</style>
