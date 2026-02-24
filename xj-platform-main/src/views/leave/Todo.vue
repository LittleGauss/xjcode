<template>
  <div class="oa-homepage">
    <el-card>
      <div slot="header">
        <span>待办审批</span>
      </div>

      <el-table :data="todoList" style="width: 100%" v-loading="loading">
        <el-table-column
          prop="name"
          label="任务名称"
          width="150"
        ></el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="180"
        ></el-table-column>
        <el-table-column label="当前处理人" width="120">
          <template slot-scope="scope">
            {{ getUserName(scope.row.leave.username) }}
          </template>
        </el-table-column>
        <el-table-column label="申请人" width="120">
          <template slot-scope="scope">
            {{
              getUserName(
                scope.row && scope.row.leave && scope.row.leave.username
              )
            }}
          </template>
        </el-table-column>
        <el-table-column label="请假类型" width="120">
          <template slot-scope="scope">
            {{
              getLeaveTypeName(
                scope.row && scope.row.leave && scope.row.leave.leaveType
              )
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button size="mini" @click="handleAction(scope.row)">
              {{ getActionButtonText(scope.row) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <ApprovalDialog
      :visible.sync="approvalDialogVisible"
      :task="currentTask"
      :leave="currentLeave"
      :users="users"
      :loading="submitting"
      @submit="handleApprovalSubmit"
    />

    <!-- 销假申请对话框 -->
    <el-dialog
      title="销假申请"
      :visible.sync="reportBackDialogVisible"
      :close-on-click-modal="true"
      width="500px"
    >
      <el-form :model="reportBackForm" ref="reportBackForm" label-width="120px">
        <el-form-item label="实际开始时间" required>
          <el-date-picker
            v-model="reportBackForm.actualStartTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="实际结束时间" required>
          <el-date-picker
            v-model="reportBackForm.actualEndTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="yyyy-MM-dd HH:mm:ss"
          >
          </el-date-picker>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="reportBackDialogVisible = false">取 消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="submitReportBack"
          >确 定</el-button
        >
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { leaveApi } from "@/api/leave";
import { userApi } from "@/api/usermag";
import ApprovalDialog from "@/components/manage/LeaveDialog.vue";
import { getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "LeaveTodo",
  components: {
    ApprovalDialog,
  },
  data() {
    return {
      // 通用头部需要的数据
      login_user: null,
      userRoles: [],
      todoList: [],
      loading: false,
      approvalDialogVisible: false,
      reportBackDialogVisible: false,
      submitting: false,
      currentTask: null,
      currentLeave: null,
      currentUser: null,
      users: [],
      userMap: {},
      reportBackForm: {
        actualStartTime: "",
        actualEndTime: "",
      },
    };
  },
  mounted() {
    const loginUser = getUserToken();
    // 填充通用头需要的用户信息
    if (loginUser) {
      this.login_user = loginUser.user || loginUser;
      this.userRoles = loginUser.roles || [];
    }
    this.currentUser = loginUser && loginUser.user ? loginUser.user : null;
    this.loadUsers().then(() => this.loadTodoTasks());
  },

  methods: {
    // 【修改点 4】简化点击审批按钮的逻辑
    handleApproval(row) {
      this.currentTask = row;
      this.currentLeave = row && row.leave ? row.leave : null;
      // 原来的 this.nextApproverOptions 计算逻辑删掉，子组件会自动算
      this.approvalDialogVisible = true;
    },
    async handleApprovalSubmit(formData) {
      // 子组件已经校验过了，这里直接处理业务逻辑
      try {
        this.submitting = true;

        // 1. 构建 commentData (上下文信息由父组件填充，内容由子组件提供)
        const commentData = {};

        // 填充 businessId
        if (this.currentLeave && this.currentLeave.id != null) {
          const bidNum = Number(this.currentLeave.id);
          // 简单的整数范围校验
          if (
            Number.isFinite(bidNum) &&
            bidNum <= 2147483647 &&
            bidNum >= -2147483648
          ) {
            commentData.businessId = bidNum;
          }
        }

        // 填充当前操作人信息
        if (this.currentUser) {
          const aid = Number(this.currentUser.id);
          if (Number.isFinite(aid)) commentData.approverId = aid;
          const aname = this.currentUser.name || this.currentUser.username;
          if (aname) commentData.approverName = aname;
        }

        // 填充审批节点名称
        if (this.currentTask && this.currentTask.name) {
          commentData.approvalNode = this.currentTask.name;
        }

        // 填充具体的审批意见 (来自子组件)
        commentData.comment = formData.comment;

        // 2. 处理下一审批人格式
        const next = formData.nextApprover;
        const nextApproverStr =
          next == null || next === "" ? undefined : String(next);

        // 3. 调用 API
        await leaveApi.approveLeave(
          this.currentTask.id,
          formData.approved,
          nextApproverStr,
          commentData
        );

        this.$message.success("审批完成");
        this.approvalDialogVisible = false; // 关闭弹窗
        this.loadTodoTasks(); // 刷新列表
      } catch (err) {
        console.error(err);
        const msg = err?.response?.data?.message || err.message || "审批失败";
        this.$message.error(msg);
      } finally {
        this.submitting = false;
      }
    },
    // 顶部组件退出回调
    async handleLogout() {
      removeUserToken();
      try {
        this.$router.push("/login");
      } catch (e) {
        console.warn("navigate to login failed:", e);
      }
    },
    // 顶部组件导航回调，接收 route name 或 path
    handleNavigate(routeName) {
      if (!routeName) return;
      try {
        this.$router.push({ name: routeName });
      } catch (e) {
        try {
          this.$router.push(routeName);
        } catch (err) {
          console.warn("navigate failed:", err);
        }
      }
    },
    async loadUsers() {
      try {
        const res = await userApi.getUserList({ pageNum: 1, pageSize: 1000 });
        const list = Array.isArray(res && res.data)
          ? res.data
          : (res && res.data && (res.data.records || res.data.list)) || [];
        const map = {};
        list.forEach((u) => {
          if (!u) return;
          const id = u.id;
          const name = u.name || u.username || u.realName || `${id}`;
          if (id != null) map[id] = name;
        });
        this.userMap = map;
        this.users = list;
      } catch (e) {
        // 静默失败，后续用 ID 兜底
        this.userMap = {};
        this.users = [];
      }
    },
    async loadTodoTasks() {
      this.loading = true;
      try {
        const res = await leaveApi.getTodoTasks();
        const allTasks = res.data || [];

        // 【核心修改】：只保留请假流程的任务
        // 假设请假流程的 key 包含 'leave' 关键字，或者有 businessType 字段
        this.todoList = allTasks.filter((task) => {
          // 方式 A: 根据流程定义 Key (推荐)
          // 检查你的请假流程 XML 中的 id，通常是 'leaveProcess'
          if (task.processDefinitionKey) {
            return task.processDefinitionKey.includes("leave");
          }

          // 方式 B: 根据业务类型字段 (如果后端有返回)
          if (task.businessType) {
            return task.businessType === "leaveAndCancelProcess";
          }

          // 方式 C: 兜底 (根据任务名称或是否存在 leave 对象)
          return !!task.leave;
        });
      } catch (err) {
        this.$message.error("加载待办任务失败");
      } finally {
        this.loading = false;
      }
    },

    getActionButtonText(row) {
      if (row.taskDefinitionKey === "reportBackApply") {
        return "我要销假";
      }
      return "审批";
    },

    handleAction(row) {
      if (row.taskDefinitionKey === "reportBackApply") {
        this.handleReportBack(row);
      } else {
        this.handleApproval(row);
      }
    },

    handleReportBack(row) {
      this.currentTask = row;
      this.currentLeave = row && row.leave ? row.leave : null;
      // 预填时间
      if (this.currentLeave) {
        this.reportBackForm.actualStartTime = this.currentLeave.startDate;
        this.reportBackForm.actualEndTime = this.currentLeave.endDate;
      }
      this.reportBackDialogVisible = true;
    },

    async submitReportBack() {
      if (
        !this.reportBackForm.actualStartTime ||
        !this.reportBackForm.actualEndTime
      ) {
        this.$message.warning("请选择实际起止时间");
        return;
      }
      try {
        this.submitting = true;
        await leaveApi.submitReportBack(
          this.currentTask.id,
          this.reportBackForm
        );
        this.$message.success("销假申请已提交");
        this.reportBackDialogVisible = false;
        this.loadTodoTasks();
      } catch (err) {
        this.$message.error("提交失败");
      } finally {
        this.submitting = false;
      }
    },

    async submitApproval() {
      if (!this.approvalForm.comment) {
        this.$message.warning("请输入审批意见");
        return;
      }

      // 若在一级审批节点且选择“同意”，必须选定下一审批人（分管领导）
      const needNextApprover =
        this.approvalForm.approved &&
        this.currentTask &&
        this.currentTask.name === "一级审批";
      if (needNextApprover && !this.approvalForm.nextApprover) {
        this.$message.warning("请选择下一审批人");
        return;
      }

      try {
        if (this.submitting) return;
        this.submitting = true;
        // 提交审批意见（使用既有 complete 接口）
        const commentData = {};
        // 仅在可用时再填充，避免后端类型不匹配导致 400
        // 注意：后端 ApprovalComment.businessId 是 Integer，避免超范围导致 400
        if (this.currentLeave && this.currentLeave.id != null) {
          const bidNum = Number(this.currentLeave.id);
          if (
            Number.isFinite(bidNum) &&
            bidNum <= 2147483647 &&
            bidNum >= -2147483648
          ) {
            commentData.businessId = bidNum;
          }
          // 否则不传 businessId，由后端根据流程变量自动补全
        }
        if (this.currentUser) {
          const aid = Number(this.currentUser.id);
          if (Number.isFinite(aid)) commentData.approverId = aid;
          const aname = this.currentUser.name || this.currentUser.username;
          if (aname) commentData.approverName = aname;
        }
        if (this.currentTask && this.currentTask.name) {
          commentData.approvalNode = this.currentTask.name;
        }
        commentData.comment = this.approvalForm.comment;

        // nextApprover 统一转字符串，避免大整数精度问题
        const next = this.approvalForm.nextApprover;
        const nextApprover =
          next == null || next === "" ? undefined : String(next);

        await leaveApi.approveLeave(
          this.currentTask.id,
          this.approvalForm.approved,
          nextApprover,
          commentData
        );

        this.$message.success("审批完成");
        this.approvalDialogVisible = false;
        this.loadTodoTasks();
        this.resetApprovalForm();
      } catch (err) {
        const msg =
          (err &&
            err.response &&
            err.response.data &&
            err.response.data.message) ||
          (err && err.message) ||
          "审批失败";
        this.$message.error(msg);
      } finally {
        this.submitting = false;
      }
    },

    // resetApprovalForm() {
    //   this.approvalForm = {
    //     approved: true,
    //     nextApprover: "",
    //     comment: "",
    //   };
    // },

    getUserName(id) {
      if (!id && id !== 0) return "";
      return this.userMap[id] || `${id}`;
    },

    getLeaveTypeName(type) {
      const types = {
        annual: "年假",
        sick: "病假",
        personal: "事假",
        marriage: "婚假",
        maternity: "产假",
        funeral: "葬假",
        parental: "育儿假",
        visiting: "探亲假",
        paternity: "陪产假",
        business_trip: "出差",
      };
      return types[type] || type;
    },

    // 计算下一审批人选项
    // computeNextApproverOptions(task) {
    //   if (!Array.isArray(this.users) || this.users.length === 0) return [];

    //   // 识别角色/职位关键词
    //   const isViceLeader = (u) => {
    //     const jt = u.job_title || u.jobTitle || "";
    //     const nn = u.nickname || u.name || "";
    //     const un = u.username || "";
    //     return (
    //       jt.includes("副") ||
    //       jt.includes("分管") ||
    //       jt.includes("副总") ||
    //       nn.includes("副") ||
    //       nn.includes("分管") ||
    //       un.includes("vice_leader")
    //     );
    //   };

    //   // 一级审批（部门主管）审批时：仅允许选择分管领导
    //   const isFirstApproval = task && task.name === "一级审批";
    //   if (isFirstApproval) {
    //     return this.users
    //       .filter((u) => isViceLeader(u))
    //       .map((u) => ({
    //         value: u.id,
    //         label:
    //           u.nickname || u.name || u.username || u.realName || `ID:${u.id}`,
    //       }));
    //   }

    //   // 其他节点暂无下一审批人
    //   return [];
    // },
  },
};
</script>

<style scoped>
.oa-homepage {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.sub-label {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}
</style>
