<!-- src/components/PermissionDialog.vue -->
<template>
  <el-dialog
    title="新增权限"
    :visible.sync="dialogVisible"
    :close-on-click-modal="true"
    width="500px"
    @close="handleClose"
  >
    <el-form
      :model="localPermissionForm"
      :rules="permissionRules"
      ref="permissionFormRef"
    >
      <el-form-item label="权限名称" prop="name">
        <el-input v-model="localPermissionForm.name"></el-input>
      </el-form-item>
      <el-form-item label="权限编码" prop="code">
        <el-input v-model="localPermissionForm.code"></el-input>
      </el-form-item>
      <el-form-item label="资源类型">
        <el-select
          v-model="localPermissionForm.resourceType"
          placeholder="请选择资源类型"
        >
          <el-option label="菜单" value="menu"></el-option>
          <el-option label="按钮" value="button"></el-option>
          <el-option label="API" value="api"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="资源路径">
        <el-input v-model="localPermissionForm.resourcePath"></el-input>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="savePermission">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "PermissionDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    permissionForm: {
      type: Object,
      default: () => ({
        name: "",
        code: "",
        resourceType: "",
        resourcePath: "",
      }),
    },
  },
  data() {
    return {
      localPermissionForm: { ...this.permissionForm }, // 创建本地副本避免直接修改 prop
      permissionRules: {
        name: [{ required: true, message: "请输入权限名称", trigger: "blur" }],
        code: [{ required: true, message: "请输入权限编码", trigger: "blur" }],
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
    permissionForm: {
      handler(newVal) {
        this.localPermissionForm = { ...newVal }; // 监听 prop 更新本地数据
      },
      deep: true,
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      this.$refs.permissionFormRef?.clearValidate();
    },
    savePermission() {
      this.$refs.permissionFormRef.validate((valid) => {
        if (valid) {
          this.$emit("save", this.localPermissionForm);
        }
      });
    },
  },
};
</script>
