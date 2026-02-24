<template>
  <div class="oa-homepage">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />
    <main class="main-content">
      <!-- 数据表格 -->
      <el-card class="table-card">
        <h2>我的电子签名管理</h2>
        <el-button type="primary" @click="fetchData" style="margin-left: 10px"
          >查询所有</el-button
        >
        <el-button
          type="success"
          @click="showAddSignature"
          style="margin-left: 10px"
          >新增我的签名</el-button
        >
        <el-table
          :data="tableData"
          border
          stripe
          v-loading="loading"
          element-loading-text="数据加载中..."
        >
          <el-table-column prop="id" label="签名ID" width="80" />
          <el-table-column
            prop="signUrl"
            label="签名图片(双击可查看)"
            min-width="200"
          >
            <template slot-scope="{ row }">
              <img
                :src="row.fullImageUrl"
                style="width: 100px; height: 50px; object-fit: contain"
                @dblclick="handleImageDoubleClick(row)"
                v-if="row.fullImageUrl"
              />
              <div v-else>
                <i class="el-icon-picture-outline"></i>
                无签名图片
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="签名时间" width="180">
            <template slot-scope="{ row }">
              {{ formatDate(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="ext1"
            label="备注"
            min-width="150"
            show-overflow-tooltip
          />
          <el-table-column label="操作" width="150" fixed="right">
            <template slot-scope="{ row }">
              <div style="white-space: nowrap">
                <el-button
                  size="small"
                  type="danger"
                  @click="deleteSignature(row.id)"
                  style="margin-left: 5px"
                  >删除</el-button
                >
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <el-pagination
          :current-page="pagination.currentPage"
          :page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          style="margin-top: 20px; text-align: right"
        />
      </el-card>

      <!-- 新增签名弹窗 -->
      <el-dialog
        title="新增签名"
        :visible.sync="addDialogVisible"
        width="500px"
        :close-on-click-modal="true"
        :before-close="handleAddDialogClose"
      >
        <SignatureCreate
          ref="signatureCreate"
          @success="handleSignatureSuccess"
        />
      </el-dialog>
    </main>
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import SignatureCreate from "@/components/manage/SignatureCreate.vue";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import signatureApi from "@/api/signature";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "SignatureManager",
  components: {
    SignatureCreate,
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      // 表格数据
      tableData: [],
      loading: false,

      // 分页参数
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      login_user: null,
      userRoles: [],

      // 弹窗相关
      addDialogVisible: false,
    };
  },
  methods: {
    // 双击图片放大处理
    handleImageDoubleClick(row) {
      if (row.fullImageUrl) {
        // 使用 Element UI 的 Dialog 组件显示大图
        this.$alert(
          `
        <div style="text-align: center;">
          <img src="${row.fullImageUrl}" style="max-width: 100%; max-height: 80vh;" />
        </div>
      `,
          "图片预览",
          {
            dangerouslyUseHTMLString: true,
            confirmButtonText: "关闭",
            customClass: "image-preview-dialog",
            closeOnClickModal: true, // 点击遮罩层关闭
            closeOnPressEscape: true, // 按ESC键关闭
          }
        ).catch(() => {
          console.log("handleImageDoubleClick:cancel");
        });
      }
    },
    //图片URl
    async getFullImageUrl(path) {
      if (!path) return "";

      // 如果已经是HTTP链接，直接返回
      if (path.startsWith("http")) {
        console.log("直接返回HTTP链接:", path);

        return path;
      }

      // 获取预签名URL
      try {
        const res = await signatureApi.getImageUrl(path); // 需要替换为实际的API方法
        if (res.code == 200) {
          return res.data;
        } else {
          this.$message.error(res.msg || "获取图片链接失败");
          return "";
        }
      } catch (error) {
        this.$message.error("获取图片链接失败");
        return "";
      }
    },

    // 格式化日期时间
    formatDate(datetime) {
      if (!datetime) return "";
      return datetime.replace("T", " ");
    },

    // 获取签名记录列表
    async fetchData() {
      this.loading = true;
      try {
        const userId = this.login_user ? this.login_user.id : "";

        if (!userId) {
          this.$message.warning("无法获取用户信息");
          this.loading = false;
          return;
        }

        // 请求参数
        const params = {
          pageNum: this.pagination.currentPage,
          pageSize: this.pagination.pageSize,
          userId: userId,
        };

        const response = await signatureApi.listSignatures(params);
        if (response.code == 200) {
          const records = response.data.records || [];
          this.tableData = await Promise.all(
            records.map(async (item) => {
              if (item.signUrl) {
                item.fullImageUrl = await this.getFullImageUrl(item.signUrl);
              }
              return item;
            })
          );
          this.pagination.total = response.data.total || 0;
        } else {
          this.$message.error(response.msg || "获取数据失败");
        }
      } catch (error) {
        this.$message.error("获取数据失败: " + error.message);
      } finally {
        this.loading = false;
      }
    },

    // 删除签名
    deleteSignature(id) {
      this.$confirm("确定要删除该签名吗？删除后不可恢复", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            const response = await signatureApi.deleteSignature(id);
            if (response.code == 200) {
              this.$message.success("删除成功");
              this.fetchData();
            } else {
              this.$message.error(response.msg || "删除失败");
            }
          } catch (error) {
            this.$message.error("删除失败: " + error.message);
          }
        })
        .catch(() => {
          console.log("取消删除");
        });
    },
    // 处理分页变化
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.fetchData();
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.fetchData();
    },

    // 显示新增签名弹窗
    showAddSignature() {
      this.addDialogVisible = true;
    },

    // 处理新增签名弹窗关闭
    handleAddDialogClose(done) {
      this.$confirm("确定要关闭吗？未保存的签名将会丢失", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          done();
        })
        .catch(() => {
          // 取消关闭
        });
    },

    // 处理签名成功提交
    handleSignatureSuccess() {
      this.addDialogVisible = false;
      this.$message.success("签名已保存");
      // 重新加载数据
      this.fetchData();
    },
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "AboutPage") {
        return; // 如果已经在关于我们页面，不进行跳转
      }
      this.$router.push({ name: routeName });
    },

    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
  },
  created() {
    // 从会话存储获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
  mounted() {
    this.fetchData();
  },
};
</script>

<style scoped>
.oa-homepage {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 主要内容样式 */
.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 15px 20px;
  width: 100%;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.signature-detail {
  padding: 20px 0;
}

.signature-preview {
  margin-top: 20px;
  text-align: center;
}

.dialog-footer {
  text-align: right;
}

/* 图片预览对话框样式 */
.image-preview-dialog {
  width: auto !important;
  min-width: 300px;
  max-width: 90vw;
}

.image-preview-dialog .el-message-box__content {
  text-align: center;
}
</style>
