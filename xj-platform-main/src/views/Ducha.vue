<template>
  <div class="home-container">
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @logout="handleLogout"
      @navigate="handleNavigate"
    />
    <div class="container mx-auto px-4 py-8 flex-1 overflow-y-auto p-6">
      <h2 class="text-2xl font-bold mb-4">督察</h2>
      <el-button type="primary" @click="goCreate">新建督察任务</el-button>

      <el-tabs
        v-model="activeTab"
        type="card"
        class="mb-4"
        style="margin-top: 12px"
      >
        <el-tab-pane label="任务列表" name="tasks">
          <div style="margin-bottom: 8px; color: #6b7280; font-size: 13px">
            仅显示您发起的任务（若需查看全部任务，请联系管理员）
          </div>

          <div class="mb-4 flex items-center" style="gap: 8px">
            <el-input
              v-model="filters.keyword"
              placeholder="按标题搜索"
              clearable
              @clear="fetchTasks"
              @keyup.enter.native="fetchTasks"
              style="width: 320px"
            />
            <el-button type="primary" @click="fetchTasks">搜索</el-button>
          </div>

          <el-table :data="tasks" stripe style="width: 100%; margin-top: 16px">
            <el-table-column prop="title" label="标题" />
            <!-- 内容列已移除：内容在操作->查看 中展示 -->
            <el-table-column label="发起人">
              <template slot-scope="{ row }">
                <span>{{
                  row.creatorName ||
                  row.starterName ||
                  getUserName(row.createdBy) ||
                  "-"
                }}</span>
              </template>
            </el-table-column>
            <el-table-column label="进度">
              <template slot-scope="{ row }">
                <div
                  style="
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    padding: 6px 0;
                    box-sizing: border-box;
                  "
                >
                  <div style="flex: 1">
                    <el-progress
                      class="ducha-progress"
                      :percentage="row.progress || 0"
                      :text-inside="false"
                      :show-text="false"
                      :stroke-width="8"
                      style="width: 100%"
                    />
                  </div>
                  <div
                    style="
                      width: 42px;
                      text-align: right;
                      font-size: 12px;
                      color: #2b6cb0;
                    "
                  >
                    {{ (row.progress || 0) + "%" }}
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="最近反馈">
              <template slot-scope="{ row }">
                <div v-if="row.lastFeedback">
                  <div style="font-weight: 600">
                    {{
                      row.lastFeedback.feedbackByName ||
                      row.lastFeedback.feedbackBy ||
                      "-"
                    }}
                  </div>
                  <div style="font-size: 12px; color: #555">
                    {{ shortText(row.lastFeedback.remarks || "", 80) }}
                  </div>
                  <div style="font-size: 11px; color: #999">
                    {{ row.lastFeedback.feedbackAt || "" }}
                  </div>
                </div>
                <div v-else>-</div>
              </template>
            </el-table-column>
            <el-table-column prop="dueDate" label="截止日期">
              <template slot-scope="{ row }">
                <span :style="overdueStyle(row)"
                  >{{ row.dueDate || row.bizDueDate || "-" }}
                  <span
                    v-if="row.isOverdue"
                    style="
                      margin-left: 6px;
                      color: #fff;
                      background: #e53e3e;
                      padding: 2px 6px;
                      border-radius: 4px;
                      font-size: 12px;
                    "
                    >逾期</span
                  >
                </span>
              </template>
            </el-table-column>

            <el-table-column label="状态">
              <template slot-scope="{ row }">
                <el-tag
                  :style="{ background: getStatusColor(row), color: '#fff' }"
                >
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作">
              <template slot-scope="{ row }">
                <el-button type="text" @click="viewDetail(row.id)"
                  >查看</el-button
                >
                <el-button
                  v-if="row.status !== 'completed'"
                  type="text"
                  @click="openEditModal(row.id)"
                  style="margin-right: 8px"
                  >编辑</el-button
                >

                <el-button
                  v-if="
                    row.status !== 'completed' &&
                    row.status !== 'cancelled' &&
                    row.status !== 'closed'
                  "
                  type="text"
                  style="color: #e53e3e; margin-right: 8px"
                  @click="revokeTask(row.id)"
                  >撤销</el-button
                >

                <el-dropdown
                  v-if="
                    isStarterOf(row) &&
                    (row.hasReviewTask ||
                      (row.status === 'feedback' &&
                        $hasPermission('NOTICE:FEEDBACK')))
                  "
                  trigger="click"
                  @command="(cmd) => quickOpenReview(row, cmd === 'approve')"
                >
                  <el-button type="primary" size="mini"
                    >审批<i class="el-icon-arrow-down el-icon--right"></i
                  ></el-button>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="approve">通过</el-dropdown-item>
                    <el-dropdown-item command="reject">驳回</el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="我的待办" name="process">
          <div class="mb-4 flex justify-between items-center">
            <div>显示当前登录用户在流程引擎中的待办任务</div>
            <div>
              <el-button type="primary" @click="fetchMyTasks">刷新</el-button>
            </div>
          </div>

          <el-table
            :data="myTasks"
            stripe
            style="width: 100%; margin-top: 16px"
          >
            <el-table-column prop="name" label="业务标题 / 节点" />
            <!-- 内容列已移除：内容在查看操作中展示 -->
            <el-table-column label="发起人"
              ><template slot-scope="{ row }"
                ><span>{{
                  row.starterName ||
                  getUserName(row.task && row.task.createdBy) ||
                  "-"
                }}</span></template
              ></el-table-column
            >
            <el-table-column label="截止/逾期"
              ><template slot-scope="{ row }"
                ><span :style="overdueStyle(row)"
                  >{{ row.bizDueDate || (row.task && row.task.dueDate) || "-"
                  }}<span
                    v-if="row.isOverdue"
                    style="
                      margin-left: 6px;
                      color: #fff;
                      background: #e53e3e;
                      padding: 2px 6px;
                      border-radius: 4px;
                      font-size: 12px;
                    "
                    >逾期</span
                  >
                </span>
              </template>
            </el-table-column>

            <el-table-column label="处理人">
              <template slot-scope="{ row }">
                <span>{{
                  getUserName(row.assignee) || row.assignee || "-"
                }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作">
              <template slot-scope="{ row }">
                <el-button type="text" @click="viewProcessTask(row)"
                  >查看</el-button
                >
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>

      <!-- 新建对话框 -->
      <el-dialog
        title="新建督察任务"
        :visible.sync="createModalVisible"
        :close-on-click-modal="true"
      >
        <el-form :model="formCreate" label-width="100px">
          <el-form-item label="标题"
            ><el-input v-model="formCreate.title"
          /></el-form-item>
          <el-form-item label="任务内容"
            ><el-input
              type="textarea"
              v-model="formCreate.description"
              :rows="4"
              placeholder="请输入任务描述"
          /></el-form-item>
          <el-form-item label="截止日期"
            ><el-date-picker v-model="formCreate.dueDate" type="date"
          /></el-form-item>
          <el-form-item label="分配给">
            <el-select
              v-model="formCreate.assignments"
              multiple
              placeholder="选择接收人员"
              style="width: 100%"
              collapse-tags
            >
              <el-option-group label="人员"
                ><el-option
                  v-for="u in users"
                  :key="'user-' + u.id"
                  :label="u.nickname || u.username || u.realName || u.name"
                  :value="'user:' + u.id"
              /></el-option-group>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="createModalVisible = false">取消</el-button
          ><el-button type="primary" @click="submitCreate">提交</el-button>
        </div>
      </el-dialog>

      <!-- 流程详情/完成/反馈等对话框 -->
      <el-dialog
        title="流程任务详情"
        :visible.sync="processDetailVisible"
        :close-on-click-modal="true"
        width="60%"
      >
        <div v-if="currentProcessTask">
          <p v-if="currentProcessTask.taskDefinitionKey">
            <strong>任务定义Key：</strong
            >{{ currentProcessTask.taskDefinitionKey }}
          </p>
          <p
            v-if="
              !currentProcessTask.task ||
              currentProcessTask.name !==
                (currentProcessTask.task && currentProcessTask.task.title)
            "
          >
            <strong>名称：</strong>{{ currentProcessTask.name }}
          </p>
          <p v-if="showProcessMeta">
            <strong>创建时间：</strong>
            {{ formatDate(currentProcessTask.createTime) }}
          </p>
          <p v-if="showProcessMeta">
            <strong>处理人：</strong>
            {{ currentProcessTask.assignee }}
          </p>
          <div
            v-if="currentProcessTask.task"
            style="
              margin-top: 12px;
              padding-top: 12px;
              border-top: 1px solid #eee;
            "
          >
            <h4 style="margin: 4px 0">督察任务</h4>
            <p><strong>标题：</strong>{{ currentProcessTask.task.title }}</p>
            <p v-if="currentProcessTask.task.description">
              <strong>内容：</strong>{{ currentProcessTask.task.description }}
            </p>
            <p v-if="currentProcessTask.task.dueDate">
              <strong>截止：</strong>{{ currentProcessTask.task.dueDate }}
            </p>
            <p v-if="currentProcessTask.task.status">
              <strong>状态：</strong
              >{{ getStatusLabel(currentProcessTask.task.status) }}
            </p>
            <div
              v-if="
                currentProcessTask.feedbacks &&
                currentProcessTask.feedbacks.length
              "
              style="margin-top: 8px"
            >
              <h5 style="margin: 4px 0">反馈记录</h5>
              <el-timeline>
                <el-timeline-item
                  v-for="f in currentProcessTask.feedbacks"
                  :key="f.id"
                  :timestamp="f.feedbackAt"
                >
                  <div>
                    <strong>{{ f.feedbackByName || f.feedbackBy }}</strong
                    >：{{ f.remarks }}
                  </div>
                  <div v-if="f.finishDate">办结日期: {{ f.finishDate }}</div>
                </el-timeline-item>
              </el-timeline>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button
            v-if="
              (isCurrentUserStarter() ||
                (currentProcessTask &&
                  currentProcessTask.reviewAssignedToStarter)) &&
              currentProcessTask &&
              (currentProcessTask.name === '发起人审批' ||
                currentProcessTask.taskDefinitionKey === 'review' ||
                currentProcessTask.hasReviewTask ||
                (currentProcessTask.task &&
                  currentProcessTask.task.status === 'feedback' &&
                  $hasPermission('NOTICE:FEEDBACK')))
            "
            type="success"
            @click="openReview(true)"
            >通过</el-button
          >
          <el-button
            v-if="
              (isCurrentUserStarter() ||
                (currentProcessTask &&
                  currentProcessTask.reviewAssignedToStarter)) &&
              currentProcessTask &&
              (currentProcessTask.name === '发起人审批' ||
                currentProcessTask.taskDefinitionKey === 'review' ||
                currentProcessTask.hasReviewTask ||
                (currentProcessTask.task &&
                  currentProcessTask.task.status === 'feedback' &&
                  $hasPermission('NOTICE:FEEDBACK')))
            "
            type="danger"
            @click="openReview(false)"
            >驳回</el-button
          >
          <el-button @click="openComplete(currentProcessTask)"
            >完成任务</el-button
          ><el-button @click="processDetailVisible = false"
            >关闭</el-button
          ></span
        >
      </el-dialog>

      <el-dialog
        title="完成流程任务"
        :visible.sync="completeModalVisible"
        :close-on-click-modal="true"
      >
        <el-form :model="completeForm"
          ><el-form-item label="办结日期"
            ><el-date-picker
              v-model="completeForm.finishDate"
              type="date" /></el-form-item
          ><el-form-item label="说明"
            ><el-input
              type="textarea"
              v-model="completeForm.remarks" /></el-form-item
        ></el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="completeModalVisible = false">取消</el-button
          ><el-button type="primary" @click="submitComplete">提交</el-button>
        </div>
      </el-dialog>

      <el-dialog
        title="督察任务详情"
        :visible.sync="detailModalVisible"
        :close-on-click-modal="true"
        width="60%"
      >
        <div v-if="currentTask">
          <p><strong>标题：</strong>{{ currentTask.title }}</p>
          <p><strong>内容：</strong>{{ currentTask.description }}</p>
          <p><strong>截止：</strong>{{ currentTask.dueDate }}</p>
          <p><strong>状态：</strong>{{ getStatusLabel(currentTask.status) }}</p>
          <div class="mt-4">
            <h4>反馈记录</h4>
            <el-timeline>
              <el-timeline-item
                v-for="f in feedbacks"
                :key="f.id"
                :timestamp="f.feedbackAt"
              >
                <div>
                  <strong>{{ f.feedbackByName || f.feedbackBy }}</strong
                  >：{{ f.remarks }}
                </div>
                <div v-if="f.finishDate">办结日期: {{ f.finishDate }}</div>
              </el-timeline-item>
            </el-timeline>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button
            v-if="canSubmitFeedback() && $hasPermission('NOTICE:FEEDBACK')"
            @click="openFeedback"
            >提交反馈</el-button
          >
          <el-button
            v-if="
              currentTask &&
              currentTask.status !== 'completed' &&
              currentTask.status !== 'cancelled' &&
              currentTask.status !== 'closed' &&
              isStarterOf(currentTask)
            "
            type="danger"
            @click="revokeTask(currentTask.id)"
            >撤销</el-button
          >
          <el-button @click="detailModalVisible = false">关闭</el-button>
        </span>
      </el-dialog>

      <el-dialog
        title="提交反馈"
        :visible.sync="feedbackModalVisible"
        :close-on-click-modal="true"
      >
        <el-form :model="formFeedback"
          ><el-form-item label="办结日期"
            ><el-date-picker
              v-model="formFeedback.finishDate"
              type="date" /></el-form-item
          ><el-form-item label="说明"
            ><el-input
              type="textarea"
              v-model="formFeedback.remarks" /></el-form-item
        ></el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="feedbackModalVisible = false">取消</el-button
          ><el-button type="primary" @click="submitFeedback">提交</el-button>
        </div>
      </el-dialog>

      <el-dialog
        title="发起人审批"
        :visible.sync="reviewModalVisible"
        :close-on-click-modal="true"
      >
        <el-form :model="reviewForm">
          <el-form-item label="说明（可选）">
            <el-input type="textarea" v-model="reviewForm.remarks" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="reviewModalVisible = false">取消</el-button
          ><el-button type="primary" @click="submitReview">确定</el-button>
        </div>
      </el-dialog>
    </div>
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import { duchaApi } from "@/api/ducha";
import { userApi } from "@/api/usermag";
import store from "@/store";
import { getUserToken, removeToken, removeUserToken } from "@/utils/auth";
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";

export default {
  name: "DuchaView",
  components: {
    HeaderComponent,
    FooterComponent,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
      tasks: [],
      page: 1,
      size: 10,
      filters: { keyword: "" },
      total: 0,
      activeTab: "tasks",
      myTasks: [],
      processDetailVisible: false,
      currentProcessTask: null,
      showProcessMeta: false,
      completeModalVisible: false,
      completeForm: { remarks: "", finishDate: null },
      createModalVisible: false,
      formCreate: {
        title: "",
        description: "",
        dueDate: null,
        assignments: [],
      },
      editMode: false,
      editId: null,
      departments: [],
      users: [],
      detailModalVisible: false,
      currentTask: null,
      feedbackModalVisible: false,
      formFeedback: { remarks: "", finishDate: null },
      reviewModalVisible: false,
      reviewForm: { approved: null, remarks: "" },
      feedbacks: [],
    };
  },
  async created() {
    // 读取登录信息以供 HeaderComponent 显示
    try {
      const storedUserInfo = getUserToken();
      if (storedUserInfo && storedUserInfo.user) {
        this.login_user = storedUserInfo.user;
        this.userRoles = storedUserInfo.roles || [];
      }
    } catch (e) {
      // ignore
    }
    await this.fetchAssignees();
    this.fetchTasks();
    // 如果从待办跳转携带业务ID/流程ID，则自动打开处理视图
    try {
      const q = (this.$route && this.$route.query) || {};
      const bizId = q.bizId ? Number(q.bizId) : null;
      const pid = q.pid ? String(q.pid) : null;
      if (bizId || pid) {
        this.viewProcessTask({ taskId: bizId, processInstanceId: pid });
        this.activeTab = "process";
      }
    } catch (e) {
      // ignore
    }
  },
  watch: {
    activeTab(v) {
      if (v === "process") this.fetchMyTasks();
    },
  },
  methods: {
    getStatusLabel(status) {
      const map = {
        draft: "草稿",
        open: "待处理",
        in_progress: "进行中",
        feedback: "需反馈",
        completed: "已完成",
        closed: "已关闭",
        cancelled: "已取消",
      };
      return map[status] || (status ? String(status) : "-");
    },
    handleLogout() {
      try {
        store.commit("SET_TOKEN", "");
      } catch (e) {
        // ignore if store not available
      }
      try {
        removeUserToken();
      } catch (e) {
        // ignore
      }
      this.login_user = null;
      try {
        removeToken();
      } catch (e) {
        // ignore
      }
      this.$router.push("/login");
    },
    handleNavigate(routeName) {
      if (!routeName) return;
      try {
        this.$router.push({ name: routeName });
      } catch (e) {
        try {
          this.$router.push(routeName);
        } catch (err) {
          console.warn("导航失败:", routeName, err);
        }
      }
    },
    getStatusColor(row) {
      // accept either row object or status string
      const status = row && row.status ? row.status : row;
      if (row && row.isOverdue) return "#e53e3e"; // red for overdue
      const map = {
        completed: "#48bb78",
        in_progress: "#3182ce",
        open: "#ed8936",
        draft: "#a0aec0",
      };
      return map[status] || "#a0aec0";
    },
    overdueStyle(row) {
      return {
        color: row && row.isOverdue ? "#c53030" : "inherit",
        fontWeight: row && row.isOverdue ? "600" : "normal",
      };
    },
    displayAssignments(row) {
      try {
        if (!row || !row.assignments || !row.assignments.length) return "-";
        const arr = row.assignments
          .slice(0, 2)
          .map(
            (a) =>
              a.assigneeUserName ||
              this.getUserName(a.assigneeUserId) ||
              a.assigneeUserId
          );
        let s = arr.join(", ");
        if (row.assignments.length > 2)
          s = s + ` +${row.assignments.length - 2}`;
        return s;
      } catch (e) {
        return "-";
      }
    },
    shortText(text, len = 40) {
      if (!text) return null;
      const s = String(text);
      return s.length > len ? s.substring(0, len - 1) + "…" : s;
    },
    formatDate(value) {
      if (!value) return "";
      try {
        // If value contains timezone region like [Asia/Shanghai], strip it
        let s = String(value);
        if (s.indexOf("[") !== -1) s = s.split("[")[0];
        // Some flows may pass numbers
        let d;
        if (!isNaN(Number(s))) d = new Date(Number(s));
        else d = new Date(s);
        if (isNaN(d.getTime())) return String(value);
        const Y = d.getFullYear();
        const M = String(d.getMonth() + 1).padStart(2, "0");
        const D = String(d.getDate()).padStart(2, "0");
        const h = String(d.getHours()).padStart(2, "0");
        const m = String(d.getMinutes()).padStart(2, "0");
        const sec = String(d.getSeconds()).padStart(2, "0");
        return `${Y}-${M}-${D} ${h}:${m}:${sec}`;
      } catch (e) {
        return String(value);
      }
    },
    async fetchAssignees() {
      try {
        const ures = await duchaApi.getUsers({ page: 1, size: 200 });
        if (ures && (ures.code === 200 || ures.code === "200")) {
          const ud = ures.data || {};
          this.users = ud.records || ud || [];
        }
      } catch (e) {
        console.warn("fetchAssignees failed", e);
      }
    },
    getUserName(id) {
      if (id === null || id === undefined) return null;
      try {
        const sid = String(id);
        if (Array.isArray(this.users) && this.users.length) {
          const u = this.users.find(
            (x) => String(x.id) === sid || String(x.username) === sid
          );
          if (u) return u.nickname || u.username || u.realName || u.name;
        }
      } catch (e) {
        return null;
      }
      return null;
    },
    async fetchTasks() {
      try {
        const params = {
          page: this.page,
          size: this.size,
          q: this.filters.keyword,
        };
        const res = await duchaApi.listTasks(params);
        if (res && (res.code === 200 || res.code === "200")) {
          const data = res.data || {};
          this.tasks = data.records || data || [];
          this.total = data.total || this.tasks.length;

          const missingIds = new Set();
          (this.tasks || []).forEach((row) => {
            if (row && row.createdBy && !this.getUserName(row.createdBy))
              missingIds.add(row.createdBy);
            if (row && row.assignments && Array.isArray(row.assignments)) {
              row.assignments.forEach((a) => {
                if (
                  a &&
                  a.assigneeUserId &&
                  !this.getUserName(a.assigneeUserId)
                )
                  missingIds.add(a.assigneeUserId);
              });
            }
          });

          if (missingIds.size > 0) {
            const jobs = Array.from(missingIds).map((id) =>
              userApi
                .getUserDetails(id)
                .then((r) =>
                  r && (r.code === 200 || r.code === "200") ? r.data : null
                )
                .catch(() => null)
            );
            const results = await Promise.all(jobs);
            const fetched = (results || []).filter(Boolean);
            if (fetched.length) this.users = (this.users || []).concat(fetched);
            this.tasks.forEach((row) => {
              if (row && row.createdBy) {
                const name = this.getUserName(row.createdBy);
                if (name) row.creatorName = name;
              }
              if (row && row.assignments && Array.isArray(row.assignments)) {
                row.assignments.forEach((a) => {
                  if (a && a.assigneeUserId) {
                    const an = this.getUserName(a.assigneeUserId);
                    if (an) a.assigneeUserName = an;
                  }
                });
              }
            });
          }
        } else this.$message.error("获取督察任务列表失败");
      } catch (e) {
        console.error(e);
        if (e && e.response && e.response.status === 404) {
          this.tasks = [];
          this.total = 0;
          return;
        }
        this.$message.error("获取督察任务列表异常");
      }
    },
    async fetchMyTasks() {
      try {
        const res = await duchaApi.listMyProcessTasks();
        if (res && (res.code === 200 || res.code === "200")) {
          // 后端返回的可能是多个 Flowable 任务（同一业务可能对应多个任务），
          // 对同一业务(taskId 或 processInstanceId)去重，保留最新一条，避免发起人看到重复审批按钮
          const raw = res.data || [];
          const byBiz = new Map();
          for (const it of raw) {
            const bizKey =
              it && (it.taskId || it.processInstanceId)
                ? String(it.taskId || it.processInstanceId)
                : null;
            if (!bizKey) continue;
            const existing = byBiz.get(bizKey);
            if (!existing) byBiz.set(bizKey, it);
            else {
              // 保留创建时间较晚的一条（更接近当前状态）
              try {
                const et = existing.createTime
                  ? new Date(existing.createTime).getTime()
                  : 0;
                const nt = it.createTime
                  ? new Date(it.createTime).getTime()
                  : 0;
                if (nt > et) byBiz.set(bizKey, it);
              } catch (e) {
                // 若解析失败，保持原有
              }
            }
          }
          this.myTasks = Array.from(byBiz.values());
          // 过滤：只保留能在应用业务表中找到对应业务记录的待办，避免展示来自其他系统或未映射的 Flowable 任务
          this.myTasks = this.myTasks.filter(
            (it) => it && it.task && it.task.id
          );
          // 进一步过滤：排除业务状态为已取消/已关闭/已完成的待办，避免撤销后仍可办理
          this.myTasks = this.myTasks.filter((it) => {
            const s = it && it.task && it.task.status;
            return s !== "cancelled" && s !== "closed" && s !== "completed";
          });
          const missingIds = new Set();
          (this.myTasks || []).forEach((it) => {
            if (
              it &&
              it.task &&
              it.task.createdBy &&
              !this.getUserName(it.task.createdBy)
            )
              missingIds.add(it.task.createdBy);
            if (it && it.assignee && !this.getUserName(it.assignee))
              missingIds.add(it.assignee);
          });

          if (missingIds.size > 0) {
            const jobs = Array.from(missingIds).map((id) =>
              userApi
                .getUserDetails(id)
                .then((r) =>
                  r && (r.code === 200 || r.code === "200") ? r.data : null
                )
                .catch(() => null)
            );
            const results = await Promise.all(jobs);
            const fetched = (results || []).filter(Boolean);
            if (fetched.length) this.users = (this.users || []).concat(fetched);
            this.myTasks.forEach((it) => {
              if (it && it.task && it.task.createdBy) {
                const name = this.getUserName(it.task.createdBy);
                if (name) it.starterName = name;
              }
              if (it && it.assignee) {
                const an = this.getUserName(it.assignee);
                if (an) it.assignee = an;
              }
            });
          }
        } else this.$message.error("获取我的待办失败");
      } catch (e) {
        console.error(e);
        this.$message.error("获取我的待办异常");
      }
    },
    goCreate() {
      this.createModalVisible = true;
    },
    async submitCreate() {
      if (!this.formCreate.title) return this.$message.warning("请输入标题");
      try {
        const assignments = (this.formCreate.assignments || [])
          .map((a) => {
            if (typeof a === "string") {
              if (a.startsWith("dept:"))
                return { type: "dept", id: Number(a.split(":")[1]) };
              if (a.startsWith("user:"))
                return { type: "user", id: Number(a.split(":")[1]) };
              return null;
            }
            return { type: a.type, id: a.id };
          })
          .filter((x) => x != null);
        const payload = {
          title: this.formCreate.title,
          description: this.formCreate.description,
          dueDate: this.formCreate.dueDate,
          assignments,
        };
        let res;
        if (this.editMode && this.editId) {
          res = await duchaApi.updateTask(this.editId, payload);
        } else {
          res = await duchaApi.createTask(payload);
        }
        if (res && (res.code === 200 || res.code === "200")) {
          this.$message.success(this.editMode ? "更新成功" : "创建成功");
          this.createModalVisible = false;
          const created = res.data;
          this.formCreate = {
            title: "",
            description: "",
            dueDate: null,
            assignments: [],
          };
          // reset edit state
          const wasEdit = this.editMode;
          this.editMode = false;
          this.editId = null;
          if (created && created.id) {
            if (this.tasks && Array.isArray(this.tasks)) {
              const idx = this.tasks.findIndex(
                (t) => String(t.id) === String(created.id)
              );
              if (idx >= 0) this.$set(this.tasks, idx, created);
              else this.tasks = [created].concat(this.tasks || []);
            } else this.fetchTasks();
            if (!wasEdit) this.total = (this.total || 0) + 1;
          } else this.fetchTasks();
        } else this.$message.error(this.editMode ? "更新失败" : "创建失败");
      } catch (e) {
        console.error(e);
        this.$message.error("创建异常");
      }
    },
    async viewDetail(id) {
      try {
        const res = await duchaApi.getTask(id);
        if (res && (res.code === 200 || res.code === "200")) {
          // prefer the actual task object if present (backend returns { task:..., feedbacks: [...] })
          this.currentTask =
            res.data && res.data.task ? res.data.task : res.data || null;
          this.feedbacks =
            res.data && res.data.feedbacks ? res.data.feedbacks : [];
          this.detailModalVisible = true;
        } else this.$message.error("获取任务详情失败");
      } catch (e) {
        console.error(e);
        this.$message.error("获取任务详情异常");
      }
    },
    async viewProcessTask(t) {
      // Normalize the incoming object and prefer explicit reviewTaskIds / flowableTaskId
      let payload = Object.assign({}, t || {});
      try {
        // If backend provided reviewTaskIds (from tasks list), use the first one
        if (t && Array.isArray(t.reviewTaskIds) && t.reviewTaskIds.length) {
          payload.id = t.reviewTaskIds[0];
          payload.taskDefinitionKey = payload.taskDefinitionKey || "review";
        } else if (t && t.assignments && Array.isArray(t.assignments)) {
          // try to find a flowableTaskId on the assignments
          const a = t.assignments.find((x) => x && x.flowableTaskId);
          if (a && a.flowableTaskId) payload.id = a.flowableTaskId;
        } else if (t && t.flowableTaskId) {
          payload.id = t.flowableTaskId;
        } else if (t && t.processInstanceId && !payload.id) {
          payload.id = t.processInstanceId;
        }
      } catch (e) {
        // ignore
      }

      this.currentProcessTask = payload;
      try {
        console.debug("viewProcessTask - currentProcessTask:", payload);
      } catch (e) {
        // ignore
      }

      // If we have a business id (taskId) or a task object on the payload, ensure task details present
      try {
        const bizId =
          payload.taskId || (payload.task && payload.task.id) || null;
        if (bizId) {
          // If payload.id is missing or likely a processInstanceId, attempt to resolve active Flowable tasks for this business
          let needResolveTask = false;
          if (!payload.id) needResolveTask = true;
          if (
            payload.id &&
            payload.processInstanceId &&
            payload.id === payload.processInstanceId
          )
            needResolveTask = true;

          if (needResolveTask) {
            try {
              const tres = await duchaApi.getProcessTasksByBusiness(bizId);
              if (tres && (tres.code === 200 || tres.code === "200")) {
                const tasks = tres.data || [];
                // prefer review node
                const chosen =
                  tasks.find((x) => x.taskDefinitionKey === "review") ||
                  tasks[0];
                if (chosen) {
                  payload.id = chosen.id;
                  payload.taskDefinitionKey =
                    payload.taskDefinitionKey || chosen.taskDefinitionKey;
                  payload.assignee = payload.assignee || chosen.assignee;
                }
              }
            } catch (e) {
              // ignore network errors here
            }
          }

          const res = await duchaApi.getTask(bizId);
          if (res && (res.code === 200 || res.code === "200")) {
            this.currentProcessTask.task =
              res.data && res.data.task ? res.data.task : res.data;
            // attach feedbacks if backend returned them
            this.currentProcessTask.feedbacks =
              res.data && res.data.feedbacks ? res.data.feedbacks : [];
          }
        } else if (payload.task) {
          // payload already contains business object
          this.currentProcessTask.task = payload.task;
        }
      } catch (e) {
        console.warn("加载业务详情失败", e);
      }

      this.processDetailVisible = true;
    },
    async openEditModal(id) {
      try {
        const res = await duchaApi.getTask(id);
        if (res && (res.code === 200 || res.code === "200")) {
          const data =
            res.data && res.data.task ? res.data.task : res.data || {};
          this.formCreate = {
            title: data.title || "",
            description: data.description || "",
            dueDate: data.dueDate || data.bizDueDate || null,
            assignments: (data.assignments || [])
              .map((a) => {
                if (!a) return null;
                if (a.type === "user" || a.assigneeUserId) {
                  return `user:${a.id || a.assigneeUserId}`;
                }
                if (a.type === "dept" || a.deptId) {
                  return `dept:${a.id || a.deptId}`;
                }
                return null;
              })
              .filter(Boolean),
          };
          this.editMode = true;
          this.editId = id;
          this.createModalVisible = true;
        } else {
          this.$message.error("加载编辑数据失败");
        }
      } catch (e) {
        console.error(e);
        this.$message.error("加载编辑数据异常");
      }
    },
    openComplete(task) {
      this.currentProcessTask = task;
      this.completeForm = { remarks: "", finishDate: null };
      this.completeModalVisible = true;
    },
    async submitComplete() {
      if (!this.currentProcessTask || !this.currentProcessTask.id)
        return this.$message.error("缺少流程任务ID");
      try {
        const vars = {
          feedback: this.completeForm.remarks,
          finishDate: this.completeForm.finishDate,
        };
        const res = await duchaApi.completeProcessTask(
          this.currentProcessTask.id,
          vars
        );
        if (res && (res.code === 200 || res.code === "200")) {
          this.$message.success("完成任务成功");

          // 如果后端返回了更新后的业务对象，优先用它更新本地列表以立即反映进度/状态
          const updated =
            res.data && (res.data.task || res.data)
              ? res.data.task || res.data
              : null;
          if (updated && updated.id) {
            try {
              const idx = this.tasks.findIndex(
                (t) => String(t.id) === String(updated.id)
              );
              if (idx >= 0) this.$set(this.tasks, idx, updated);
              else this.tasks = [updated].concat(this.tasks || []);
            } catch (e) {
              // fallback to full refresh
              this.fetchTasks();
            }
          } else {
            // fallback: refresh whole list
            this.fetchTasks();
          }

          this.completeModalVisible = false;
          this.processDetailVisible = false;
          this.fetchMyTasks();
        } else this.$message.error("完成任务失败");
      } catch (e) {
        console.error(e);
        this.$message.error("完成任务异常");
      }
    },
    openFeedback() {
      this.formFeedback = { remarks: "", finishDate: null };
      this.feedbackModalVisible = true;
    },
    isCurrentUserStarter() {
      try {
        const stored = getUserToken();
        if (!stored) return false;
        const u = stored.user || stored;
        const uid = u && (u.id || u.userId || u.uid || u.accountId);
        if (!uid || !this.currentProcessTask) return false;
        if (!this.currentProcessTask.task) return false;
        return String(this.currentProcessTask.task.createdBy) === String(uid);
      } catch (e) {
        return false;
      }
    },
    // 判断某条业务记录是否由当前登录用户发起（用于在列表中判定是否显示审批入口）
    isStarterOf(row) {
      try {
        const stored = getUserToken();
        if (!stored) return false;
        const u = stored.user || stored;
        const uid = u && (u.id || u.userId || u.uid || u.accountId);
        if (!uid || !row) return false;
        const creator =
          row.createdBy ||
          row.starterId ||
          (row.task && row.task.createdBy) ||
          null;
        return creator != null && String(creator) === String(uid);
      } catch (e) {
        return false;
      }
    },
    openReview(approved) {
      this.reviewForm = { approved: approved, remarks: "" };
      this.reviewModalVisible = true;
    },
    quickOpenReview(row, approved) {
      try {
        // try to find a process/flowable task id from the row
        let pid = null;
        if (row) {
          if (Array.isArray(row.reviewTaskIds) && row.reviewTaskIds.length) {
            pid = row.reviewTaskIds[0];
          } else if (row.processTaskId) {
            pid = row.processTaskId;
          } else if (row.flowableTaskId) {
            pid = row.flowableTaskId;
          }
        }

        this.currentProcessTask = { id: pid, task: row };
        this.reviewForm = { approved: approved, remarks: "" };

        if (!pid) {
          // if no process task id, warn user and still allow them to enter remarks,
          // but do NOT automatically open the full process detail to avoid
          // showing two dialogs at once.
          this.$message.warning(
            "未找到流程任务ID，已打开审批窗口，请先确认或通过“查看”查看详情。"
          );
        }

        // Open only the review modal here. Do not open `processDetailVisible` to
        // avoid duplicate dialogs stacking on top of each other.
        this.reviewModalVisible = true;
      } catch (e) {
        console.error("quickOpenReview error", e);
        this.$message.error("操作失败");
      }
    },
    async submitReview() {
      if (!this.currentProcessTask || !this.currentProcessTask.id)
        return this.$message.error("缺少流程任务ID");
      try {
        // 驳回时必须填写说明
        if (
          this.reviewForm.approved === false &&
          (!this.reviewForm.remarks || !String(this.reviewForm.remarks).trim())
        ) {
          return this.$message.warning("驳回时请填写说明");
        }
        const vars = {
          approved: this.reviewForm.approved,
          feedback: this.reviewForm.remarks,
        };
        const res = await duchaApi.completeProcessTask(
          this.currentProcessTask.id,
          vars
        );
        if (res && (res.code === 200 || res.code === "200")) {
          this.$message.success("提交审批成功");
          this.reviewModalVisible = false;
          this.processDetailVisible = false;

          // If backend returned updated business object, update local tasks list
          const updated =
            res.data && (res.data.task || res.data)
              ? res.data.task || res.data
              : null;
          if (updated && updated.id) {
            try {
              const idx = this.tasks.findIndex(
                (t) => String(t.id) === String(updated.id)
              );
              if (idx >= 0) this.$set(this.tasks, idx, updated);
              else this.tasks = [updated].concat(this.tasks || []);
            } catch (e) {
              // fallback to full refresh
              this.fetchTasks();
            }
          } else {
            // fallback: refresh whole list so UI reflects new status/progress
            this.fetchTasks();
          }

          // refresh process-related lists and detail view
          this.fetchMyTasks();
          if (this.currentProcessTask.task && this.currentProcessTask.task.id)
            await this.viewDetail(this.currentProcessTask.task.id);
        } else this.$message.error("提交审批失败");
      } catch (e) {
        console.error(e);
        this.$message.error("提交审批异常");
      }
    },
    async submitFeedback() {
      if (!this.currentTask || !this.currentTask.id)
        return this.$message.error("缺少任务ID");
      try {
        const payload = {
          remarks: this.formFeedback.remarks,
          finishDate: this.formFeedback.finishDate,
        };
        const res = await duchaApi.submitFeedback(this.currentTask.id, payload);
        if (res && (res.code === 200 || res.code === "200")) {
          this.$message.success("反馈提交成功");
          this.feedbackModalVisible = false;
          await this.viewDetail(this.currentTask.id);
          this.fetchTasks();
        } else this.$message.error("反馈提交失败");
      } catch (e) {
        console.error(e);
        this.$message.error("提交反馈异常");
      }
    },
    // 是否允许当前登录用户提交反馈
    canSubmitFeedback() {
      try {
        const stored = getUserToken();
        if (!stored) return false;
        // stored may be { user: { id: ... } } or direct user object
        const u = stored.user || stored;
        const uid = u && (u.id || u.userId || u.uid || u.accountId);
        if (!uid || !this.currentTask) return false;
        const sid = String(uid);
        // creator can always feedback
        if (
          this.currentTask.createdBy &&
          String(this.currentTask.createdBy) === sid
        )
          return true;
        // assigned users can feedback
        if (
          this.currentTask.assignments &&
          Array.isArray(this.currentTask.assignments)
        ) {
          for (const a of this.currentTask.assignments) {
            if (!a) continue;
            if (a.assigneeUserId && String(a.assigneeUserId) === sid)
              return true;
            if (a.assigneeUser && String(a.assigneeUser) === sid) return true;
          }
        }
        return false;
      } catch (e) {
        return false;
      }
    },
    onPageChange(p) {
      this.page = p;
      this.fetchTasks();
    },
    // 撤销督察任务：优先调用后端撤销接口，失败则回退为状态更新
    async revokeTask(id) {
      if (!id) return;
      const doRevoke = async () => {
        try {
          // 1) 尝试后端专用撤销接口（应同时处理流程待办）
          let ures = null;
          try {
            ures = await duchaApi.revokeTask(id);
          } catch (err) {
            // 404/网络异常等不阻断，记录并继续回退
            console.warn("revoke API not available, fallback to update", err);
          }
          // 2) 若接口不存在或失败，回退为普通更新
          if (!(ures && (ures.code === 200 || ures.code === "200"))) {
            const res = await duchaApi.getTask(id);
            if (!(res && (res.code === 200 || res.code === "200"))) {
              return this.$message.error("加载任务信息失败");
            }
            const task =
              res.data && res.data.task ? res.data.task : res.data || {};
            const payload = { ...task, status: "cancelled" };
            ures = await duchaApi.updateTask(id, payload);
          }
          if (ures && (ures.code === 200 || ures.code === "200")) {
            this.$message.success("任务已撤销");
            // 更新列表
            try {
              const idx = this.tasks.findIndex(
                (t) => String(t.id) === String(id)
              );
              if (idx >= 0)
                this.$set(this.tasks, idx, {
                  ...this.tasks[idx],
                  status: "cancelled",
                });
            } catch (e) {
              // 回退到刷新
              this.fetchTasks();
            }
            // 若在详情弹窗中，刷新显示
            if (
              this.detailModalVisible &&
              this.currentTask &&
              String(this.currentTask.id) === String(id)
            ) {
              this.currentTask = { ...this.currentTask, status: "cancelled" };
            }
            // 刷新我的待办，确保撤销后不再可办理
            this.fetchMyTasks();
          } else {
            this.$message.error("撤销失败");
          }
        } catch (e) {
          console.error(e);
          // 若为 404 等后端未提供撤销接口场景，避免用户困惑，提示为“已回退状态更新”
          this.$message.warning("后端撤销接口不可用，已回退为状态更新");
        }
      };

      if (this.$confirm) {
        this.$confirm("确定要撤销此督察任务吗？", "提示", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning",
        })
          .then(doRevoke)
          .catch(() => {});
      } else {
        if (window.confirm && window.confirm("确定要撤销此督察任务吗？")) {
          await doRevoke();
        }
      }
    },
  },
};
</script>

<style scoped>
container {
  max-width: 100%;
}

/* Ensure el-progress internal text is fully visible and centered */
.ducha-progress {
  position: relative;
}

/* Make table cells wrap so long content doesn't force horizontal scrolling */
.el-table th,
.el-table td {
  white-space: normal;
}
.el-table .cell {
  word-break: break-word;
}
</style>
