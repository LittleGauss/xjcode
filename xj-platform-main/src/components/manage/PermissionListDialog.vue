<template>
  <el-dialog
    title="权限列表"
    :visible.sync="permissionVisible"
    :close-on-click-modal="true"
    width="800px"
    @open="handleOpen"
    @close="handleClose"
  >
    <!-- 调整为 角色-权限 二列表格，左边仅显示一次角色名，右侧为对应权限标签 -->
    <el-table :data="rolePermissionRows" v-loading="loading" border>
      <el-table-column prop="roleName" label="角色" width="200">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.roleName }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="权限">
        <template #default="scope">
          <div class="perm-wrap">
            <el-tag
              v-for="(perm, idx) in scope.row.permissions"
              :key="idx"
              size="small"
              class="perm-tag"
              type="success"
            >
              {{ perm.name || perm }}
            </el-tag>
            <span
              v-if="
                !scope.row.permissions || scope.row.permissions.length === 0
              "
              class="empty-text"
            >
              无
            </span>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <template #footer>
      <el-button @click="handleClose">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script>
import { roleApi } from "@/api/usermag";

export default {
  name: "PermissionListDialog",
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      rolePermissionRows: [],
      loading: false,
    };
  },
  computed: {
    permissionVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },
  methods: {
    async handleOpen() {
      await this.loadRolePermissions();
    },
    handleClose() {
      this.$emit("update:visible", false);
    },
    // 加载角色与权限映射，构建表格数据：[{ roleName, permissions: [{id,name,code}, ...] }]
    async loadRolePermissions() {
      this.loading = true;
      try {
        const res = await roleApi.getRoleList();
        if (res.code == 200) {
          const roles = Array.isArray(res.data) ? res.data : [];
          this.rolePermissionRows = roles.map((r) => ({
            roleName: r.name || r.code || "-",
            permissions: Array.isArray(r.permissions) ? r.permissions : [],
          }));
        } else {
          this.$message.error(res.message || "获取角色权限失败");
        }
      } catch (error) {
        this.$message.error("获取角色权限失败: " + (error.message || error));
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.perm-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.perm-tag {
  margin: 2px 4px 2px 0;
}
.empty-text {
  color: #999;
}
</style>
