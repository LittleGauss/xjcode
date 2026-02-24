<!-- src/components/RoleDialog.vue -->
<template>
  <el-dialog
    title="编辑新角色"
    :visible.sync="dialogVisible"
    :close-on-click-modal="true"
    width="500px"
    @close="handleClose"
  >
    <el-form :model="localRoleForm" :rules="roleRules" ref="roleFormRef">
      <el-form-item label="角色名称" prop="name">
        <el-input v-model="localRoleForm.name"></el-input>
      </el-form-item>
      <el-form-item label="角色编码" prop="code">
        <el-input v-model="localRoleForm.code"></el-input>
      </el-form-item>
      <el-form-item label="权限">
        <el-checkbox-group v-model="localRoleForm.permissionIds">
          <el-checkbox
            v-for="permission in permissionList"
            :key="permission.id"
            :label="permission.id"
          >
            {{ permission.name }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="saveRole">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "RoleDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    roleForm: {
      type: Object,
      default: () => ({
        name: "",
        code: "",
        permissionIds: [],
      }),
    },
    permissionList: {
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      localRoleForm: { ...this.roleForm }, // 创建本地副本避免直接修改 prop
      roleRules: {
        name: [{ required: true, message: "请输入角色名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入角色编码", trigger: "blur" }],
      },
    };
  },
  computed: {
    dialogVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },
  watch: {
    roleForm: {
      handler(newVal) {
        this.localRoleForm = { ...newVal }; // 监听外部变化同步到本地
      },
      deep: true,
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      this.$refs.roleFormRef?.clearValidate();
    },
    saveRole() {
      this.$refs.roleFormRef.validate((valid) => {
        if (valid) {
          this.$emit("save", this.localRoleForm); // 使用本地表单提交
        }
      });
    },
  },
};
</script>
