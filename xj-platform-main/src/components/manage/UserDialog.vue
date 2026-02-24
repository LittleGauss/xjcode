<template>
  <el-dialog
    :title="editMode ? '编辑用户' : '新增用户'"
    :visible.sync="userDialogVisible"
    width="500px"
    :close-on-click-modal="true"
    @close="handleClose"
  >
    <el-form :model="localUserForm" :rules="userRules" ref="userFormRef">
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="localUserForm.username"
          :disabled="editMode"
        ></el-input>
      </el-form-item>
      <el-form-item label="姓名" prop="nickname">
        <el-input
          v-model="localUserForm.nickname"
          placeholder="请输入姓名"
        ></el-input>
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="!editMode">
        <el-input
          v-model="localUserForm.password"
          type="password"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword" v-if="!editMode">
        <el-input
          v-model="localUserForm.confirmPassword"
          type="password"
          show-password
        ></el-input>
      </el-form-item>
      <el-form-item label="邮箱" prop="email">
        <el-input v-model="localUserForm.email"></el-input>
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="localUserForm.phone"></el-input>
      </el-form-item>
      <el-form-item label="入职时间" prop="createdAt">
        <el-date-picker
          v-model="localUserForm.createdAt"
          type="datetime"
          placeholder="请选择入职时间"
          format="yyyy-MM-dd HH:mm:ss"
          value-format="yyyy-MM-dd HH:mm:ss"
          clearable
          style="width: 100%"
        ></el-date-picker>
      </el-form-item>
      <el-form-item label="部门">
        <el-select
          v-model="localUserForm.departmentId"
          placeholder="请选择部门"
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="角色">
        <el-select
          v-model="localUserForm.roleId"
          multiple
          filterable
          placeholder="请选择角色"
        >
          <el-option
            v-for="role in roleList"
            :key="role.id"
            :label="role.name"
            :value="role.id"
          ></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取 消</el-button>
      <el-button type="primary" @click="saveUser">确 定</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "UserDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    editMode: {
      type: Boolean,
      default: false,
    },
    userForm: {
      type: Object,
      default: () => ({
        username: "",
        nickname: "",
        password: "",
        confirmPassword: "",
        email: "",
        phone: "",
        departmentId: null,
        roleId: [],
        createdAt: "", // 新增：创建时间字段
      }),
    },
    departmentList: {
      type: Array,
      default: () => [],
    },
    roleList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (!this.editMode && value !== this.localUserForm.password) {
        callback(new Error("两次输入密码不一致!"));
      } else {
        callback();
      }
    };

    return {
      localUserForm: { ...this.userForm },
      userRules: {
        createdAt: [
          { required: true, message: "请选择入职时间", trigger: "change" },
        ],
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
        ],
        nickname: [
          { required: true, message: "请输入姓名", trigger: "blur" },
          {
            min: 2,
            max: 20,
            message: "长度在 2 到 20 个字符",
            trigger: "blur",
          },
        ],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
        ],
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
    };
  },
  computed: {
    userDialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },
  watch: {
    userForm: {
      handler(newVal) {
        this.localUserForm = { ...newVal };
      },
      deep: true,
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      this.$refs.userFormRef?.clearValidate();
    },
    saveUser() {
      this.$refs.userFormRef.validate((valid) => {
        if (valid) {
          this.$emit("save", this.localUserForm);
        } else {
          this.$message.warning("请完善表单信息");
        }
      });
    },
  },
};
</script>
