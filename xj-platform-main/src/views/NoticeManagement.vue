<template>
  <div class="home-container">
    <!-- 使用通用页头 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="container mx-auto px-4 py-8 flex-1 overflow-y-auto">
      <!-- 头部标题 -->
      <div class="flex justify-between items-center mb-8">
        <h1 class="text-3xl font-bold text-gray-800">行政公示</h1>
        <div class="flex items-center space-x-3">
          <el-button
            class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
            v-if="$hasPermission('NOTICE:SUPERVISE')"
            @click="$router.push({ name: 'Ducha' })"
          >
            <i class="fas fa-search mr-2"></i>督察
          </el-button>
          <el-button
            class="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg flex items-center"
            v-if="$hasPermission('NOTICE:MANAGE')"
            @click="openAddModal"
          >
            <i class="fas fa-plus mr-2"></i>新增公示
          </el-button>
        </div>
      </div>

      <!-- 公示列表 -->
      <div
        class="space-y-6"
        v-show="!showAddModal && !showDetailModal"
        v-if="$hasPermission('NOTICE:VIEW')"
      >
        <div
          v-for="notice in notices"
          :key="notice.id"
          class="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200 hover:shadow-lg transition-shadow duration-300"
        >
          <div class="p-6">
            <div class="flex justify-between items-start mb-2">
              <h3 class="text-xl font-semibold text-gray-800">
                {{ notice.title }}
              </h3>
              <span
                v-if="notice.status === 'active'"
                class="bg-green-100 text-green-800 text-xs font-medium px-2.5 py-0.5 rounded-full"
              >
                进行中
              </span>
              <span
                v-else
                class="bg-gray-100 text-gray-800 text-xs font-medium px-2.5 py-0.5 rounded-full"
              >
                已结束
              </span>
            </div>

            <!-- 公示内容预览 -->
            <div class="text-gray-600 mb-3 line-clamp-2">
              {{ notice.content }}
            </div>

            <div class="flex items-center text-gray-500 text-sm mb-2">
              <i class="fas fa-building mr-2"></i>
              <span>发布部门: {{ notice.department || "未指定" }}</span>
            </div>

            <div class="flex items-center text-gray-500 text-sm mb-4">
              <i class="far fa-calendar-alt mr-2"></i>
              <span>发布时间: {{ formatDate(notice.issueDate) }}</span>
            </div>

            <div class="flex items-center text-gray-500 text-sm mb-4">
              <i class="far fa-clock mr-2"></i>
              <span>{{
                formatDateTimeRange(notice.effectiveDate, notice.expireDate)
              }}</span>
            </div>

            <div class="flex justify-between items-center">
              <button
                class="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 flex items-center"
                @click="viewNotice(notice)"
              >
                <i class="far fa-eye mr-2"></i>查看详情
              </button>
              <div
                v-if="$hasPermission('NOTICE:STAT')"
                class="text-sm text-gray-600 flex items-center space-x-2"
              >
                <span>已读: {{ notice.readCount || 0 }}</span>
                <button
                  v-if="$hasPermission('NOTICE:STAT')"
                  class="text-blue-600 hover:underline"
                  @click="openReadUsersDialog(notice)"
                >
                  查看详细
                </button>
              </div>
              <div class="flex items-center space-x-2">
                <button
                  class="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-lg hover:bg-yellow-200 flex items-center h-10"
                  @click="openEditModal(notice.id)"
                >
                  <i class="fas fa-edit mr-2"></i>编辑
                </button>
                <button
                  class="px-4 py-2 bg-green-100 text-green-700 rounded-lg hover:bg-green-200 flex items-center h-10"
                  @click="exportUnreadNotice(notice)"
                >
                  <i class="fas fa-file-export mr-2"></i>导出未读
                </button>
                <button
                  v-if="notice.status === 'active'"
                  class="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 flex items-center h-10"
                  @click="revokeNotice(notice.id)"
                >
                  <i class="fas fa-ban mr-2"></i>撤销
                </button>
                <button
                  class="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 flex items-center h-10"
                  @click="handleDeleteNotice(notice.id)"
                >
                  <i class="fas fa-trash mr-2"></i>删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 新增/编辑公示模态框 -->
      <div
        v-if="showAddModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
        @click.self="closeAddModal"
      >
        <div class="bg-white rounded-lg shadow-xl w-full max-w-2xl fade-in">
          <div class="flex justify-between items-center border-b px-6 py-4">
            <h3 class="text-xl font-semibold text-gray-800">
              {{ isEditing ? "编辑公示" : "新增公示" }}
            </h3>
            <button
              class="text-gray-500 hover:text-gray-700"
              @click="closeAddModal"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="p-6 space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >公示标题</label
              >
              <input
                v-model="form.title"
                type="text"
                placeholder="请输入公示标题"
                class="w-full px-3 py-2 border rounded-lg"
              />
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >公示内容</label
              >
              <textarea
                v-model="form.content"
                placeholder="请输入公示内容"
                rows="4"
                class="w-full px-3 py-2 border rounded-lg"
              ></textarea>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >上传附件（仅支持PDF文件）文件不能超过10M</label
              >
              <input
                ref="pdfInput"
                type="file"
                accept="application/pdf"
                @change="onPdfSelected"
                class="hidden"
              />
              <!-- 上传按钮：点击时触发原生input的点击事件 -->
              <el-button size="small" type="primary" @click="triggerFileInput">
                选择 PDF 文件
              </el-button>
              <div
                v-if="pdfFileName"
                class="mt-2 flex items-center justify-between"
              >
                <div class="text-sm text-gray-700">
                  已选择: {{ pdfFileName }}
                </div>
                <button class="text-sm text-red-600" @click="clearPdfPreview">
                  清除附件
                </button>
              </div>
            </div>

            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1"
                >发布部门</label
              >
              <el-select
                v-model="form.departmentId"
                placeholder="请选择发布部门"
                clearable
                class="w-full"
              >
                <el-option
                  v-for="d in departmentList"
                  :key="d.id"
                  :label="d.name"
                  :value="d.id"
                />
              </el-select>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >发布日期</label
                >
                <input
                  v-model="form.issueDate"
                  type="date"
                  class="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >生效日期</label
                >
                <input
                  v-model="form.effectiveDate"
                  type="date"
                  class="w-full px-3 py-2 border rounded-lg"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1"
                  >失效日期</label
                >
                <input
                  v-model="form.expireDate"
                  type="date"
                  class="w-full px-3 py-2 border rounded-lg"
                />
              </div>
            </div>
          </div>
          <div class="flex justify-end border-t px-6 py-4 space-x-3">
            <button
              class="px-4 py-2 border rounded-lg text-gray-700 hover:bg-gray-100"
              @click="closeAddModal"
            >
              取消
            </button>
            <button
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
              @click="submitForm"
            >
              {{ isEditing ? "更新" : "提交" }}
            </button>
          </div>
        </div>
      </div>

      <!-- 公示详情模态框 -->
      <div
        v-if="showDetailModal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center"
        style="z-index: 200"
        @click.self="closeDetailModal"
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
                <div
                  v-if="$hasPermission('NOTICE:STAT')"
                  class="text-sm text-gray-600 flex items-center space-x-2 mt-2"
                >
                  <span
                    >已读:
                    {{ (currentNotice && currentNotice.readCount) || 0 }}</span
                  >
                  <button
                    class="text-blue-600 hover:underline"
                    @click="openReadUsersDialog(currentNotice)"
                  >
                    查看详细
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="flex justify-between items-center border-t px-6 py-4">
            <div class="text-gray-500 text-sm">
              创建时间:
              {{ formatDateTime(currentNotice && currentNotice.createdAt) }}
            </div>
            <div class="flex items-center space-x-2">
              <button
                class="px-4 py-2 bg-yellow-100 text-yellow-700 rounded-lg hover:bg-yellow-200 flex items-center h-10"
                @click="openEditModal(currentNotice.id)"
              >
                <i class="fas fa-edit mr-2"></i>编辑
              </button>
              <button
                class="px-4 py-2 bg-green-100 text-green-700 rounded-lg hover:bg-green-200 flex items-center h-10"
                v-if="$hasPermission('NOTICE:EXPORT_UNREAD')"
                @click="exportUnreadNotice(currentNotice, true)"
              >
                <i class="fas fa-file-export mr-2"></i>导出未读
              </button>
              <button
                v-if="currentNotice && currentNotice.status === 'active'"
                class="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 flex items-center h-10"
                @click="revokeNotice(currentNotice.id, true)"
              >
                <i class="fas fa-ban mr-2"></i>撤销
              </button>
              <button
                class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 flex items-center h-10"
                @click="handleDeleteNotice(currentNotice.id, true)"
              >
                <i class="fas fa-trash mr-2"></i>删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 已读人员列表弹窗 -->
      <el-dialog
        title="已读人员"
        :visible.sync="readUsersVisible"
        width="520px"
        :close-on-click-modal="true"
      >
        <div v-if="readUsers && readUsers.length" class="space-y-2">
          <div
            v-for="u in readUsers"
            :key="u.id || u.username"
            class="flex justify-between items-center px-2 py-1 border rounded"
          >
            <div class="flex items-center space-x-2">
              <span class="font-medium">{{
                u.nickname || u.username || "未知"
              }}</span>
              <span class="text-gray-500 text-sm">{{
                u.departmentName || u.department || ""
              }}</span>
            </div>
            <div class="text-gray-600 text-sm">
              {{ u.phone || u.mobile || "" }}
            </div>
          </div>
        </div>
        <div v-else class="text-gray-500">暂无已读人员</div>
        <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="readUsersVisible = false"
            >关闭</el-button
          >
        </span>
      </el-dialog>
    </div>
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { removeToken, removeUserToken, getUserToken } from "@/utils/auth";
import { noticeApi } from "@/api/notice";
import { userApi } from "@/api/usermag";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import PdfViewer from "@/views/PdfViewer.vue";
// pdfjs will be loaded at runtime from CDN to avoid build-time transpile issues

