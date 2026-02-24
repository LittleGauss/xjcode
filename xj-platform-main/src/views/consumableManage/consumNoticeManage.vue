<!-- 模块用途：专门用来做信息公示，包含两类核心内容：
基础公示：展示 “入库信息”“报废信息”；
定期统计公示：每月 / 每季度发布 “物资领用的金额、领用数量、统计数据”。
展示规则：系统里的公告栏，要按 “信息类别”“所属部门” 来分类展示这些内容；
时效规则：这些公示内容会保留展示 30 天；
附加功能：可以导出这些公示的历史记录。 -->
<template>
  <div class="notice-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />
    <div class="notice-manage-container">
      <div class="flex justify-between items-center mb-4">
        <div class="page-title">
          低值易耗品公示管理（默认公示有效期30天，细节可查看
          <span
            v-if="$hasPermission('SUPPLY:EXPORT')"
            class="text-indigo-600 hover:text-indigo-800 hover:underline cursor-pointer inline-flex items-center ml-1"
            @click="goToInOutRecords"
          >
            出入库记录
          </span>
          ）
        </div>
        <div>
          <button
            class="px-4 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg"
            @click="$router.go(-1)"
          >
            返回
          </button>
        </div>
      </div>
      <!-- 查询筛选区 -->
      <el-card shadow="hover" class="search-card">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="公示标题">
            <el-input
              v-model="searchForm.title"
              placeholder="请输入公示标题模糊搜索"
              clearable
              style="width: 220px"
            />
          </el-form-item>
          <el-form-item label="公示类型">
            <el-select
              v-model="searchForm.noticeType"
              placeholder="请选择公示类型"
              clearable
              style="width: 180px"
            >
              <el-option label="入库公示" value="IN" />
              <el-option label="报废公示" value="SCRAP" />
              <el-option label="领用统计公示" value="STAT" />
            </el-select>
          </el-form-item>
          <el-form-item label="公示状态">
            <el-select
              v-model="searchForm.status"
              placeholder="请选择公示状态"
              clearable
              style="width: 180px"
            >
              <el-option label="待审批" value="UNAPPROVED" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已过期" value="EXPIRED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              icon="el-icon-search"
              @click="getNoticeList()"
              >查询</el-button
            >
            <el-button icon="el-icon-refresh" @click="resetSearch()"
              >重置</el-button
            >
          </el-form-item>
        </el-form>
      </el-card>

      <!-- 功能按钮区  -->
      <div class="btn-area">
        <el-button type="info" icon="el-icon-time" @click="openCreateDialog"
          >创建领用统计公示</el-button
        >
        <el-button
          type="warning"
          icon="el-icon-time"
          @click="delAllExpiredNotice()"
          >一键删除过期公示</el-button
        >
        <el-button
          type="danger"
          icon="el-icon-delete"
          @click="batchDelNotice()"
          :disabled="!selectIds.length"
          style="margin-left: 10px"
          >批量删除公示</el-button
        >
      </div>

      <!-- 公示列表 -->
      <el-card shadow="hover" class="table-card">
        <el-table
          :data="noticeList"
          border
          stripe
          v-loading="loading"
          element-loading-text="加载中..."
          @selection-change="handleSelectionChange"
          highlight-current-row
          style="width: 100%"
        >
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column prop="title" label="公示标题" min-width="150" />
          <el-table-column
            prop="noticeType"
            label="公示类型"
            width="120"
            align="center"
          >
            <template slot-scope="scope">
              <el-tag v-if="scope.row.noticeType === 'IN'" type="success"
                >入库公示</el-tag
              >
              <el-tag
                v-else-if="scope.row.noticeType === 'SCRAP'"
                type="warning"
                >报废公示</el-tag
              >
              <el-tag v-else type="primary">领用统计公示</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            label="公示状态"
            width="110"
            align="center"
          >
            <template slot-scope="scope">
              <el-tag v-if="scope.row.status === 'UNAPPROVED'" type="info"
                >待审批</el-tag
              >
              <el-tag v-else-if="scope.row.status === 'APPROVED'" type="success"
                >已批准</el-tag
              >
              <el-tag v-else type="danger">已过期</el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="expireTime"
            label="过期时间"
            width="180"
            align="center"
          >
            <template slot-scope="scope">
              <span :class="isExpired(scope.row.expireTime) ? 'text-red' : ''">
                {{ formatDateTime(scope.row.expireTime) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column
            prop="creatorName"
            label="发布人"
            width="120"
            align="center"
          />
          <el-table-column
            label="操作"
            width="400"
            align="center"
            fixed="right"
          >
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="primary"
                icon="el-icon-view"
                @click="showNoticeDetail(scope.row)"
                >详情</el-button
              >
              <el-button
                size="mini"
                type="success"
                icon="el-icon-check"
                @click="approveNotice(scope.row)"
                :disabled="
                  scope.row.status !== 'UNAPPROVED' ||
                  isExpired(scope.row.expireTime)
                "
              >
                批准公示
              </el-button>
              <el-button
                size="mini"
                type="danger"
                icon="el-icon-delete"
                @click="delNotice(scope.row.id)"
                >删除</el-button
              >
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[10, 20, 30, 50]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          class="pagination"
          background
        >
        </el-pagination>
      </el-card>

      <!-- 易耗品公示详情弹窗 -->
      <el-dialog
        title="公示详情"
        :visible.sync="detailDialogVisible"
        width="85%"
        top="5vh"
        close-on-click-modal
      >
        <el-descriptions :column="6" border style="font-size: 13px">
          <!-- 第一行：公示标题(占3栏) + 公示类型(占1栏) + 公示状态(占2栏) → 3+1+2=6 -->
          <el-descriptions-item label="公示标题" :span="3">
            {{ detailNotice.title }}
          </el-descriptions-item>
          <el-descriptions-item label="公示类型" :span="1">
            <span v-if="detailNotice.noticeType === 'IN'">入库公示</span>
            <span v-else-if="detailNotice.noticeType === 'SCRAP'"
              >报废公示</span
            >
            <span v-else>领用统计公示</span>
          </el-descriptions-item>
          <el-descriptions-item label="公示状态" :span="2">
            <span v-if="detailNotice.status === 'UNAPPROVED'" class="text-info"
              >待批准公示</span
            >
            <span
              v-else-if="detailNotice.status === 'APPROVED'"
              class="text-success"
              >公示中</span
            >
            <span v-else class="text-red">已过期</span>
          </el-descriptions-item>

          <!-- 第二行：所属部门(占2栏) + 开始时间(占1栏) + 公示截止时间(占2栏) + 发布人(占1栏) → 2+1+2+1=6 -->
          <el-descriptions-item label="所属部门" :span="2">
            {{ detailNotice.deptName || "无" }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间" :span="1">
            {{ formatDateTime(detailNotice.noticeTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="过期时间" :span="2">
            <span :class="isExpired(detailNotice.expireTime) ? 'text-red' : ''">
              {{ formatDateTime(detailNotice.expireTime) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="发布人" :span="1">
            {{ detailNotice.creatorName }}
          </el-descriptions-item>

          <el-descriptions-item label="公示内容" :span="6" label-align="top">
            <div
              v-html="formatContent(detailNotice.content)"
              class="detail-content"
            ></div>
          </el-descriptions-item>
        </el-descriptions>
        <div slot="footer" class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </el-dialog>
    </div>
    <!-- 在模板末尾添加子组件 -->
    <create-stat-notice
      :visible.sync="showCreateDialog"
      :login_user="login_user"
      :departments="departmentList"
      @created="onStatNoticeCreated"
    />
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import {
  getValidNotices,
  approveNotice,
  batchDeleteNotices,
  deleteNotice,
  getAllDepartments,
  deleteExpiredNotices,
} from "@/api/consumnotice";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import CreateStatNotice from "@/components/consumable/CreateStatNotice.vue";
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "consumNoticeManage",
  components: {
    HeaderComponent,
    FooterComponent,
    CreateStatNotice, // 注册新组件
  },
  data() {
    return {
      loading: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchForm: {
        title: "",
        noticeType: "",
        status: "",
      },
      noticeList: [],
      selectIds: [],
      detailDialogVisible: false,
      detailNotice: {},
      login_user: null,
      userRoles: [],
      departmentList: [], // 存储部门列表
      showCreateDialog: false, // 控制创建领用公示对话框显示
    };
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
    this.getNoticeList();
    this.loadDepartmentList();
  },
  methods: {
    async loadDepartmentList() {
      try {
        const res = await getAllDepartments();
        if (res.code == 200) {
          this.departmentList = res.data || [];
        } else {
          this.$message.error(res.msg || "获取部门列表失败");
        }
      } catch (error) {
        console.error("获取部门列表失败:", error);
        this.$message.error("获取部门列表失败");
      }
    },
    // ========== 查询有效公示列表 ==========
    async getNoticeList() {
      this.loading = true;
      try {
        // 拼接分页+筛选参数，完全适配你的接口入参规范
        const params = {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          ...this.searchForm,
        };
        const res = await getValidNotices(params);
        if (res.code == 200) {
          this.noticeList = res.data;
          this.total = res.data.length;
        } else {
          this.$message.error(res.msg || "查询公示列表失败");
        }
      } catch (err) {
        this.$message.error("网络异常，查询公示列表失败");
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    // ==========查看公示详情=========
    async showNoticeDetail(row) {
      this.loading = true;
      try {
        this.detailNotice = row;
        this.detailDialogVisible = true;
      } catch (err) {
        console.error(err);
      } finally {
        this.loading = false;
      }
    },

    // 批准公示 建议设置标题
    async approveNotice(row) {
      // 弹出对话框让用户输入新标题
      const { value: newTitle } = await this.$prompt("请输入公示标题", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        inputValue: row.title, // 默认显示当前标题
        inputPlaceholder: "请输入新的公示标题",
        inputValidator: (value) => {
          if (!value) {
            return "标题不能为空";
          }
          if (value.trim().length > 100) {
            // 假设标题最大长度为100字符
            return "标题长度不能超过100个字符";
          }
          return true;
        },
        inputErrorMessage: "标题格式不正确",
      }).catch(() => {
        this.$message.info("已取消修改标题");
        return { value: null };
      });

      // 如果用户取消了操作
      if (newTitle === null) {
        return;
      }

      this.$confirm(`确定要批准该公示吗？标题为：${newTitle}`, "温馨提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(async () => {
          try {
            // 调用API批准公示并同时更新标题
            const res = await approveNotice({
              id: row.id,
              title: newTitle,
            });
            if (res.code == 200) {
              this.$message.success("公示批准成功！");
              this.getNoticeList(); // 刷新列表
            } else {
              this.$message.error(res.msg || "公示批准失败");
            }
          } catch (err) {
            this.$message.error("网络异常，公示批准失败");
            console.error(err);
          }
        })
        .catch(() => {
          this.$message.info("已取消批准");
        });
    },

    // 删除单条公示 ==========
    async delNotice(id) {
      this.$confirm("确定要删除该公示吗？删除后不可恢复！", "温馨提示", {
        confirmButtonText: "确定删除",
        cancelButtonText: "取消",
        type: "danger",
      })
        .then(async () => {
          try {
            const res = await deleteNotice(id);
            if (res.code == 200) {
              this.$message.success("公示删除成功！");
              this.getNoticeList();
            } else {
              this.$message.error(res.msg || "公示删除失败");
            }
          } catch (err) {
            this.$message.error("网络异常，公示删除失败");
            console.error(err);
          }
        })
        .catch(() => {
          this.$message.info("已取消删除");
        });
    },

    // ========== 批量删除公示 ========
    async batchDelNotice() {
      this.$confirm(
        `确定要删除选中的【${this.selectIds.length}条】公示吗？删除后不可恢复！`,
        "温馨提示",
        {
          confirmButtonText: "确定删除",
          cancelButtonText: "取消",
          type: "danger",
        }
      )
        .then(async () => {
          try {
            const res = await batchDeleteNotices(this.selectIds);
            if (res.code == 200) {
              this.$message.success("批量删除公示成功！");
              this.getNoticeList();
              this.selectIds = [];
            } else {
              this.$message.error(res.msg || "批量删除公示失败");
            }
          } catch (err) {
            this.$message.error("网络异常，批量删除公示失败");
            console.error(err);
          }
        })
        .catch(() => {
          this.$message.info("已取消批量删除");
        });
    },
    // 跳转到出入库记录页面
    goToInOutRecords() {
      if (this.$route.name !== "InOutRecords") {
        this.$router.push({ name: "InOutRecords" });
      }
    },
    // ==========一键删除所有过期公示 ==========
    async delAllExpiredNotice() {
      this.$confirm(
        "确定要一键删除所有过期的公示吗？删除后不可恢复！",
        "温馨提示",
        {
          confirmButtonText: "确定删除",
          cancelButtonText: "取消",
          type: "warning",
        }
      )
        .then(async () => {
          try {
            const res = await deleteExpiredNotices();
            if (res.code == 200) {
              this.$message.success(res.data);
              this.getNoticeList(); // 刷新列表
            } else {
              this.$message.error("删除过期公示失败");
            }
          } catch (err) {
            this.$message.error("网络异常，删除过期公示失败");
            console.error(err);
          }
        })
        .catch(() => {
          this.$message.info("已取消删除过期公示");
        });
    },

    // ========== 表格多选事件 ==========
    handleSelectionChange(val) {
      this.selectIds = val.map((item) => item.id);
    },

    // ========== 分页相关 ==========
    handleSizeChange(val) {
      this.pageSize = val;
      this.pageNum = 1;
      this.getNoticeList();
    },
    handleCurrentChange(val) {
      this.pageNum = val;
      this.getNoticeList();
    },
    openCreateDialog() {
      this.showCreateDialog = true;
    },

    onStatNoticeCreated() {
      // 创建成功后刷新列表
      this.getNoticeList();
      this.showCreateDialog = false;
    },

    // ========== 重置查询条件 ==========
    resetSearch() {
      this.searchForm = { title: "", noticeType: "", status: "" };
      this.pageNum = 1;
      this.getNoticeList();
    },

    // ========== 工具方法：判断公示是否过期 ==========
    isExpired(expireTime) {
      if (!expireTime) return false;
      return new Date().getTime() > new Date(expireTime).getTime();
    },
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "consumNoticeManage") {
        return; // 如果已经在公示管理页面，不进行跳转
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
    // 辅助方法
    formatDateTime(dateTime) {
      if (!dateTime) return "-";
      try {
        const date = new Date(dateTime);
        return date
          .toLocaleString("zh-CN", {
            year: "numeric",
            month: "2-digit",
            day: "2-digit",
            hour: "2-digit",
            minute: "2-digit",
          })
          .replace(/\//g, "-");
      } catch (e) {
        return dateTime;
      }
    },
    formatContent(content) {
      // 调试日志：先看拿到的content是什么
      if (!content) return "<div style='padding:10px;'>无公示内容</div>";

      try {
        // 【强制处理转义符】防止后端返回的转义不完整
        const cleanContent = content.replace(/\\/g, ""); // 先去掉所有反斜杠

        // 解析JSON
        const contentObj = JSON.parse(cleanContent);
        console.log("解析后的JSON对象:", contentObj);

        // 递归渲染JSON对象为表格
        let html = `<div style="width:100%;">`;
        html += this.renderObjectAsTable(contentObj);

        return html;
      } catch (e) {
        // 解析失败时，打印错误原因（方便排查）
        console.error("JSON解析失败:", e);
        // 直接返回原内容+错误提示
        return `<div style="padding:10px;color:red;">内容解析失败：${e.message}<br>原始内容：${content}</div>`;
      }
    },

    // 将对象渲染为表格
    renderObjectAsTable(obj) {
      let tableHtml =
        '<table width="100%" cellpadding="8" cellspacing="0" style="border-collapse: collapse; border: 1px solid #ddd; margin-top: 10px;">';

      // 遍历对象属性
      for (const key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
          const value = obj[key];

          // 如果值是对象，递归处理
          if (
            typeof value === "object" &&
            value !== null &&
            !Array.isArray(value)
          ) {
            tableHtml += `
          <tr>
            <td style="border: 1px solid #ddd; padding: 8px; background-color: #f5f5f5; font-weight: bold; width: 25%;">
              ${key}
            </td>
            <td style="border: 1px solid #ddd; padding: 8px; width: 75%;">
              ${this.renderObjectAsTable(value)}
            </td>
          </tr>
        `;
          }
          // 如果值是数组，特殊处理
          else if (Array.isArray(value)) {
            tableHtml += `
          <tr>
            <td style="border: 1px solid #ddd; padding: 8px; background-color: #f5f5f5; font-weight: bold; width: 25%;">
              ${key}
            </td>
            <td style="border: 1px solid #ddd; padding: 8px; width: 75%;">
        `;

            if (value.length > 0) {
              // 如果数组元素是对象，渲染为表格
              if (typeof value[0] === "object" && value[0] !== null) {
                tableHtml +=
                  '<table width="100%" cellpadding="6" cellspacing="0" style="border-collapse: collapse; border: 1px solid #ccc; margin-top: 5px;">';

                // 生成表头
                tableHtml += '<thead><tr style="background-color: #f9f9f9;">';
                if (value.length > 0) {
                  Object.keys(value[0]).forEach((k) => {
                    tableHtml += `<th style="border: 1px solid #ccc; padding: 6px; text-align: left;">${k}</th>`;
                  });
                }
                tableHtml += "</tr></thead>";

                // 生成表体
                tableHtml += "<tbody>";
                value.forEach((item) => {
                  tableHtml += "<tr>";
                  Object.values(item).forEach((v) => {
                    tableHtml += `<td style="border: 1px solid #ccc; padding: 6px;">${v}</td>`;
                  });
                  tableHtml += "</tr>";
                });
                tableHtml += "</tbody>";
                tableHtml += "</table>";
              } else {
                // 简单数组，直接显示
                tableHtml += value.join(", ");
              }
            } else {
              tableHtml += "无数据";
            }

            tableHtml += "</td></tr>";
          }
          // 普通值
          else {
            tableHtml += `
          <tr>
            <td style="border: 1px solid #ddd; padding: 8px; background-color: #f5f5f5; font-weight: bold; width: 25%;">
              ${key}
            </td>
            <td style="border: 1px solid #ddd; padding: 8px; width: 75%;">
              ${value}
            </td>
          </tr>
        `;
          }
        }
      }

      tableHtml += "</table>";
      return tableHtml;
    },
  },
};
</script>

<style scoped>
.notice-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.notice-manage-container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}
.search-card {
  margin-bottom: 15px;
}
.search-form {
  padding: 5px 0;
}
.btn-area {
  margin-bottom: 15px;
  text-align: right;
}
.table-card {
  padding-bottom: 10px;
}
.pagination {
  margin-top: 20px;
  text-align: right;
}
.text-red {
  color: #f56c6c !important;
}
.text-success {
  color: #67c23a !important;
}
.text-info {
  color: #909399 !important;
}
.detail-content {
  line-height: 1.8;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  min-height: 100px;
}
</style>
