<template>
  <div class="pending-tasks-page">
    <!-- 头部组件 -->
    <HeaderComponent
      :login_user="login_user"
      :userRoles="userRoles"
      @navigate="handleNavigate"
      @logout="handleLogout"
    />
    <!-- 主要内容区域 -->
    <main class="main-content">
      <el-card class="search-card">
        <div slot="header">
          <span>待办任务筛选</span>
        </div>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="业务类型">
            <el-select
              v-model="searchForm.businessType"
              placeholder="请选择业务类型"
              clearable
              @change="loadPendingTasks"
            >
              <!-- 这里就是流程key 可以根据这个来查对应流程的任务 -->
              <el-option label="全部类型" value=""></el-option>
              <el-option
                label="请销假"
                value="leaveAndCancelProcess"
              ></el-option>
              <el-option
                label="耗材领用"
                value="consumableApplicationProcess"
              ></el-option>
              <el-option
                label="合同审核"
                value="contractReviewProcess"
              ></el-option>
              <el-option label="督查任务" value="supervision"></el-option>
              <el-option
                label="日常监督检查"
                value="inspectionTaskProcess"
              ></el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="关键字">
            <el-input
              v-model="searchForm.keyword"
              placeholder="申请人/标题"
              clearable
              @keyup.enter.native="loadPendingTasks"
            ></el-input>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              icon="el-icon-search"
              @click="loadPendingTasks"
            >
              查询
            </el-button>
            <el-button icon="el-icon-refresh" @click="resetSearch">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="tasks-card">
        <div slot="header">
          <span>待办任务列表</span>
          <el-badge :value="total" class="total-badge">
            <el-button
              style="float: right; padding: 3px 0"
              type="text"
              @click="loadPendingTasks"
            >
              刷新
            </el-button>
          </el-badge>
        </div>

        <el-table
          :data="pendingTasks"
          v-loading="loading"
          element-loading-text="加载中..."
          stripe
          style="width: 100%"
        >
          <el-table-column prop="businessType" label="业务类型" width="120">
            <template slot-scope="scope">
              <el-tag :type="getBusinessTypeTag(scope.row.businessType)">
                {{ getBusinessTypeName(scope.row.businessType) }}
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="taskName" label="任务标题" width="150">
            <template slot-scope="scope">
              <span class="task-title" @click="viewTaskDetail(scope.row)">
                {{ scope.row.taskName }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="assignee" label="申请人" width="120">
            <template slot-scope="scope">
              <span>
                {{
                  getUseNameByBusinessType(scope.row, scope.row.businessType)
                }}
              </span>
            </template>
          </el-table-column>

          <el-table-column prop="department" label="申请部门" width="150">
            <template slot-scope="scope">
              {{ getDeptByBusinessType(scope.row, scope.row.businessType) }}
            </template>
          </el-table-column>

          <el-table-column prop="createTime" label="申请时间" width="120">
            <template slot-scope="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="assignee" label="当前审核人" width="120">
            <template slot-scope="scope">
              <span>
                {{
                  getApproverByBusinessType(scope.row, scope.row.businessType)
                }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template slot-scope="scope">
              <!-- 督查任务：根据业务状态切换“处理/审核” -->
              <el-button
                v-if="
                  String(scope.row.businessType).includes('supervision') &&
                  !isSupervisionCompleted(scope.row)
                "
                size="mini"
                type="primary"
                @click="goHandleSupervision(scope.row)"
              >
                处理
              </el-button>
              <el-button
                v-if="
                  String(scope.row.businessType).includes('supervision') &&
                  isSupervisionCompleted(scope.row)
                "
                size="mini"
                type="primary"
                @click="handleApprove(scope.row)"
              >
                审核
              </el-button>
              <el-button
                v-if="!String(scope.row.businessType).includes('supervision')"
                size="mini"
                type="primary"
                @click="handleApprove(scope.row)"
              >
                审批/处理
              </el-button>
              <el-button
                size="mini"
                @click="
                  showDiagram(
                    scope.row.processInstanceId,
                    scope.row.businessType
                  )
                "
              >
                查看流程图
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-container">
          <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pagination.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
          >
          </el-pagination>
        </div>
      </el-card>

      <ApprovalDialog
        :visible.sync="leaveApprovalVisible"
        :task="{ ...currentTask, name: currentTask.taskName }"
        :leave="currentLeave"
        :users="users"
        :loading="approvalLoading"
        @submit="handleLeaveApprovalSubmit"
      />
      <ContractApprovalDialog
        v-if="contractApprovalVisible"
        :visible.sync="contractApprovalVisible"
        :contract="currentContract"
        :loading="approvalLoading"
        @submit="handleContractApprovalSubmit"
      />
      <!-- 审批对话框 -->
      <el-dialog
        :title="
          '审批 - ' +
          ((currentTask &&
            (currentTask.title ||
              (currentTask.businessData &&
                (currentTask.businessData.title ||
                  currentTask.businessData.subject)) ||
              currentTask.taskName)) ||
            '')
        "
        :visible.sync="approvalDialogVisible"
        :close-on-click-modal="true"
        width="640px"
      >
        <!-- 督办任务详情：仅在业务类型为督查任务时展示 -->
        <div
          v-if="String(currentTask.businessType || '').includes('supervision')"
          class="supervision-detail"
        >
          <el-card shadow="never" class="supervision-card">
            <div slot="header" class="clearfix">
              <span>督办任务详情</span>
            </div>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="标题">
                {{
                  (currentTask.businessData &&
                    (currentTask.businessData.title ||
                      currentTask.businessData.subject ||
                      currentTask.taskName)) ||
                  currentTask.taskName
                }}
              </el-descriptions-item>
              <el-descriptions-item label="发起人">
                {{
                  currentTask.businessData &&
                  (currentTask.businessData.creatorName ||
                    currentTask.businessData.applicantName)
                }}
              </el-descriptions-item>
              <el-descriptions-item label="部门">
                {{
                  currentTask.businessData &&
                  (currentTask.businessData.creatorDepartment ||
                    currentTask.businessData.department ||
                    currentTask.businessData.applicantDept)
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData &&
                  (currentTask.businessData.deadline ||
                    currentTask.businessData.endTime)
                "
                label="截止时间"
              >
                {{
                  formatDate(
                    currentTask.businessData.deadline ||
                      currentTask.businessData.endTime
                  )
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData && currentTask.businessData.status
                "
                label="当前状态"
              >
                {{ currentTask.businessData.status }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData &&
                  (currentTask.businessData.content ||
                    currentTask.businessData.description)
                "
                :span="2"
                label="任务内容"
              >
                {{
                  currentTask.businessData.content ||
                  currentTask.businessData.description
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="attachments() && attachments().length"
                :span="2"
                label="附件"
              >
                <el-link
                  v-for="(att, idx) in attachments()"
                  :key="idx"
                  :href="att.url || att"
                  type="primary"
                  target="_blank"
                  style="margin-right: 8px"
                  >{{ att.name || att.fileName || "附件" + (idx + 1) }}</el-link
                >
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>
        <!-- 反馈记录：仅在督查任务显示 -->
        <div
          v-if="String(currentTask.businessType || '').includes('supervision')"
        >
          <el-card
            shadow="never"
            class="supervision-card"
            style="margin-top: 12px"
          >
            <div slot="header" class="clearfix">
              <span>反馈记录</span>
            </div>
            <div v-if="currentFeedbacks && currentFeedbacks.length">
              <el-timeline>
                <el-timeline-item
                  v-for="(fb, idx) in currentFeedbacks"
                  :key="fb.id || idx"
                  :timestamp="formatDate(fb.feedbackAt)"
                >
                  <div>
                    <strong>{{ fb.feedbackByName || "未知" }}</strong
                    >：{{ fb.remarks }}
                  </div>
                </el-timeline-item>
              </el-timeline>
            </div>
            <div v-else class="empty-text">暂无反馈</div>
          </el-card>
        </div>
        <el-form :model="approvalForm" ref="approvalForm" label-width="80px">
          <el-form-item label="审批意见">
            <el-input
              type="textarea"
              v-model="approvalForm.comment"
              :rows="4"
              placeholder="请输入审批意见"
            ></el-input>
          </el-form-item>

          <el-form-item
            v-if="
              currentTask.nextApprovers && currentTask.nextApprovers.length > 0
            "
            label="下一审批人"
          >
            <el-select
              v-model="approvalForm.nextApprover"
              placeholder="请选择下一审批人"
              style="width: 100%"
            >
              <el-option
                v-for="approver in currentTask.nextApprovers"
                :key="approver.id"
                :label="approver.name"
                :value="approver.id"
              >
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>

        <span slot="footer" class="dialog-footer">
          <el-button @click="approvalDialogVisible = false">取 消</el-button>
          <el-button
            type="success"
            @click="submitApproval('approve')"
            :loading="approvalLoading"
          >
            通 过
          </el-button>
          <el-button
            type="danger"
            @click="submitApproval('reject')"
            :loading="approvalLoading"
          >
            驳 回
          </el-button>
        </span>
      </el-dialog>

      <!-- 督查任务处理弹窗（与审核弹窗区分） -->
      <el-dialog
        :title="
          '处理 - ' +
          ((currentTask &&
            (currentTask.taskName ||
              (currentTask.businessData &&
                (currentTask.businessData.title ||
                  currentTask.businessData.subject)))) ||
            '')
        "
        :visible.sync="handleDialogVisible"
        :close-on-click-modal="true"
        width="640px"
      >
        <!-- 任务详情展示（同审核弹窗的督查详情区块，可按需简化） -->
        <div
          v-if="String(currentTask.businessType || '').includes('supervision')"
          class="supervision-detail"
        >
          <el-card shadow="never" class="supervision-card">
            <div slot="header" class="clearfix">
              <span>督办任务详情</span>
            </div>
            <el-descriptions :column="2" size="small" border>
              <el-descriptions-item label="标题">
                {{
                  (currentTask.businessData &&
                    (currentTask.businessData.title ||
                      currentTask.businessData.subject ||
                      currentTask.taskName)) ||
                  currentTask.taskName
                }}
              </el-descriptions-item>
              <el-descriptions-item label="发起人">
                {{
                  currentTask.businessData &&
                  (currentTask.businessData.creatorName ||
                    currentTask.businessData.applicantName)
                }}
              </el-descriptions-item>
              <el-descriptions-item label="部门">
                {{
                  currentTask.businessData &&
                  (currentTask.businessData.creatorDepartment ||
                    currentTask.businessData.department ||
                    currentTask.businessData.applicantDept)
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData &&
                  (currentTask.businessData.deadline ||
                    currentTask.businessData.endTime)
                "
                label="截止时间"
              >
                {{
                  formatDate(
                    currentTask.businessData.deadline ||
                      currentTask.businessData.endTime
                  )
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData && currentTask.businessData.status
                "
                label="当前状态"
              >
                {{ currentTask.businessData.status }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="
                  currentTask.businessData &&
                  (currentTask.businessData.content ||
                    currentTask.businessData.description)
                "
                :span="2"
                label="任务内容"
              >
                {{
                  currentTask.businessData.content ||
                  currentTask.businessData.description
                }}
              </el-descriptions-item>
              <el-descriptions-item
                v-if="attachments() && attachments().length"
                :span="2"
                label="附件"
              >
                <el-link
                  v-for="(att, idx) in attachments()"
                  :key="idx"
                  :href="att.url || att"
                  type="primary"
                  target="_blank"
                  style="margin-right: 8px"
                  >{{ att.name || att.fileName || "附件" + (idx + 1) }}</el-link
                >
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </div>

        <!-- 处理表单（与审核不同） -->
        <el-form :model="handleForm" ref="handleForm" label-width="80px">
          <el-form-item label="处理说明">
            <el-input
              type="textarea"
              v-model="handleForm.comment"
              :rows="4"
              placeholder="请输入处理说明"
            />
          </el-form-item>
        </el-form>

        <span slot="footer" class="dialog-footer">
          <el-button @click="handleDialogVisible = false">取 消</el-button>
          <el-button
            type="primary"
            @click="submitHandle()"
            :loading="approvalLoading"
            >提 交</el-button
          >
        </span>
      </el-dialog>

      <!-- 详情弹框组件 -->
      <task-detail-modal
        ref="taskDetailModal"
        :users="users"
        @close="handleCloseModal"
      />
    </main>

    <!-- 流程图子组件 -->
    <FlowDiagram
      :visible="showDiagramFlag"
      :process-instance-id="processInstanceId"
      :dialogTitle="flowDiagramTitle"
      :default-scale="2.0"
      :dialog-width="'100%'"
      @update:visible="showDiagramFlag = $event"
      @close="handleDiagramClose"
    />
    <!-- 使用通用页脚 -->
    <FooterComponent :userRoles="userRoles" @navigate="handleNavigate" />
  </div>
</template>

<script>
import HeaderComponent from "@/components/HeaderComponent.vue";
import FooterComponent from "@/components/FooterComponent.vue";
import FlowDiagram from "@/components/flow/FlowDiagram.vue";
import TaskDetailModal from "@/components/TodoDetail/TaskDetailModal.vue";
import ApprovalDialog from "@/components/manage/LeaveDialog.vue";
import ContractApprovalDialog from "@/components/manage/ContractApprovalDialog.vue";

import { removeToken, getUserToken, removeUserToken } from "@/utils/auth";
import { pendingApi } from "@/api/pendingtask";
import { leaveApi } from "@/api/leave";
import { userApi } from "@/api/usermag";
import contractApi from "@/api/contract";
import { duchaApi } from "@/api/ducha";

export default {
  name: "PendingTasks",
  components: {
    HeaderComponent,
    FooterComponent,
    FlowDiagram,
    TaskDetailModal,
    ApprovalDialog,
    ContractApprovalDialog,
  },
  data() {
    return {
      login_user: null,
      userRoles: [],
      leaveApprovalVisible: false, // 控制请假弹框显示
      currentLeave: null, // 当前请假详情数据
      users: [], // 所有用户列表（用于选择下一审批人）
      // 【新增 3】合同审批相关数据
      contractApprovalVisible: false,
      currentContract: null,
      loading: false,
      approvalLoading: false,
      searchForm: {
        businessType: "",
        keyword: "",
      },
      pagination: {
        currentPage: 1,
        pageSize: 10,
        total: 0,
      },
      pendingTasks: [],
      total: 0,

      // 审批相关
      approvalDialogVisible: false,
      approvalForm: {
        comment: "",
        nextApprover: "",
      },
      currentTask: {},
      userMap: {},
      currentFeedbacks: [],

      // 处理相关（处理弹窗）
      handleDialogVisible: false,
      handleForm: {
        result: "completed", // completed/feedback/closed
        comment: "",
        attachments: [],
      },

      // 详情相关
      detailDrawerVisible: false,
      activeTab: "basic",

      processInstanceId: "",
      showDiagramFlag: false,

      flowDiagramTitle: "流程图",
    };
  },

  created() {
    const storedUserInfo = getUserToken();
    if (storedUserInfo && storedUserInfo.user) {
      this.login_user = storedUserInfo.user;
      this.userRoles = storedUserInfo.roles;
    } else {
      this.$message.warning("您尚未登录，请先登录！");
      // 如果未登录，跳转到登录页面
      this.$router.push("/login");
    }
    this.loadUsers();
    this.ensureUserMap().then(() => this.loadPendingTasks());
  },

  methods: {
    // 【新增 5】加载所有用户的方法
    async loadUsers() {
      try {
        const res = await userApi.getUserList({ pageNum: 1, pageSize: 1000 });
        this.users = Array.isArray(res && res.data)
          ? res.data
          : (res && res.data && (res.data.records || res.data.list)) || [];
      } catch (e) {
        console.error("加载用户列表失败", e);
        this.users = [];
      }
    },
    // 加载待办任务列表
    async loadPendingTasks() {
      this.loading = true;
      try {
        const params = {
          page: this.pagination.currentPage,
          size: this.pagination.pageSize,
          businessType: this.searchForm.businessType, //可以没值
          keyword: this.searchForm.keyword, //可以没值
          assigneeId: this.login_user.id, //必须有值没值
        };

        const response = await pendingApi.getPendingTasks(params);
        console.log("待办任务列表:", response.data);
        this.pendingTasks = response.data.records;
        this.total = response.data.total;
      } catch (error) {
        this.$message.error("加载待办任务失败: " + error.message);
      } finally {
        this.loading = false;
      }
    },
    // 当前任务的附件（督办任务）
    attachments() {
      try {
        const bd = this.currentTask && this.currentTask.businessData;
        const list =
          (bd && (bd.attachments || bd.files || bd.attachmentList)) || [];
        // 统一为数组
        return Array.isArray(list) ? list : [];
      } catch (e) {
        return [];
      }
    },

    // 重置搜索条件
    resetSearch() {
      this.searchForm = {
        businessType: "",
        keyword: "",
      };
      this.pagination.currentPage = 1;
      this.loadPendingTasks();
    },

    // 分页相关
    handleSizeChange(val) {
      this.pagination.pageSize = val;
      this.loadPendingTasks();
    },

    handleCurrentChange(val) {
      this.pagination.currentPage = val;
      this.loadPendingTasks();
    },

    // 业务类型相关
    getBusinessTypeName(type) {
      const typeMap = {
        leaveAndCancelProcess: "请销假",
        consumableApplicationProcess: "低值易耗品",
        contractReviewProcess: "合同审核",
        supervision: "督查任务",
        inspectionTaskProcess: "日常监督检查",
      };
      return typeMap[type] || "未知类型";
    },
    // 加载一次用户映射，便于把ID显示成人名
    async ensureUserMap() {
      try {
        if (this._userMapLoaded) return;
        const res = await duchaApi.getUsers({ page: 1, size: 500 });
        const list = (res && res.data) || (res && res.records) || [];
        const map = {};
        (Array.isArray(list) ? list : []).forEach((u) => {
          if (!u) return;
          const id = u.id != null ? String(u.id) : null;
          if (!id) return;
          const name =
            u.nickname || u.name || u.username || u.realName || `ID:${id}`;
          map[id] = name;
        });
        this.userMap = map;
        this._userMapLoaded = true;
      } catch (e) {
        this._userMapLoaded = true;
      }
    },

    resolveName(val) {
      try {
        if (val == null) return "未知";
        if (typeof val === "string" && isNaN(Number(val))) return val;
        const idStr = String(val);
        return this.userMap[idStr] || idStr;
      } catch (e) {
        return String(val);
      }
    },

    getBusinessTypeTag(type) {
      const tagMap = {
        leaveAndCancelProcess: "请销假流程",
        consumableApplicationProcess: "低值易耗品流程",
        contractReviewProcess: "合同审核流程",
        supervision: "督查任务流程",
        inspectionTaskProcess: "日常监督检查",
      };
      return tagMap[type] || "info";
    },
    // 时间格式化
    formatDate(dateTimeString) {
      if (!dateTimeString) return "";
      const date = new Date(dateTimeString);
      return date.toLocaleString("zh-CN");
    },

    // 查看待办详情
    async viewTaskDetail(task) {
      try {
        this.$refs.taskDetailModal.show(task);
      } catch (error) {
        this.$message.error("获取任务详情失败: " + error.message);
      }
    },
    /**
     * 处理弹框关闭事件
     */
    handleCloseModal() {
      console.log("弹框已关闭");
      // 可以在这里执行一些清理操作
    },

    // 处理审批 不同业务的审批流程不一样
    handleApprove(task) {
      this.currentTask = task;

      // 判断业务类型
      if (task.businessType == "leaveAndCancelProcess") {
        // === 情况 A：请销假流程 ===
        this.currentLeave = task.businessData || {};
        this.leaveApprovalVisible = true;
      } else if (task.businessType == "contractReviewProcess") {
        // === 【新增逻辑】情况 B：合同审核流程 ===
        // 1. 提取合同详情 (pendingTask接口返回的 businessData 通常就是合同实体)
        this.currentContract = task.businessData || {};

        // 2. 如果后端返回的字段名不一致，可能需要做简单映射 (例如 taskName -> contractName)
        // 这里假设 businessData 已经包含了合同信息
        if (!this.currentContract.contractName && task.taskName) {
          // 仅作兜底，具体看你的 businessData 结构
          this.currentContract.contractName =
            task.businessData.contractName || "未命名合同";
        }

        // 3. 打开合同专用弹窗
        this.contractApprovalVisible = true;
      } else if (task.businessType == "consumableApplicationProcess") {
        this.$router.push({
          // 跳转到低值易耗品审批页面
          path: "/consumable",
          query: { showApproval: true },
        });
      } else if (task.businessType == "inspectionTaskProcess") {
        if (task.businessData.assignment == null) {
          this.$router.push("/inspection/task-monitor").catch((err) => {
            if (err.name != "NavigationDuplicated") throw err;
          });
        } else {
          this.$router.push("/inspection/inspector-task").catch((err) => {
            if (err.name != "NavigationDuplicated") throw err;
          });
        }
      } else {
        // === 情况 D：其他流程 (保持原有通用逻辑) ===
        this.approvalForm = {
          comment: "",
          nextApprover: "",
        };
        // 督查任务：预加载反馈记录
        try {
          const bizType = String(task.businessType || "");
          if (bizType.includes("supervision")) {
            this.loadSupervisionFeedbacks(task);
          } else {
            this.currentFeedbacks = [];
          }
        } catch (e) {
          this.currentFeedbacks = [];
        }
        this.approvalDialogVisible = true;
      }
    },

    // 【新增 5】处理合同审批提交
    async handleContractApprovalSubmit(formData) {
      try {
        this.approvalLoading = true;

        // 1. 获取合同 ID 和 任务 ID
        // 注意：待办列表返回的 task 对象中，id 字段通常就是 taskId
        const taskId = this.currentTask.id || this.currentTask.taskId;
        const contractId = this.currentContract.id;

        if (!taskId || !contractId) {
          this.$message.error("缺少任务ID或合同ID，无法审批");
          return;
        }

        // 2. 构造提交参数
        const payload = {
          taskId: taskId,
          contractId: contractId,
          approved: formData.approved, // true or false
          comment: formData.comment,
        };

        // 3. 调用合同审批接口
        const res = await contractApi.completeTask(payload);

        if (res?.success || res?.code == 200) {
          this.$message.success(formData.approved ? "审批通过" : "已驳回");
          this.contractApprovalVisible = false; // 关闭弹窗
          this.loadPendingTasks(); // 刷新列表
        } else {
          this.$message.warning(res?.message || "操作未成功");
        }
      } catch (e) {
        console.error("合同审批失败", e);
        this.$message.error("审批操作失败");
      } finally {
        this.approvalLoading = false;
      }
    },
    // 【新增 7】处理 ApprovalDialog 提交的事件
    async handleLeaveApprovalSubmit(formData) {
      try {
        this.approvalLoading = true; // 复用 loading 状态

        // 1. 构造提交参数 (逻辑参考 LeaveTodo.vue)
        const commentData = {};

        // 填充 businessId
        if (this.currentLeave && this.currentLeave.id) {
          commentData.businessId = Number(this.currentLeave.id);
        }

        // 填充审批人信息
        if (this.login_user) {
          commentData.approverId = this.login_user.id;
          commentData.approverName =
            this.login_user.name || this.login_user.username;
        }

        // 填充节点名称
        if (this.currentTask && this.currentTask.taskName) {
          // 注意：PendingTask接口返回的是 taskName，ApprovalDialog 里 task.name
          // 需要确保字段对应。这里传给后端记录日志。
          commentData.approvalNode = this.currentTask.taskName;
        }

        commentData.comment = formData.comment;

        // 处理下一审批人
        const next = formData.nextApprover;
        const nextApproverStr =
          next == null || next === "" ? undefined : String(next);

        // 2. 调用请假审批接口
        // 注意：ApprovalDialog 传回来的 task 对象通常包含 id。
        // 在待办列表中，task id 字段通常是 task.id 或 task.taskId，请根据实际接口返回确认
        const taskId = this.currentTask.id || this.currentTask.taskId;

        await leaveApi.approveLeave(
          taskId,
          formData.approved,
          nextApproverStr,
          commentData
        );

        this.$message.success("审批完成");
        this.leaveApprovalVisible = false; // 关闭弹窗
        this.loadPendingTasks(); // 刷新列表
      } catch (err) {
        console.error(err);
        const msg = err?.response?.data?.message || err.message || "审批失败";
        this.$message.error(msg);
      } finally {
        this.approvalLoading = false;
      }
    },
    // 提交审批
    async submitApproval(action) {
      this.approvalLoading = true;
      try {
        const approve = action === "approve";
        const task = this.currentTask || {};
        const bizType = String(task.businessType || "");
        // 督查任务走督查流程接口，传递 Flowable 任务ID 和变量 approved
        if (bizType.includes("supervision")) {
          let taskId = task.taskId || task.id;
          // 若列表项未携带任务ID，尝试通过业务ID/流程实例ID解析当前审核节点
          if (!taskId) {
            const bizKey =
              task.businessKey || (task.businessData && task.businessData.id);
            const pid = task.processInstanceId || task.pid || task.processId;
            try {
              if (bizKey) {
                const tres = await duchaApi.getProcessTasksByBusiness(bizKey);
                const tlist = (tres && (tres.data || tres)) || [];
                // 优先匹配审核节点（按定义Key或名称包含“审核”）
                const review = tlist.find(
                  (x) =>
                    String(x.taskDefinitionKey || "")
                      .toLowerCase()
                      .includes("review") ||
                    String(x.name || "").includes("审核")
                );
                taskId =
                  (review && (review.id || review.taskId)) ||
                  (tlist[0] && (tlist[0].id || tlist[0].taskId));
              }
              // 回退：通过流程实例ID获取“我的流程任务”并尝试匹配
              if (!taskId && pid) {
                const mres = await duchaApi.listMyProcessTasks();
                const mlist = (mres && (mres.data || mres)) || [];
                const candid = mlist.find(
                  (x) => String(x.processInstanceId || x.pid) === String(pid)
                );
                taskId = candid && (candid.id || candid.taskId);
              }
            } catch (e) {
              // 保持静默，交由后续校验抛错
            }
          }
          if (!taskId)
            throw new Error("未找到流程任务ID，请先通过“查看”打开详情");
          const vars = {
            approved: approve,
            comment: this.approvalForm.comment,
          };
          await duchaApi.completeProcessTask(taskId, vars);
        } else {
          // 其他流程暂未接入，这里直接抛出提示或按需补充对应接口
          throw new Error("当前流程类型暂未接入审批接口");
        }

        this.$message.success(`${approve ? "审批通过" : "审批驳回"}成功`);
        this.approvalDialogVisible = false;
        await this.loadPendingTasks();
      } catch (error) {
        this.$message.error(
          `${action === "approve" ? "审批通过" : "审批驳回"}失败: ${
            error && error.message ? error.message : error
          }`
        );
      } finally {
        this.approvalLoading = false;
      }
    },

    // 加载督查任务反馈记录（用于审核弹窗展示）
    async loadSupervisionFeedbacks(task) {
      try {
        const bizId =
          task.businessKey ||
          (task.businessData &&
            (task.businessData.id || task.businessData.taskId)) ||
          task.taskId ||
          task.id;
        if (!bizId) {
          this.currentFeedbacks = [];
          return;
        }
        const resp = await duchaApi.getTask(bizId);
        const d = resp && resp.data ? resp.data : resp;
        const list =
          (d && d.feedbacks) || (d && d.data && d.data.feedbacks) || [];
        this.currentFeedbacks = Array.isArray(list) ? list : [];
      } catch (e) {
        this.currentFeedbacks = [];
      }
    },

    // 提交处理（与审核不同的业务流）
    async submitHandle() {
      this.approvalLoading = true;
      try {
        const task = this.currentTask || {};
        const bizType = String(task.businessType || "");
        if (!bizType.includes("supervision")) {
          throw new Error("仅督查任务支持处理弹窗");
        }
        // 业务ID：用于保存反馈
        const bizId =
          task.businessKey ||
          (task.businessData &&
            (task.businessData.id || task.businessData.taskId)) ||
          task.taskId ||
          task.id;

        let taskId = task.taskId || task.id; // Flowable 任务ID：用于完成流程任务
        if (!taskId) {
          const bizKey =
            task.businessKey || (task.businessData && task.businessData.id);
          if (bizKey) {
            const tres = await duchaApi.getProcessTasksByBusiness(bizKey);
            const tlist = (tres && (tres.data || tres)) || [];
            taskId = tlist[0] && (tlist[0].id || tlist[0].taskId);
          }
        }
        if (!bizId) throw new Error("未找到业务ID，无法保存处理说明");
        // 先保存业务反馈到数据库
        await duchaApi.submitFeedback(bizId, {
          remarks: this.handleForm.comment,
        });
        // 再完成流程任务，不再携带 feedback，避免与上一步重复入库
        if (!taskId) throw new Error("未找到流程任务ID，无法推进流程");
        await duchaApi.completeProcessTask(taskId, {});
        this.$message.success("处理提交成功");
        this.handleDialogVisible = false;
        await this.loadPendingTasks();
      } catch (error) {
        this.$message.error(
          `处理提交失败: ${error && error.message ? error.message : error}`
        );
      } finally {
        this.approvalLoading = false;
      }
    },
    handleNavigate(routeName) {
      // 处理头部组件的导航事件
      if (routeName === "AboutPage") {
        return; // 如果已经在关于我们页面，不进行跳转
      }
      this.$router.push({ name: routeName });
    },
    // 拿到任务申请人的姓名
    getUseNameByBusinessType(rowData, businessType) {
      // 定义了一个映射对象applicantFieldMap，将业务类型映射到对应的申请人ID字段名
      // 通过传入的businessType参数获取相应的字段名
      // 从rowData.businessData对象中返回对应字段的值
      const applicantFieldMap = {
        leaveAndCancelProcess: "username", //userId 有值  原则上应该直接显示姓名userName
        consumableApplicationProcess: "applicantName", //审核人是assigneeName
        contractReviewProcess: "applicantName",
        supervision: "creatorName", // 使用 creatorName 字段
        inspectionTaskProcess: "mainTask.initiatorName", // 使用 initiatorName 字段
      };
      const applicantIdField = applicantFieldMap[businessType];
      if (!rowData || !rowData.businessData || !applicantIdField) return "";
      //const raw = rowData.businessData[applicantIdField];
      const raw = this.getNestedValue(rowData.businessData, applicantIdField);
      if (raw == null || raw === "") return "";

      if (typeof raw === "object") {
        return raw.username || raw.nickname || raw.name || JSON.stringify(raw);
      }

      const rawStr = String(raw);
      const users = Array.isArray(this.users) ? this.users : [];
      const found = users.find(
        (u) =>
          String(u.id) === rawStr ||
          String(u.userId) === rawStr ||
          String(u.username) === rawStr ||
          String(u.nickname) === rawStr
      );
      if (found)
        return found.username || found.nickname || found.name || rawStr;

      return rawStr;
    },
    /**
     * 解析嵌套对象的属性（支持 "a.b.c" 或 "a[b][c]" 格式）
     * @param {Object} obj 源对象
     * @param {String} path 属性路径
     * @returns {*} 解析后的值（无值返回空字符串）
     */
    getNestedValue(obj, path) {
      if (!obj || !path || typeof path !== "string") return "";

      // 统一路径格式：把 "a[b][c]" 转换成 "a.b.c"
      const normalizedPath = path
        .replace(/\[([^\]]+)\]/g, ".$1") // 把 [xxx] 替换成 .xxx
        .replace(/^\./, ""); // 去掉开头的 .（如果有）

      // 按 . 分割路径成数组（如 ["mainTask", "initiatorName"]）
      const pathArr = normalizedPath.split(".");

      // 逐层取值（容错：某一层不存在时返回空字符串）
      return pathArr.reduce((current, key) => {
        return current && typeof current === "object" && key in current
          ? current[key]
          : "";
      }, obj);
    },
    getApproverByBusinessType(rowData, businessType) {
      // 定义了一个映射对象，将业务类型映射到对应的审核人字段名
      // 如果字段是用户ID，则尝试用 this.users 查找对应姓名并返回
      const applicantFieldMap = {
        leaveAndCancelProcess: "currentApprover",
        consumableApplicationProcess: "assigneeName", // 审核人可能已是姓名
        contractReviewProcess: "currentApprover",
        supervision: "currentApprover", // 使用 currentApprover 字段
        inspectionTaskProcess: "assignee", // 使用 assignee 字段
      };

      const field = applicantFieldMap[businessType];
      if (!rowData || !rowData.businessData || !field) return "";
      let raw = "";
      if (businessType == "inspectionTaskProcess") {
        //当是日常监督检查时，当前处理人需要判断下
        raw = rowData.assignee;
      } else {
        raw = rowData.businessData[field];
      }
      if (raw == null || raw == "") return "";

      // 仅在请销假和合同审核时尝试把 id 转成姓名（避免对其他流程做不必要的解析）
      const shouldResolveName =
        businessType === "leaveAndCancelProcess" ||
        businessType === "contractReviewProcess" ||
        businessType === "inspectionTaskProcess";

      // 如果返回的是对象，优先取常见姓名字段
      if (typeof raw === "object") {
        return raw.name || raw.realName || raw.username || "";
      }

      const rawStr = String(raw);

      if (shouldResolveName) {
        // 尝试在已加载的用户列表中按 id/用户名匹配（兼容字符串或数字 id）
        const users = Array.isArray(this.users) ? this.users : [];
        const found = users.find(
          (u) =>
            String(u.id) === rawStr ||
            String(u.userId) === rawStr ||
            String(u.username) === rawStr
        );
        if (found)
          return found.username || found.nickname || found.name || rawStr;

        // 如果 rawStr 明显包含非数字字符，很可能已经是姓名，直接返回
        if (/\D/.test(rawStr)) return rawStr;

        // 回退：返回原始字符串（可能是 id）
        return rawStr;
      }

      // 其它流程不解析 id，仅返回原始值（或对象的姓名字段已在上面处理）
      return rawStr;
    },
    // 拿到任务申请人的部门
    getDeptByBusinessType(rowData, businessType) {
      // 定义了一个映射对象applicantFieldMap，将业务类型映射到对应的申请人ID字段名
      // 通过传入的businessType参数获取相应的字段名
      // 从rowData.businessData对象中返回对应字段的值
      const applicantFieldMap = {
        leaveAndCancelProcess: "departmentName",
        consumableApplicationProcess: "applicantDept", //申请人部门
        contractReviewProcess: "department",
        supervision: "creatorDepartment", // 使用 creatorDepartment 字段
      };
      if (businessType == "inspectionTaskProcess") {
        return "质量检测中心";
      } else {
        const applicantIdField = applicantFieldMap[businessType];
        return rowData.businessData[applicantIdField] || "未知";
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
    showDiagram(processInstanceId, businessType) {
      if (!processInstanceId) {
        this.$message.warning("流程实例不存在");
        return;
      }
      this.flowDiagramTitle = this.getBusinessTypeTag(businessType) + `-流程图`;
      console.log("流程实例ID:", processInstanceId);
      this.processInstanceId = processInstanceId;
      this.showDiagramFlag = true;
    },
    handleDiagramClose() {
      this.showDiagramFlag = false;
      this.processInstanceId = "";
    },
    // 督查任务处理：打开“处理”弹窗（与审核弹窗不同）
    goHandleSupervision(row) {
      try {
        this.currentTask = row;
        this.handleForm = {
          result: "completed",
          comment: "",
          attachments: [],
        };
        this.handleDialogVisible = true;
      } catch (e) {
        console.warn("打开督查处理弹窗失败", e);
        this.$message.error("打开处理弹窗失败，请稍后重试");
      }
    },
    // 判断督查任务是否已完成，用于切换按钮显示为“审核”
    isSupervisionCompleted(row) {
      try {
        const data = row && row.businessData ? row.businessData : {};
        const status = (data.status || "").toLowerCase();
        // completed/closed/feedback 视为已完成流转，待审核
        return (
          status === "completed" || status === "closed" || status === "feedback"
        );
      } catch (e) {
        return false;
      }
    },
  },
};
</script>

<style scoped>
.pending-tasks-page {
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
.pending-tasks {
  flex: 1;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.tasks-card {
  margin-bottom: 20px;
}

.total-badge {
  margin-left: 10px;
}

.task-title {
  color: #409eff;
  cursor: pointer;
}

.task-title:hover {
  text-decoration: underline;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.task-detail {
  padding: 20px;
}
</style>