export default {
  name: "NoticeManagement",
  components: {
    HeaderComponent,
    FooterComponent,
    PdfViewer,
  },

  data() {
    return {
      fileUrl: "",
      currentPage: "notice",
      notices: [],
      showAddModal: false,
      showDetailModal: false,
      // 已读人员弹窗
      readUsersVisible: false,
      readUsers: [],
      currentNotice: null,
      form: {
        id: null,
        title: "",
        content: "",
        departmentId: null,
        department: "",
        issueDate: "",
        effectiveDate: "",
        expireDate: "",
        status: "active",
      },
      departmentList: [],
      isEditing: false,
      // PDF preview
      pdfFileName: "",
      pdfPages: [], // data URLs for rendered pages
      renderingPdf: false,
      pdfFile: null,
    };
  },
  mounted() {
    console.log("权限数据:", this.$store.getters.permissions);
  },
  created() {
    // 在组件创建后安全地获取用户信息
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
      console.log("获取到的权限数据:", this.$store.getters.permissions);
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    this.fetchNotices();
    // 加载部门列表以供发布部门下拉使用
    this.loadDepartments();
  },
  methods: {
    // 退出登录
    handleLogout() {
      // 1. 清除 Vuex 状态
      removeUserToken();
      this.login_user = null;
      removeToken();
      this.$router.push("/login");
    },
    // 处理头部/底部组件发出的导航事件
    // 支持字符串路径或对象形式的路由参数
    handleNavigate(target) {
      try {
        if (!target) return;
        // 字符串：当作路径或已注册的路由名（优先按路径）
        if (typeof target === "string") {
          if (target.startsWith("/") || target.startsWith("./")) {
            this.$router.push(target);
          } else {
            // 尝试按路由名跳转
            this.$router.push({ name: target }).catch(() => {});
          }
          return;
        }
        // 对象：直接交给 vue-router
        if (typeof target === "object") {
          this.$router.push(target).catch(() => {});
        }
      } catch (e) {
        console.warn("handleNavigate 跳转失败", e);
      }
    },
    // 触发原生文件选择器（替代直接点击input）
    triggerFileInput() {
      // 点击前先清理可能的无效状态（可选，优化体验）
      this.clearPdfPreview();
      // 触发原生input的点击事件
      this.$refs.pdfInput.click();
    },
    // 处理 PDF 上传
    async onPdfSelected(event) {
      const file = event.target.files && event.target.files[0];
      if (!file) return;
      if (file.type !== "application/pdf") {
        this.$message.error("仅支持 PDF 文件");
        return;
      }

      // 2. 新增：判断文件大小（不超过 10MB）
      const maxSize = 10 * 1024 * 1024; // 10MB 对应的字节数（10485760 B）
      if (file.size > maxSize) {
        // 提示用户文件过大，同时显示当前文件大小（优化体验）
        const fileSizeMB = (file.size / 1024 / 1024).toFixed(2); // 转换为 MB 并保留 2 位小数
        this.$message.error(
          `文件过大！当前文件大小：${fileSizeMB}MB，仅支持不超过 10MB 的 PDF 文件`
        );
        return; // 终止后续逻辑
      }

      // 只记录文件名和文件对象，不进行页面渲染预览
      this.pdfFile = file;
      this.pdfFileName = file.name;
      this.pdfPages = [];
    },

    ensurePdfJsLoaded() {
      return new Promise((resolve, reject) => {
        if (window.pdfjsLib) return resolve();
        const script = document.createElement("script");
        // use a stable UMD build from jsDelivr (pdfjs v2.x UMD)
        script.src =
          "https://cdn.jsdelivr.net/npm/pdfjs-dist@2.16.105/build/pdf.min.js";
        script.onload = () => {
          // pdfjs attaches to window.pdfjsLib
          if (window.pdfjsLib) resolve();
          else reject(new Error("pdfjs 加载失败"));
        };
        script.onerror = () => reject(new Error("pdfjs 加载失败"));
        document.head.appendChild(script);
      });
    },
    clearPdfPreview() {
      this.pdfFileName = "";
      this.pdfPages = [];
      this.pdfFile = null;
      // 清空文件输入（若需要）
      const el = this.$refs.pdfInput;
      if (el) el.value = null;
    },
    // GET /administrative-notice - 查询所有公示
    async fetchNotices() {
      try {
        const res = await noticeApi.getNoticeList();
        console.debug("公示接口响应:", res);
        if (res && (res.code === 200 || res.code === "200")) {
          // 保持后端返回数据结构，直接赋值数组或空数组
          this.notices = Array.isArray(res.data) ? res.data : [];
          // 初始化 readCount 为 0（保证 Vue2 响应式可观察到该属性）
          try {
            this.notices.forEach((nn) => this.$set(nn, "readCount", 0));
            // 仅当用户有 NOTICE:STAT 权限时，异步获取已读数（若后端未返回 readCount）
            if (this.$hasPermission && this.$hasPermission("NOTICE:STAT")) {
              for (let n of this.notices) {
                (async (notice) => {
                  try {
                    const rc = await noticeApi.getReadCount(notice.id);
                    if (rc && (rc.code == 200 || rc.code == "200") && rc.data) {
                      this.$set(notice, "readCount", rc.data.readCount || 0);
                    } else {
                      this.$set(notice, "readCount", 0);
                    }
                  } catch (e) {
                    this.$set(notice, "readCount", 0);
                  }
                })(n);
              }
            }
          } catch (e) {
            // 忽略读取已读数的错误
          }
        } else {
          this.notices = [];
          this.$message && this.$message.error
            ? this.$message.error(
                res && res.message ? res.message : "获取公示列表失败"
              )
            : console.error("获取公示列表失败:", res && res.message);
        }
      } catch (err) {
        this.notices = [];
        this.$message && this.$message.error
          ? this.$message.error("获取公示列表失败: " + (err.message || err))
          : console.error("获取公示数据失败:", err);
      }
    },

    // 加载部门列表
    async loadDepartments() {
      try {
        const res = await userApi.getDepartmentList();
        this.departmentList = Array.isArray(res.data)
          ? res.data
          : res.data || [];
      } catch (e) {
        console.error("加载部门列表失败:", e);
        this.departmentList = [];
      }
    },

    getDepartmentName(id) {
      if (!id) return null;
      const d = (this.departmentList || []).find(
        (x) => x.id === id || x.id == id
      );
      return d ? d.name : null;
    },

    // GET /administrative-notice/{id} - 查询单个公示
    async fetchNoticeById(id) {
      try {
        const res = await noticeApi.getNoticeById(id);
        if (res && (res.code == 200 || res.code == "200")) {
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

    // POST /administrative-notice - 添加/修改行政公示
    async saveNotice(noticeData) {
      try {
        const res = await noticeApi.saveNotice(noticeData);
        if (res && (res.code == 200 || res.code == "200")) {
          return res.data;
        } else {
          throw new Error((res && res.message) || "保存失败");
        }
      } catch (err) {
        console.error("保存公示失败:", err);
        throw err;
      }
    },

    // 导出未读人员为 CSV 并触发下载
    async exportUnreadNotice(notice) {
      if (!notice || !notice.id) {
        this.$message && this.$message.error
          ? this.$message.error("无效的公示，无法导出")
          : alert("无效的公示，无法导出");
        return;
      }
      const id = notice.id;
      try {
        this.$message && this.$message.info
          ? this.$message.info("正在生成导出文件，请稍候...")
          : console.info("exporting...");
        const res = await noticeApi.exportUnread(id);
        // 仅在 2xx 且 Content-Type 非 JSON 时视为下载成功
        if (
          res &&
          res.status >= 200 &&
          res.status < 300 &&
          res.data &&
          !String(
            (res.headers &&
              (res.headers["content-type"] || res.headers["Content-Type"])) ||
              ""
          )
            .toLowerCase()
            .includes("application/json")
        ) {
          const blob = res.data;
          // 优先尝试从响应头获取文件名
          let fileName = `unread_users_${id}.xlsx`;
          try {
            const disposition =
              (res.headers &&
                (res.headers["content-disposition"] ||
                  res.headers["Content-Disposition"])) ||
              "";
            if (disposition) {
              // filename*=UTF-8''encoded 或 filename="..."
              const fnMatch = disposition.match(
                /filename\*=UTF-8''([^;\n\r]+)/i
              );
              if (fnMatch && fnMatch[1]) {
                fileName = decodeURIComponent(fnMatch[1]);
              } else {
                const m = disposition.match(/filename="?([^";]+)"?/i);
                if (m && m[1]) fileName = m[1];
              }
            }
          } catch (e) {
            console.warn("解析下载文件名失败", e);
          }
          const url = URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = fileName;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
          this.$message && this.$message.success
            ? this.$message.success("导出已开始，查看浏览器下载")
            : console.info("export started");
        } else {
          // 若是 JSON 错误响应，尝试读取信息
          try {
            const ct = String(
              (res.headers &&
                (res.headers["content-type"] || res.headers["Content-Type"])) ||
                ""
            ).toLowerCase();
            if (ct.includes("application/json") && res.data) {
              const reader = new FileReader();
              const msg = await new Promise((resolve) => {
                reader.onload = () => {
                  try {
                    const obj = JSON.parse(reader.result);
                    resolve(obj.message || obj.msg || "导出失败");
                  } catch (e) {
                    resolve("导出失败");
                  }
                };
                reader.onerror = () => resolve("导出失败");
                reader.readAsText(res.data, "utf-8");
              });
              throw new Error(msg);
            }
          } catch (e) {
            // ignore
          }
          throw new Error("导出失败");
        }
      } catch (err) {
        console.error("导出未读人员失败:", err);
        this.$message && this.$message.error
          ? this.$message.error("导出失败: " + (err.message || err))
          : alert("导出失败: " + (err.message || err));
      }
    },

    // 打开已读人员弹窗（使用结构化 el-dialog 展示）
    async openReadUsersDialog(notice) {
      if (!notice || !notice.id) return;
      try {
        const res = await noticeApi.getReadUsers(notice.id);
        const users = Array.isArray(res && res.data) ? res.data : [];
        // 规范化字段以保证显示整齐
        this.readUsers = users.map((u) => ({
          id: u.id,
          username: u.username || "",
          nickname: u.nickname || "",
          departmentName: u.departmentName || u.department || "",
          phone: u.phone || u.mobile || "",
        }));
        this.readUsersVisible = true;
      } catch (e) {
        this.$message && this.$message.error
          ? this.$message.error("获取已读人员失败")
          : alert("获取已读人员失败");
      }
    },

    // DELETE /administrative-notice/{id} - 删除单个公示
    async deleteNotice(id) {
      try {
        const res = await noticeApi.deleteNotice(id);
        if (res && (res.code == 200 || res.code == "200")) {
          return true;
        } else {
          throw new Error((res && res.message) || "删除失败");
        }
      } catch (err) {
        console.error("删除公示失败:", err);
        throw err;
      }
    },

    // 日期格式化方法
    formatDate(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      return date.toLocaleDateString("zh-CN");
    },

    formatDateTime(dateTimeString) {
      if (!dateTimeString) return "";
      const date = new Date(dateTimeString);
      return date.toLocaleString("zh-CN");
    },

    formatDateTimeRange(start, end) {
      if (!start || !end) return "";
      const s = new Date(start);
      const e = new Date(end);
      const format = (d) => d.toLocaleDateString("zh-CN");
      return `${format(s)} 至 ${format(e)}`;
    },

    // 新增公示
    openAddModal() {
      this.isEditing = false;
      this.form = {
        id: null,
        title: "",
        content: "",
        department: "",
        issueDate: this.getTodayDate(),
        effectiveDate: this.getTodayDate(),
        expireDate: this.getNextMonthDate(),
        status: "active",
      };
      // 清理之前的 PDF 预览
      this.pdfFile = null;
      this.pdfFileName = "";
      this.pdfPages = [];
      this.showAddModal = true;
    },

    // 编辑公示
    async openEditModal(id) {
      try {
        const notice = await this.fetchNoticeById(id);
        if (notice) {
          this.isEditing = true;
          // 将后端数据填充到表单，便于编辑
          this.form = {
            id: notice.id,
            title: notice.title || "",
            content: notice.content || "",
            department: notice.department || "",
            issueDate: notice.issueDate || this.getTodayDate(),
            effectiveDate: notice.effectiveDate || this.getTodayDate(),
            expireDate: notice.expireDate || this.getNextMonthDate(),
            status: notice.status || "active",
          };

          // 清理临时上传预览
          this.pdfFile = null;
          this.pdfFileName = "";
          this.pdfPages = [];

          // 如果已有附件，仅显示文件名（不渲染图片预览）
          if (notice.attachments && notice.attachments.length) {
            const first = notice.attachments[0];
            this.pdfFileName = first.name || "";
          }

          // 打开新增/编辑模态
          this.showAddModal = true;
        }
      } catch (err) {
        console.error("打开编辑模态失败:", err);
        alert("无法获取公示信息，稍后重试。");
      }
    },

    closeAddModal() {
      this.showAddModal = false;
      this.form = {
        id: null,
        title: "",
        content: "",
        department: "",
        issueDate: "",
        effectiveDate: "",
        expireDate: "",
        status: "active",
      };
      // 清理 PDF 预览
      this.pdfFile = null;
      this.pdfFileName = "";
      this.pdfPages = [];
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
              if (this.$hasPermission && this.$hasPermission("NOTICE:STAT")) {
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
              } else {
                // 没有权限时不查询，确保字段存在但不暴露数值
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

    async submitForm() {
      if (
        !this.form.title ||
        !this.form.content ||
        !(this.form.departmentId || this.form.department)
      ) {
        alert("请填写标题、内容和部门！");
        return;
      }

      if (
        !this.form.issueDate ||
        !this.form.effectiveDate ||
        !this.form.expireDate
      ) {
        alert("请填写完整的日期信息！");
        return;
      }

      try {
        // 构造保存数据
        const noticeData = {
          title: this.form.title,
          content: this.form.content,
          // 兼容后端：优先传 departmentId，同时保留 department 名称回退
          departmentId: this.form.departmentId || null,
          department:
            (this.form.departmentId &&
              this.getDepartmentName(this.form.departmentId)) ||
            this.form.department ||
            "",
          issueDate: this.form.issueDate,
          effectiveDate: this.form.effectiveDate,
          expireDate: this.form.expireDate,
          status: "active",
          attachments: [],
        };

        // 如果是编辑，添加ID
        if (this.isEditing && this.form.id) {
          noticeData.id = this.form.id;
        }

        // 如果有 PDF 文件，先上传（直接把 File 对象发送，走后端 multipart 接口）
        if (this.pdfFile) {
          try {
            // Pass the File object so noticeApi.uploadFile will use multipart/form-data
            const payload = { file: this.pdfFile, name: this.pdfFile.name };
            const uploadRes = await noticeApi.uploadFile(payload);
            const ok =
              uploadRes && (uploadRes.code === 200 || uploadRes.code === "200");
            if (ok && uploadRes.data) {
              // backend returns { attachment: { name, filePath, fileUrl, uploadId } }
              let fileInfo = uploadRes.data.attachment || uploadRes.data;
              // attach preview pages if available
              if (this.pdfPages && this.pdfPages.length) {
                fileInfo.previewPages = [...this.pdfPages];
              }
              // normalize id field used across UI
              fileInfo.id = fileInfo.id || fileInfo.uploadId || fileInfo.fileId;
              noticeData.attachments.push(fileInfo);
            } else {
              console.warn("上传附件返回异常：", uploadRes);
            }
          } catch (err) {
            console.error("附件上传失败：", err);
            alert("附件上传失败，请重试或略过上传");
          }
        }

        // 保存到后端
        // 如果有上传的附件（upload-first），把 uploadIds 单独抽出并随 payload 提交，后端会把这些 upload 记录的 origin_id 更新为 noticeId
        const uploadIds = (noticeData.attachments || [])
          .map((a) => a.id || a.uploadId || a.fileId)
          .filter((x) => x);
        const payload = {
          administrativeNotice: noticeData,
          uploadIds: uploadIds,
        };
        await this.saveNotice(payload);

        alert(this.isEditing ? "公示修改成功！" : "公示添加成功！");
        this.showAddModal = false;
        // 清理临时上传数据
        this.pdfFile = null;
        this.pdfFileName = "";
        this.pdfPages = [];
        this.fetchNotices(); // 重新加载列表
      } catch (err) {
        alert((this.isEditing ? "修改失败：" : "添加失败：") + err.message);
      }
    },

    // 打开附件（在新标签页），增强匹配与调试信息
    async openAttachment(attachment) {
      if (!attachment) return;
      const attachId =
        attachment.id || attachment.uploadId || attachment.fileId;
      const normalize = (s) => (s ? String(s).trim().toLowerCase() : "");
      const shortName =
        attachment && attachment.name
          ? String(attachment.name).split("/").pop()
          : "";
      console.debug("openAttachment called", {
        attachId,
        name: attachment && attachment.name,
      });

      if (!attachId && !shortName) {
        alert("无法定位附件ID或文件名");
        return;
      }

      try {
        // 如果 attachment 已经包含 fileUrl（后端 MinIO 临时 URL），直接在新标签打开
        if (attachment.fileUrl) {
          window.open(attachment.fileUrl, "_blank");
          return;
        }

        let res;
        // 1) 按 id 查询（兼容 ）
        if (attachId) {
          console.debug("Trying //uploads/{id}", attachId);
          const r = await noticeApi.getUploadById(attachId);
          res = r;
          console.debug("result by id:", res);
        }

        // 2) 精确按 name 查询
        if ((!res || !(res.code === 200 && res.data)) && shortName) {
          // const q = encodeURIComponent(shortName);
          console.debug("Trying //uploads?name=", shortName);
          const r2 = await noticeApi.queryUploads({ name: shortName });
          res = r2;
          console.debug("result by name query:", res);
        }

        // 3) 获取所有 uploads 并进行容错匹配（忽略大小写、前后空白，允许部分匹配）
        if ((!res || !(res.code === 200 && res.data)) && shortName) {
          console.debug("Trying full list fuzzy match for", shortName);
          const r3 = await noticeApi.queryUploads();
          const listRes = r3;
          console.debug(
            "uploads list:",
            listRes && listRes.data ? listRes.data.length : listRes
          );
          if (listRes && listRes.code === 200 && Array.isArray(listRes.data)) {
            const key = normalize(shortName);
            const found = listRes.data.find((u) => {
              const un = normalize(u.name || "");
              return (
                un === key ||
                un.includes(key) ||
                key.includes(un) ||
                un.replace(/\s+/g, "") === key.replace(/\s+/g, "")
              );
            });
            console.debug("fuzzy match result:", found);
            if (found) res = { code: 200, data: found };
          }
        }

        if (res && res.code === 200 && res.data && res.data.base64) {
          const base64 = res.data.base64;
          const parts = base64.split(",");
          const mime = parts[0].match(/:(.*?);/)[1] || "application/pdf";
          const byteString = atob(parts[1]);
          const ab = new ArrayBuffer(byteString.length);
          const ia = new Uint8Array(ab);
          for (let i = 0; i < byteString.length; i++)
            ia[i] = byteString.charCodeAt(i);
          const blob = new Blob([ab], { type: mime });
          const url = URL.createObjectURL(blob);
          window.open(url, "_blank");
          setTimeout(() => URL.revokeObjectURL(url), 60 * 1000);
        } else {
          console.warn("openAttachment: no match found", { attachment, res });
          this.$message && this.$message.error
            ? this.$message.error(
                "无法获取附件，请检查 uploads 或 localStorage"
              )
            : console.error("无法获取附件，请检查 uploads 或 localStorage");
        }
      } catch (err) {
        console.error("openAttachment failed", err);
        this.$message && this.$message.error
          ? this.$message.error("打开附件失败")
          : console.error("打开附件失败", err);
      }
    },

    // 撤销公示（修改状态为expired）
    async revokeNotice(id, fromDetail = false) {
      if (this.$confirm) {
        this.$confirm("确定要撤销此公示吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(async () => {
            try {
              const notice = await this.fetchNoticeById(id);
              if (notice) {
                const updatedNotice = { ...notice, status: "expired" };
                await this.saveNotice(updatedNotice);
                this.$message && this.$message.success
                  ? this.$message.success("公示已撤销！")
                  : console.log("公示已撤销！");
                this.fetchNotices();
              }
            } catch (err) {
              this.$message && this.$message.error
                ? this.$message.error("撤销失败：" + (err.message || err))
                : console.error("撤销失败：", err);
            }
            if (fromDetail) this.closeDetailModal();
          })
          .catch(() => {});
      } else {
        if (confirm("确定要撤销此公示吗？")) {
          try {
            const notice = await this.fetchNoticeById(id);
            if (notice) {
              const updatedNotice = { ...notice, status: "expired" };
              await this.saveNotice(updatedNotice);
              alert("公示已撤销！");
              this.fetchNotices();
            }
          } catch (err) {
            alert("撤销失败：" + err.message);
          }
          if (fromDetail) this.closeDetailModal();
        }
      }
    },

    // 删除公示
    async handleDeleteNotice(id, fromDetail = false) {
      if (this.$confirm) {
        this.$confirm("确定要删除此公示吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(async () => {
            try {
              await this.deleteNotice(id);
              this.$message && this.$message.success
                ? this.$message.success("公示删除成功！")
                : console.log("公示删除成功！");
              this.fetchNotices();
              if (fromDetail) this.closeDetailModal();
            } catch (err) {
              this.$message && this.$message.error
                ? this.$message.error("删除失败：" + (err.message || err))
                : console.error("删除失败：", err);
            }
          })
          .catch(() => {});
      } else {
        if (confirm("确定要删除此公示吗？")) {
          try {
            await this.deleteNotice(id);
            alert("公示删除成功！");
            this.fetchNotices();
            if (fromDetail) this.closeDetailModal();
          } catch (err) {
            alert("删除失败：" + err.message);
          }
        }
      }
    },

    // 工具方法
    getTodayDate() {
      return new Date().toISOString().split("T")[0];
    },

    getNextMonthDate() {
      const date = new Date();
      date.setMonth(date.getMonth() + 1);
      return date.toISOString().split("T")[0];
    },
    // 文件转 base64
    fileToBase64(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(reader.result);
        reader.onerror = reject;
        reader.readAsDataURL(file);
      });
    },
  },
};
</script>

<style lang="scss" scoped>
$headHeight: 148px;
$bottomHeight: 161px;

.home-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #e5e5e5;
  .head {
    height: $headHeight;
    background-image: url("@/assets/home/top.png");
    &-operate {
      display: flex;
      justify-content: flex-end;
      padding: 45px;
      right: 10%;
      position: relative;
      z-index: 40; /* 设置一个较大的层级值 */
    }
  }
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    line-clamp: 2; /* 标准属性（建议添加，未来兼容） */
    overflow: hidden;
  }

  .fade-in {
    animation: fadeIn 0.3s ease-in-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  .bottom {
    height: $bottomHeight;
    background: #397dcd;
    width: 100%;
    display: flex;
    &-left {
      width: 30%;
    }
    &-middle {
      width: 50%;
      padding-top: 70px;
      p {
        text-align: center;
        color: #fff;
      }
    }
    &-right {
      width: 30%;
    }
  }
  .hidden {
    display: none; /* 彻底隐藏原生input */
  }
}
</style>
