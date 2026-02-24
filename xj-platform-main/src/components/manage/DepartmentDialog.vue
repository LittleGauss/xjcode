<template>
  <el-dialog
    title="新增部门"
    :visible.sync="departVisible"
    :close-on-click-modal="true"
    width="500px"
    @close="handleClose"
  >
    <el-form
      :model="localDepartmentForm"
      :rules="departmentRules"
      ref="departmentFormRef"
    >
      <el-form-item label="部门名称" prop="name">
        <el-input v-model="localDepartmentForm.name"></el-input>
      </el-form-item>
      <el-form-item label="部门负责人" prop="leaderId">
        <el-select
          v-model="localDepartmentForm.leaderId"
          placeholder="请选择部门负责人"
          clearable
        >
          <el-option
            v-for="user in managerList"
            :key="user.id"
            :label="user.nickname"
            :value="user.id"
          ></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="父部门">
        <el-select
          v-model="localDepartmentForm.parentId"
          placeholder="请选择父部门"
        >
          <el-option
            v-for="dept in departmentList"
            :key="dept.id"
            :label="dept.name"
            :value="dept.id"
          ></el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="saveDepartment">保存</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "DepartmentDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    departmentForm: {
      type: Object,
      default: () => ({
        name: "",
        code: "",
        parentId: null,
      }),
    },
    departmentList: {
      type: Array,
      default: () => [],
    },
    managerList: {
      // 新增：部门主管列表
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      localDepartmentForm: { ...this.departmentForm }, // 创建本地副本
      departmentRules: {
        name: [{ required: true, message: "请输入部门名称", trigger: "blur" }],
        leaderId: [
          { required: true, message: "请选择部门负责人", trigger: "blur" },
        ],
      },
    };
  },
  computed: {
    departVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },
  watch: {
    departmentForm: {
      handler(newVal) {
        this.localDepartmentForm = { ...newVal }; // 监听外部变化同步更新
      },
      deep: true,
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      // 更安全的检查方式
      if (
        this.$refs.departmentFormRef &&
        typeof this.$refs.departmentFormRef.clearValidate === "function"
      ) {
        this.$refs.departmentFormRef.clearValidate();
      }
    },
    saveDepartment() {
      // 同样需要检查 ref 是否存在
      if (this.$refs.departmentFormRef) {
        this.$refs.departmentFormRef.validate((valid) => {
          if (valid) {
            this.$emit("save", this.localDepartmentForm);
          }
        });
      }
    },
  },
};
</script>
