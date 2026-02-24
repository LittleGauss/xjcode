<template>
  <div class="oa-homepage">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="app-container">
      <el-form
        :model="queryParams"
        ref="queryForm"
        :inline="true"
        v-show="showSearch"
        label-width="68px"
      >
        <el-form-item label="系统模块" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入系统模块"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item label="操作人员" prop="operName">
          <el-input
            v-model="queryParams.operName"
            placeholder="请输入操作人员"
            clearable
            size="small"
            @keyup.enter.native="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            icon="el-icon-search"
            size="mini"
            @click="handleQuery"
            >搜索</el-button
          >
          <el-button icon="el-icon-refresh" size="mini" @click="resetQuery"
            >重置</el-button
          >
        </el-form-item>
      </el-form>
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="danger"
            plain
            icon="el-icon-delete"
            size="mini"
            :disabled="multiple"
            @click="handleDelete"
            >批量删除</el-button
          >
        </el-col>
      </el-row>

      <el-table
        v-loading="loading"
        :data="list"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="日志编号" align="center" prop="operId" />
        <el-table-column label="系统模块" align="center" prop="title" />
        <el-table-column label="业务类型" align="center" prop="businessType">
          <template slot-scope="scope">
            <el-tag>{{ formatType(scope.row.businessType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="请求方法" align="center" prop="requestMethod" />
        <el-table-column label="操作人员" align="center" prop="operName" />
        <el-table-column
          label="主机"
          align="center"
          prop="operIp"
          width="130"
          :show-overflow-tooltip="true"
        />
        <el-table-column label="状态" align="center" prop="status">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">
              {{ scope.row.status === 0 ? "成功" : "失败" }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="操作时间"
          align="center"
          prop="operTime"
          width="180"
        >
          <template slot-scope="scope">
            <span>{{ parseTime(scope.row.operTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="消耗时间" align="center" prop="costTime">
          <template slot-scope="scope">
            <span>{{ scope.row.costTime }}毫秒</span>
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          align="center"
          class-name="small-padding fixed-width"
        >
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-view"
              @click="handleView(scope.row)"
              >详细</el-button
            >
            <el-button
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleDelete(scope.row)"
              >删除</el-button
            >
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; text-align: right">
        <el-pagination
          v-show="total > 0"
          :current-page="queryParams.pageNum"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="queryParams.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>

      <el-dialog
        title="操作日志详细"
        :visible.sync="open"
        :close-on-click-modal="true"
        width="700px"
        append-to-body
      >
        <el-form ref="form" :model="form" label-width="100px" size="mini">
          <el-row>
            <el-col :span="12">
              <el-form-item label="操作模块：">{{ form.title }}</el-form-item>
              <el-form-item label="请求地址：">{{ form.operUrl }}</el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="请求方式：">{{
                form.requestMethod
              }}</el-form-item>
              <el-form-item label="操作方法：">{{ form.method }}</el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="请求参数：">{{
                form.operParam
              }}</el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="返回参数：">{{
                form.jsonResult
              }}</el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="异常信息：" v-if="form.status == 1">{{
                form.errorMsg
              }}</el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="open = false">关 闭</el-button>
        </div>
      </el-dialog>
    </div>

    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { list, delOperlog } from "@/api/operlog";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";

import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";

export default {
  name: "OperLog",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0, // [新增] 总条数
      list: [],
      open: false,
      // 选中数组
      ids: [],
      // 非多个禁用
      multiple: true,
      login_user: null,
      userRoles: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
      },
      form: {},
      // 简单模拟字典
      typeOptions: [
        "其它",
        "新增",
        "修改",
        "删除",
        "授权",
        "导出",
        "导入",
        "强退",
        "生成代码",
        "清空数据",
        "上传",
      ],
    };
  },
  created() {
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;

      //在控制台打印您当前已加载的 "角色"
      console.log("当前登录用户的角色 (User Roles):", this.userRoles);

      //尝试从登录信息中获取 "权限"
      //权限被存储在 storedUserInfo.permissions
      if (storedUserInfo.permissions) {
        this.userPermissions = storedUserInfo.permissions;

        //打印
        console.log("当前登录用户的权限 (User Permissions):");
        console.log(
          "当前登录用户的权限 (User Permissions):",
          this.userPermissions
        );
      } else {
        //如果取不到，就打印警告
        console.warn("未能从 Session Storage 中加载 userPermissions！");
        console.log(
          "（这很正常，通常意味着您需要在登录时将权限数据存入 Session）"
        );
      }
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      list(this.queryParams).then((response) => {
        // [修改] 适配后端返回结构，通常包含 records(列表) 和 total(总数)
        this.list = response.data.records;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      // 建议这样重置，保留 title/operName 键，防止 undefined 导致响应式丢失
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
        operName: undefined,
      };
      this.getList();
    },
    /** 【新增 5】 多选框选中数据 */
    handleSelectionChange(selection) {
      // 获取选中的所有 operId
      this.ids = selection.map((item) => item.operId);
      // 如果没有选中项，multiple 为 true (禁用删除按钮)
      this.multiple = !selection.length;
    },

    /** 【新增 6】 删除按钮操作 */
    handleDelete(row) {
      // 这一行逻辑：如果是点击行内删除，取 row.operId；如果是点击批量删除，取 this.ids
      const operIds = row.operId || this.ids;

      this.$confirm(
        '是否确认删除日志编号为"' + operIds + '"的数据项？',
        "系统提示",
        {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(function () {
          return delOperlog(operIds);
        })
        .then(() => {
          this.getList();
          this.$message.success("删除成功");
        })
        .catch(() => {});
    },
    /** [新增] 每页条数改变 */
    handleSizeChange(val) {
      this.queryParams.pageSize = val;
      this.getList();
    },
    /** [新增] 页码改变 */
    handleCurrentChange(val) {
      this.queryParams.pageNum = val;
      this.getList();
    },
    handleView(row) {
      this.form = row;
      this.open = true;
    },
    formatType(type) {
      return this.typeOptions[type] || "未知";
    },
    parseTime(time) {
      return time ? time.replace("T", " ").substring(0, 19) : "";
    },
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      if (routeName == "oplog") {
        return;
      }
      this.$router.push({ name: routeName });
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
</style>
