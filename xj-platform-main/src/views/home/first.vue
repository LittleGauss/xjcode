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
      <div class="top-quick-access">
        <a
          href="#"
          class="quick-link"
          v-if="
            $hasPermission([
              'SUPPLY:APPROVE_DEPT',
              'SUPPLY:APPROVE_IT',
              'SUPPLY:APPROVE_QUALITY',
              'SUPPLY:APPROVE_FINAL',
              'LEAVE:APPROVE_HALF_DAY',
              'LEAVE:APPROVE_3DAY',
              'LEAVE:APPROVE_LONG',
              'LEAVE:APPROVE_LEADER',
              'CONTRACT:LEGAL_REVIEW',
              'SUPERVISION:MANAGE',
              'SUPERVISION:INSPECT',
            ])
          "
          @click.prevent="navigateTo('PeningTask')"
          >待我审批/处理</a
        >
        <a
          href="#"
          class="quick-link"
          v-if="$hasPermission('LEAVE:VIEW_ALL')"
          @click.prevent="navigateTo('UserExamination')"
          >审核新用户</a
        >
      </div>
      <!-- 快捷入口和通知区域 -->
      <div class="quick-access-section">
        <ul class="quick-access">
          <h3>最新通知</h3>
          <ul class="notice-list" v-if="homeNews.length > 0">
            <li class="notice-item" v-for="news in homeNews" :key="news.id">
              <span class="notice-title clickable" @click="viewNews(news)">{{
                news.title
              }}</span>
              <span class="notice-time">
                {{ formatDate(news.createTime) }}
              </span>
            </li>
          </ul>
          <ul class="notice-list" v-else>
            暂无通知信息
          </ul>
        </ul>

        <div class="recent-notices">
          <h3>最新公示</h3>
          <ul class="notice-list" v-if="notices.length > 0">
            <li class="notice-item" v-for="notice in notices" :key="notice.id">
              <span class="notice-title clickable" @click="viewNotice(notice)"
                >[行政公示]{{ notice.title }}</span
              >
              <span class="notice-time">{{
                formatDate(notice.createdAt)
              }}</span>
            </li>
          </ul>
          <ul class="notice-list" v-if="consumnotices.length > 0">
            <li
              class="notice-item"
              v-for="consumnotice in consumnotices"
              :key="consumnotice.id"
            >
              <span
                class="notice-title clickable"
                @click="showNoticeDetail(consumnotice)"
                >[易耗品公示]{{ consumnotice.title }}</span
              >
              <span class="notice-time">{{
                formatDate(consumnotice.noticeTime)
              }}</span>
            </li>
          </ul>
          <div
            class="no-notice"
            v-if="notices.length == 0 && consumnotices.length == 0"
          >
            暂无公示信息
          </div>
        </div>
      </div>
      <!-- 业务功能网格 -->
      <div class="business-grid">
        <!-- 请销假 -->
        <div
          class="business-card"
          @click="navigateTo('leave')"
          v-if="$hasPermission('LEAVE:APPLY')"
        >
          <div class="card-icon leave-icon">
            <i class="icon">📝</i>
          </div>
          <h3>请销假管理</h3>
          <p>请假申请、审批、销假一站式服务</p>
          <div class="card-badge" v-if="pendingLeaveCount > 0">
            {{ pendingLeaveCount }} 待处理
          </div>
        </div>

        <!-- 低值易耗品管理 -->
        <div
          class="business-card"
          v-if="$hasPermission('SUPPLY:APPLY')"
          @click="navigateTo('supplies')"
        >
          <div class="card-icon supplies-icon">
            <i class="icon">📦</i>
          </div>
          <h3>低值易耗品管理</h3>
          <p>办公用品申领、库存管理</p>
        </div>

        <!-- 行政公示 -->
        <div
          class="business-card"
          v-if="$hasPermission('NOTICE:VIEW')"
          @click="navigateTo('notice')"
        >
          <div class="card-icon notice-icon">
            <i class="icon">📢</i>
          </div>
          <h3>行政公示</h3>
          <p>中心公告、规章制度发布</p>
        </div>

        <!-- 代办事项
        <div class="business-card" @click="navigateTo('todo')">
          <div class="card-icon todo-icon">
            <i class="icon">✅</i>
          </div>
          <h3>代办事项</h3>
          <p>待办任务、审批事项管理</p>
          <div class="card-badge" v-if="todoCount > 0">
            {{ todoCount }} 待办
          </div>
        </div> -->

        <!-- 公车管理 -->
        <div
          class="business-card"
          v-if="$hasPermission('VEHICLE:MANAGE')"
          @click="navigateTo('vehicle')"
        >
          <div class="card-icon vehicle-icon">
            <i class="icon">🚗</i>
          </div>
          <h3>公车管理</h3>
          <p>车辆预约、使用记录管理</p>
        </div>

        <!-- 日常监督检查 -->
        <div
          class="business-card"
          v-if="$hasPermission('NOTICE:SUPERVISE')"
          @click="navigateTo('dailySupervision')"
        >
          <div class="card-icon inspection-icon">
            <i class="icon">🔍</i>
          </div>
          <h3>日常监督检查</h3>
          <p>工作检查、问题整改跟踪</p>
        </div>

        <!-- 法律合同 -->
        <div
          class="business-card"
          v-if="
            $hasPermission([
              'CONTRACT:UPLOAD',
              'CONTRACT:VIEW_DEPT',
              'CONTRACT:VIEW_ALL',
            ])
          "
          @click="navigateTo('contract')"
        >
          <div class="card-icon inspection-icon">
            <i class="icon">🔍</i>
          </div>
          <h3>法律合同审核</h3>
          <p>合同提交、合同审核、合同统计</p>
        </div>
      </div>
    </main>
    <!-- 公示详情模态框 -->
    <div
      v-if="showDetailModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
      style="z-index: 200"
    >
      <div
        class="bg-white rounded-lg shadow-xl w-full max-w-4xl h-5/6 fade-in flex flex-col"
      >
        <div class="flex justify-between items-center border-b px-6 py-4">
          <h3 class="text-xl font-semibold text-gray-800">
            {{ currentNotice && currentNotice.title }}
          </h3>
          <button
            class="text-gray-500 hover:text-gray-700"
            @click="closeDetailModal"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="flex-1 overflow-y-auto p-6">
          <!-- 附件信息 -->
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 mb-1"
              >附件</label
            >
            <!-- 优先展示后端或 blob 提供的临时 URL（内嵌预览） -->
            <div v-if="currentNotice && currentNotice._pdfBlobUrl">
              <div style="height: 60vh; border: 1px solid #e5e7eb">
                <PdfViewer
                  :src="currentNotice._pdfBlobUrl"
                  :title="currentNotice.title"
                  :allow-download="false"
                  @close="closeDetailModal"
                />
              </div>
            </div>
            <div
              v-else-if="
                currentNotice &&
                currentNotice.attachments &&
                currentNotice.attachments.length
              "
              class="flex items-center gap-4"
            >
              <div class="text-sm text-gray-700">
                {{ currentNotice.attachments[0].name || "未知文件" }}
              </div>
              <button
                class="px-3 py-1 bg-blue-500 text-white rounded"
                @click.prevent="openAttachment(currentNotice.attachments[0])"
              >
                查看附件
              </button>
            </div>
          </div>

          <!-- 公示内容 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"
              >公示内容</label
            >
            <div class="bg-white border rounded-lg p-4 min-h-[200px]">
              <p class="text-gray-900 whitespace-pre-wrap">
                {{ currentNotice && currentNotice.content }}
              </p>
            </div>
          </div>

          <!-- 有效期 -->
          <div class="mt-6 p-4 bg-blue-50 rounded-lg">
            <label class="block text-sm font-medium text-blue-700 mb-2"
              >有效期</label
            >
            <p class="text-blue-900">
              {{
                formatDateTimeRange(
                  currentNotice && currentNotice.effectiveDate,
                  currentNotice && currentNotice.expireDate
                )
              }}
            </p>
          </div>
        </div>

        <div class="flex justify-between items-center border-t px-6 py-4">
          <div class="text-gray-500 text-sm">
            创建时间:
            {{ formatDateTime(currentNotice && currentNotice.createdAt) }}
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="新闻详情"
      width="60%"
      :close-on-click-modal="true"
    >
      <div>
        <h3>{{ selectedNews.title }}</h3>
        <p><strong>发布时间:</strong> {{ selectedNews.createTime }}</p>
        <div v-html="selectedNews.content"></div>
      </div>
      <span slot="footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>

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
          <span v-else-if="detailNotice.noticeType === 'SCRAP'">报废公示</span>
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
          <span v-else class="text-danger">已过期</span>
        </el-descriptions-item>

        <!-- 第二行：所属部门(占2栏) + 开始时间(占1栏) + 公示截止时间(占2栏) + 发布人(占1栏) → 2+1+2+1=6 -->
        <el-descriptions-item label="所属部门" :span="2">
          {{ detailNotice.deptName || "无" }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间" :span="1">
          {{ formatDateTime(detailNotice.noticeTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="公示截止时间" :span="2">
          {{ formatDateTime(detailNotice.expireTime) }}
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
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";
import { leaveApi } from "@/api/leave";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import { validNewnotices } from "@/api/consumnotice";
import { noticeApi } from "@/api/notice";
import { getHomePageNews } from "@/api/news";

export default {
  name: "OAHomepage",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      pendingLeaveCount: 2,
      todoCount: 5,
      login_user: null,
      userRoles: [],
      userPermissions: [],
      notices: [],
      consumnotices: [],
      showDetailModal: false,
      homeNews: [],
      dialogVisible: false,
      selectedNews: {},
      detailDialogVisible: false,
      detailNotice: {},
    };
  },
  created() {
    // 在组件创建后安全地获取用户信息
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
  },
  mounted() {
    console.log("first.vue: mounted()被调用");
    this.fetchNotices();
    this.fetchConsumNotices();
    this.loadHomeNews();
    this.loadTodo();
  },
  methods: {
    viewNews(news) {
      this.selectedNews = news;
      this.dialogVisible = true;
    },
    async fetchConsumNotices() {
      try {
        const response = await validNewnotices();
        if (response.code == "200") {
          this.consumnotices = response.data || [];
        }
      } catch (error) {
        console.error("获取易耗品公示失败:", error);
        this.$message.error("获取公示信息失败，请稍后重试");
      }
    },
    async fetchNotices() {
      try {
        const response = await noticeApi.getCurrentThree();
        if (response.code == "200") {
          this.notices = response.data || [];
        }
      } catch (error) {
        console.error("获取公示失败:", error);
        this.$message.error("获取公示信息失败，请稍后重试");
      }
    },
    async loadTodo() {
      try {
        const res = await leaveApi.getTodoTasks();
        this.pendingLeaveCount = res.data.length || 0;
      } catch (err) {
        this.$message.error("加载待办任务失败");
      }
    },
    async loadHomeNews() {
      try {
        const res = await getHomePageNews();
        this.homeNews = res.data;
      } catch (error) {
        console.error("获取新闻失败:", error);
      }
    },
    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return "";
      // 处理LocalDateTime格式，如：2024-09-25T14:30:00
      const date = new Date(dateString);
      return `${date.getFullYear()}-${(date.getMonth() + 1)
        .toString()
        .padStart(2, "0")}-${date.getDate().toString().padStart(2, "0")}`;
    },
    navigateTo(module) {
      // 这里可以添加路由跳转逻辑
      switch (module) {
        case "leave":
          this.$router.push("/leave");
          break;
        case "supplies":
          this.$router.push("/consumable");
          break;
        case "notice":
          this.$router.push("/notice");
          break;
        case "todo":
          this.$router.push("/todo-list"); // 这里要单独搞个待办的页面，把所有待办都集中起来才好
          break;
        case "vehicle":
          this.$router.push("/vehicle");
          break;
        case "inspection": //日常监督检查
          this.$router.push("/supervision");
          break;
        case "dailySupervision": //日常监督检查新
          this.$router.push("/inspection").catch((err) => {
            if (err.name != "NavigationDuplicated") throw err;
          });
          break;
        case "contract":
          this.$router.push("/contract");
          break;
        case "system": // 系统设置跳转逻辑
          this.$router.push("/user");
          break;
        case "UserExamination": // 用户审核跳转逻辑
          this.$router.push("/UserExamination");
          break;
        case "LeaveTodo": // 待办审批页面
          this.$router.push("/leave/todo");
          break;
        case "PeningTask": // 全部工作流待办页面
          this.$router.push("/PeningTask");
          break;
        default:
          this.$router.push("/home-first");
      }
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
      if (routeName == "home-first") {
        return;
      }
      this.$router.push({ name: routeName });
    },

    isAdmin() {
      return (
        this.userRoles &&
        this.userRoles.some((role) => role.code === "SUPER_ADMIN")
      );
    },
    isAdminOffice() {
      return (
        this.userRoles &&
        this.userRoles.some((role) => role.code === "ROLE_ADMIN_OFFICE")
      );
    },
    // 查看公示详情
    async viewNotice(notice) {
      try {
        // 获取最新数据
        const latestNotice = await this.fetchNoticeById(notice.id);
        this.currentNotice = latestNotice || notice;
        // 标记已读（把当前用户 id 传给后端）
        try {
          const userId = this.login_user && this.login_user.id;
          if (userId) {
            await noticeApi.markRead(this.currentNotice.id, userId);
            // 刷新已读数（使用 $set 保证 Vue2 响应式生效）
            try {
              const rc = await noticeApi.getReadCount(this.currentNotice.id);
              console.debug("getReadCount response:", rc);
              if (rc && (rc.code === 200 || rc.code === "200") && rc.data) {
                this.$set(
                  this.currentNotice,
                  "readCount",
                  rc.data.readCount || 0
                );
              } else {
                this.$set(this.currentNotice, "readCount", 0);
              }
            } catch (e) {
              console.warn("获取已读数失败", e);
              this.$set(this.currentNotice, "readCount", 0);
            }
          }
        } catch (err) {
          console.warn("标记已读或获取已读数失败", err);
        }
        // 如果详情包含附件的 previewPages，赋值以便详情显示
        if (
          this.currentNotice &&
          this.currentNotice.attachments &&
          this.currentNotice.attachments.length
        ) {
          const first = this.currentNotice.attachments[0];
          if (first.previewPages && first.previewPages.length) {
            this.pdfPages = [...first.previewPages];
            this.pdfFileName = first.name || "";
            // clear any existing blob url
            if (this.currentNotice._pdfBlobUrl) {
              try {
                URL.revokeObjectURL(this.currentNotice._pdfBlobUrl);
              } catch (e) {
                // 忽略撤销错误，但记录以便调试
                console.warn("revokeObjectURL failed", e);
              }
              delete this.currentNotice._pdfBlobUrl;
            }
          } else {
            this.pdfPages = [];
            this.pdfFileName = "";
            // 如果 attachment 中包含后端返回的 fileUrl（MinIO 临时 URL），直接使用它
            if (first.fileUrl) {
              try {
                // 直接指向临时 URL 以便在详情里以 iframe/embed 打开或在新窗口打开
                this.$set(this.currentNotice, "_pdfBlobUrl", first.fileUrl);
                this.pdfFileName = first.name || "";
              } catch (err) {
                console.warn("设置 fileUrl 失败", err);
              }
            } else {
              // attempt to fetch original uploaded base64 from  and create blob url for embedding
              try {
                const attachId = first.id || first.uploadId || first.fileId;
                if (attachId) {
                  const getRes = await noticeApi.getUploadById(attachId);
                  if (
                    getRes &&
                    getRes.code === 200 &&
                    getRes.data &&
                    getRes.data.base64
                  ) {
                    try {
                      const base64 = getRes.data.base64;
                      const byteString = atob(base64.split(",").pop());
                      const ab = new ArrayBuffer(byteString.length);
                      const ia = new Uint8Array(ab);
                      for (let i = 0; i < byteString.length; i++)
                        ia[i] = byteString.charCodeAt(i);
                      const blob = new Blob([ab], { type: "application/pdf" });
                      const blobUrl = URL.createObjectURL(blob);
                      this.$set(this.currentNotice, "_pdfBlobUrl", blobUrl);
                    } catch (err) {
                      console.warn("生成 PDF Blob 失败", err);
                    }
                  }
                }
              } catch (err) {
                // 获取上传文件失败，记录错误用于调试
                console.warn("获取上传文件失败", err);
              }
            }
          }
        } else {
          this.pdfPages = [];
          this.pdfFileName = "";
        }
        this.showDetailModal = true;
      } catch (err) {
        this.currentNotice = notice;
        this.pdfPages = [];
        this.pdfFileName = "";
        this.showDetailModal = true;
      }
    },

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

    closeDetailModal() {
      this.showDetailModal = false;
      // revoke any blob URL we created
      if (this.currentNotice && this.currentNotice._pdfBlobUrl) {
        try {
          URL.revokeObjectURL(this.currentNotice._pdfBlobUrl);
        } catch (e) {
          // 记录撤销错误以便排查
          console.warn("revokeObjectURL failed", e);
        }
      }
      this.currentNotice = null;
    },
    async fetchNoticeById(id) {
      try {
        const res = await noticeApi.getNoticeById(id);
        if (res && (res.code === 200 || res.code === "200")) {
          return res.data;
        } else {
          console.error("获取公示详情失败:", res && res.message);
          return null;
        }
      } catch (err) {
        console.error("获取公示详情失败:", err);
        return null;
      }
    },
    formatDateTimeRange(start, end) {
      if (!start || !end) return "";
      const s = new Date(start);
      const e = new Date(end);
      const format = (d) => d.toLocaleDateString("zh-CN");
      return `${format(s)} 至 ${format(e)}`;
    },
    formatDateTime(dateTimeString) {
      if (!dateTimeString) return "";
      const date = new Date(dateTimeString);
      return date.toLocaleString("zh-CN");
    },
    formatContent(content) {
      // 调试日志：先看拿到的content是什么
      if (!content) return "<div style='padding:10px;'>无公示内容</div>";

      try {
        // 【强制处理转义符】防止后端返回的转义不完整
        const cleanContent = content.replace(/\\/g, ""); // 先去掉所有反斜杠
        console.log("清理转义后的content:", cleanContent);

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

/* 业务网格样式 */
.business-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 50px;
}

.business-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  border: 1px solid #eaeaea;
}

.business-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.card-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  font-size: 24px;
}
/* 系统设置入口样式 */
.system-setting {
  color: rgb(226, 15, 15);
  cursor: pointer;
  margin-left: 20px;
  font-weight: bold;
}

.system-setting:hover {
  text-decoration: underline;
}
.leave-icon {
  background: #e3f2fd;
}
.supplies-icon {
  background: #f3e5f5;
}
.notice-icon {
  background: #e8f5e8;
}
.todo-icon {
  background: #fff3e0;
}
.vehicle-icon {
  background: #e0f2f1;
}
.inspection-icon {
  background: #fce4ec;
}
.more-icon {
  background: #f5f5f5;
}

.business-card h3 {
  font-size: 1.3rem;
  color: #333;
  margin-bottom: 8px;
}

.business-card p {
  color: #666;
  line-height: 1.5;
}

.card-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  background: #ff4757;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.8rem;
}

/* 快捷入口区域 */
.quick-access-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
  margin-bottom: 40px; /* 添加这行 */
  margin-top: 10px;
}

