<template>
  <el-dialog
    title="部门列表"
    :visible.sync="departmentVisible"
    :close-on-click-modal="true"
    width="600px"
    @close="handleClose"
  >
    <!-- 部门表格 -->
    <el-table
      :data="formattedDepartmentList"
      style="width: 100%; margin-top: 20px"
      row-key="id"
      border
      :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
    >
      <el-table-column
        prop="name"
        label="部门名称"
        width="130"
      ></el-table-column>
      <el-table-column
        prop="code"
        label="部门编码"
        width="100"
      ></el-table-column>
      <el-table-column
        prop="parentName"
        label="父部门"
        width="120"
      ></el-table-column>
      <el-table-column
        prop="leaderName"
        label="部门负责人"
        width="120"
      ></el-table-column>
      <el-table-column label="操作" width="100">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" @click="handleDelete(scope.row)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script>
export default {
  name: "DepartmentListDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    departmentList: {
      type: Array,
      default: () => [],
    },
    managerList: {
      // 添加 managerList prop
      type: Array,
      default: () => [],
    },
  },
  data() {
    return {
      loading: false,
    };
  },
  computed: {
    departmentVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
    formattedDepartmentList() {
      const deptMap = {};
      const managerMap = {}; // 添加管理者映射

      // 建立部门映射
      this.departmentList.forEach((dept) => {
        deptMap[dept.id] = dept;
      });

      // 建立管理者映射
      this.managerList.forEach((manager) => {
        managerMap[manager.id] = manager;
      });

      return this.departmentList.map((dept) => {
        return {
          ...dept,
          parentName:
            dept.parentId && deptMap[dept.parentId]
              ? deptMap[dept.parentId].name
              : "-",
          // 添加部门负责人名称
          leaderName:
            dept.leaderId && managerMap[dept.leaderId]
              ? managerMap[dept.leaderId].nickname
              : "-",
        };
      });
    },
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
    },
    // 获取部门列表
    async fetchDepartments() {
      this.loading = true;
    },
    // 删除部门
    async handleDelete(row) {
      try {
        this.$emit("delete-department", row.id);
      } catch (error) {
        // 用户取消删除
      }
    },
  },
};
</script>