.quick-access,
.recent-notices {
  background: white;
  padding: 24px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-access h3,
.recent-notices h3 {
  margin-bottom: 20px;
  color: #333;
  border-bottom: 2px solid #667eea;
  padding-bottom: 8px;
}

.quick-links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.quick-link {
  background: #f8f9fa;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  color: #333333;
  transition: all 0.3s;
}

.quick-link:hover {
  background: #667eea;
  color: white;
}

.notice-list {
  list-style: none;
  padding: 0;
}

.notice-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.notice-item:last-child {
  border-bottom: none;
}

.notice-title {
  color: #333;
  flex: 1;
}

.notice-time {
  color: #999;
  font-size: 0.9rem;
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
/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    height: auto;
    padding: 15px 0;
  }

  .nav-menu {
    margin: 15px 0;
  }

  .business-grid {
    grid-template-columns: 1fr;
  }

  .quick-access-section {
    grid-template-columns: 1fr;
  }

  .top-quick-access {
    border-radius: 8px;
    max-width: 1100px;
    margin: 0 auto;
    display: flex;
    gap: 20px; /* 增加间距 */
    padding: 12px 0; /* 调整内边距 */
    align-items: center;
  }

  .quick-links-container {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }

  .quick-link {
    background: #f8f9fa;
    border: none; /* 移除边框 */
    padding: 4px 8px; /* 减小内边距 */
    text-decoration: underline; /* 添加下划线 */
    color: #667eea; /* 设置文字颜色 */
    font-size: 8px; /* 设置小字号 */
    transition: all 0.3s;
  }

  .quick-link:hover {
    color: #333; /* 悬停时文字变深 */
    text-decoration: underline; /* 保持下划线 */
    background: transparent;
  }
  .content-table {
    width: 100%;
    border-collapse: collapse;
  }
  .content-table td {
    border: 1px solid #e6e6e6;
  }
}
</style>
