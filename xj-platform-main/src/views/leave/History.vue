<template>
  <div class="page-container">
    <div class="main-content">
      <!-- 切换按钮 -->
      <div style="margin-bottom: 20px">
        <el-button-group>
          <el-button
            :type="activeTab === 'myLeaves' ? 'primary' : ''"
            @click="activeTab = 'myLeaves'"
          >
            我的请假记录
          </el-button>
          <el-button
            :type="activeTab === 'myApprovals' ? 'primary' : ''"
            @click="switchToApprovals"
          >
            我的审批记录
          </el-button>
        </el-button-group>
      </div>

      <!-- 我的请假记录 -->
      <el-card v-if="activeTab === 'myLeaves'">
        <div slot="header">
          <span>我的请假记录</span>
        </div>

        <el-table :data="historyList" style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="80"></el-table-column>
          <el-table-column prop="leaveType" label="请假类型" width="120">
            <template slot-scope="scope">
              {{ getLeaveTypeName(scope.row.leaveType) }}
            </template>
          </el-table-column>
          <el-table-column label="申请人" width="120">
            <template slot-scope="scope">
              {{ scope.row.username || currentUserName }}
            </template>
          </el-table-column>
          <el-table-column
            prop="startDate"
            label="开始时间"
            width="180"
          ></el-table-column>
          <el-table-column
            prop="endDate"
            label="结束时间"
            width="180"
          ></el-table-column>
          <el-table-column prop="status" label="状态" width="120">
            <template slot-scope="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusName(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="createdAt"
            label="申请时间"
            width="180"
          ></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewDetail(scope.row)"
                >查看详情</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 我的审批记录 -->
      <el-card v-if="activeTab === 'myApprovals'">
        <div slot="header">
          <span>我的审批记录</span>
        </div>

        <el-table
          :data="approvalList"
          style="width: 100%"
          v-loading="loadingApprovals"
        >
          <!-- <el-table-column prop="id" label="ID" width="80"></el-table-column> -->
          <el-table-column label="申请人" width="120">
            <template slot-scope="scope">
              {{ scope.row.username || currentUserName }}
            </template>
          </el-table-column>
          <el-table-column prop="leaveType" label="请假类型" width="120">
            <template slot-scope="scope">
              {{ getLeaveTypeName(scope.row.leaveType) }}
            </template>
          </el-table-column>
          <el-table-column
            prop="startDate"
            label="开始时间"
            width="180"
          ></el-table-column>
          <el-table-column
            prop="endDate"
            label="结束时间"
            width="180"
          ></el-table-column>
          <el-table-column prop="approvalResult" label="审批结果" width="120">
            <template slot-scope="scope">
              <el-tag
                :type="
                  scope.row.approvalResult === 'APPROVED' ? 'success' : 'danger'
                "
              >
                {{
                  scope.row.approvalResult === "APPROVED" ? "已同意" : "已拒绝"
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column
            prop="approvedTime"
            label="审批时间"
            width="180"
          ></el-table-column>
          <el-table-column label="操作">
            <template slot-scope="scope">
              <el-button size="mini" @click="viewApprovalDetail(scope.row)"
                >查看详情</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-dialog
        title="请假详情"
        :visible.sync="detailDialogVisible"
        :close-on-click-modal="true"
        width="700px"
      >
        <el-form label-width="100px">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="请假类型">
                {{ getLeaveTypeName(currentLeave && currentLeave.leaveType) }}
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="状态">
                <el-tag
                  :type="getStatusType(currentLeave && currentLeave.status)"
                >
                  {{ getStatusName(currentLeave && currentLeave.status) }}
                </el-tag>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="开始时间">
                {{ currentLeave && currentLeave.startDate }}
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="结束时间">
                {{ currentLeave && currentLeave.endDate }}
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="请假原因">
            {{ currentLeave && currentLeave.reason }}
          </el-form-item>

          <el-form-item label="附件">
            <Attachment
              v-if="currentLeave && currentLeave.id"
              :leave-id="currentLeave.id"
              :show-uploaded-files="true"
              :allow-delete="false"
            />
          </el-form-item>

          <el-form-item label="审批意见" v-if="comments.length > 0">
            <el-table :data="comments" style="width: 100%">
              <el-table-column
                prop="approverName"
                label="审批人"
                width="100"
              ></el-table-column>
              <el-table-column
                prop="approvalNode"
                label="审批节点"
                width="120"
              ></el-table-column>
              <el-table-column
                prop="approvalResult"
                label="审批结果"
                width="100"
              >
                <template slot-scope="scope">
                  <el-tag
                    :type="
                      scope.row.approvalResult == 'APPROVED'
                        ? 'success'
                        : 'danger'
                    "
                  >
                    {{
                      scope.row.approvalResult == "APPROVED" ? "同意" : "拒绝"
                    }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="comment" label="意见"></el-table-column>
              <el-table-column
                prop="approvedTime"
                label="审批时间"
                width="180"
              ></el-table-column>
            </el-table>
          </el-form-item>
        </el-form>

        <span slot="footer" class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关 闭</el-button>
          <el-button
            v-if="canWithdraw()"
            type="warning"
            @click="confirmWithdraw"
            size="mini"
          >
            撤 回
          </el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
// JS 逻辑部分保持完全不变，只需要关注上面的 template 和下面的 style
import { leaveApi } from "@/api/leave";
import Attachment from "@/components/Attachment.vue";
import { getUserToken, removeUserToken } from "@/utils/auth";

export default {
  name: "LeaveHistory",
  components: {
    Attachment,
  },
  data() {
    return {
      // 通用头部需要的数据
      login_user: null,
      userRoles: [],

      activeTab: "myLeaves", // 当前激活的标签页
      historyList: [],
      loading: false,
      approvalList: [], // 我的审批记录
      loadingApprovals: false,
      detailDialogVisible: false,
      currentLeave: null,
      comments: [],
      currentUserId: null,
      currentUserName: "",
      withdrawing: false,
    };
  },
  mounted() {
    const loginUser = getUserToken();
    // 填充通用头需要的用户信息
    if (loginUser) {
      this.login_user = loginUser.user || loginUser;
      this.userRoles = loginUser.roles || [];
    }
    const uid = loginUser && loginUser.user && loginUser.user.id;
    const uname =
      loginUser &&
      loginUser.user &&
      (loginUser.user.nickname ||
        loginUser.user.name ||
        loginUser.user.username ||
        loginUser.user.realName);
    this.currentUserId = uid || 1; // 兜底 1，防止未登录态下无ID
    if (uname) this.currentUserName = uname;
    this.loadHistory();
  },
  methods: {
    // 顶部组件退出回调
    handleLogout() {
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

    async switchToApprovals() {
      this.activeTab = "myApprovals";
      if (this.approvalList.length === 0) {
        await this.loadMyApprovals();
      }
    },

    async loadMyApprovals() {
      this.loadingApprovals = true;
      try {
        const res = await leaveApi.getMyApprovals(this.currentUserId);
        console.log("审批记录API返回数据:", res);
        const records = Array.isArray(res?.data) ? res.data : [];
        console.log("解析后的审批记录:", records);
        this.approvalList = records.map((r) => ({
          ...r,
          id: r && r.id != null ? String(r.id) : r.id,
        }));
        console.log("最终的approvalList:", this.approvalList);
      } catch (err) {
        console.error("加载审批记录失败:", err);
        this.$message.error("加载审批记录失败");
      } finally {
        this.loadingApprovals = false;
      }
    },

    async loadHistory() {
      this.loading = true;
      try {
        const res = await leaveApi.getMyLeaves(this.currentUserId);
        const records = Array.isArray(res?.data) ? res.data : [];
        // 防止大数精度丢失，前端以字符串展示ID
        this.historyList = records.map((r) => ({
          ...r,
          id: r && r.id != null ? String(r.id) : r.id,
        }));
      } catch (err) {
        this.$message.error("加载历史记录失败");
      } finally {
        this.loading = false;
      }
    },

    async viewDetail(row) {
      this.currentLeave = row;
      await this.loadComments(row.id);
      this.detailDialogVisible = true;
    },

    async viewApprovalDetail(row) {
      this.currentLeave = row;
      await this.loadComments(row.id || row.leaveId);
      this.detailDialogVisible = true;
    },

    async loadComments(leaveId) {
      try {
        const res = await leaveApi.getComments(leaveId);
        this.comments = res.data || [];
      } catch (err) {
        console.error("加载审批意见失败:", err);
      }
    },

    // 是否可以撤回（无任何已同意审批且是本人申请）
    canWithdraw() {
      if (!this.currentLeave) return false;
      if (!this.currentUserId) return false;
      // 确认是申请人
      if (
        this.currentLeave.userId != null &&
        Number(this.currentLeave.userId) !== Number(this.currentUserId)
      )
        return false;
      // 仅当未存在已批准的审批意见时允许撤回
      if (!this.comments || this.comments.length === 0) return true;
      for (const c of this.comments) {
        if (
          c &&
          c.approvalResult &&
          c.approvalResult.toUpperCase() === "APPROVED"
        )
          return false;
      }
      // 未发现批准，允许撤回
      return true;
    },

    async confirmWithdraw() {
      if (!this.currentLeave || !this.currentLeave.id) return;
      try {
        await this.$confirm(
          "确认要撤回此请假申请吗？撤回后流程将取消。",
          "确认撤回",
          {
            confirmButtonText: "确定",
            cancelButtonText: "取消",
            type: "warning",
          }
        );
      } catch (e) {
        return; // 取消
      }
      await this.withdrawCurrentLeave();
    },

    async withdrawCurrentLeave() {
      if (!this.currentLeave || !this.currentLeave.id) return;
      this.withdrawing = true;
      try {
        const res = await leaveApi.withdrawLeave(
          this.currentLeave.id,
          this.currentUserId
        );
        if (res && res.data) {
          this.$message.success("撤回成功");
          this.detailDialogVisible = false;
          this.loadHistory();
        } else {
          this.$message.error((res && res.msg) || "撤回失败");
        }
      } catch (err) {
        console.error("撤回失败", err);
        const msg =
          err && err.response && err.response.data && err.response.data.msg
            ? err.response.data.msg
            : "撤回失败";
        this.$message.error(msg);
      } finally {
        this.withdrawing = false;
      }
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

    getStatusName(status) {
      const statuses = {
        DRAFT: "草稿",
        SUBMITTED: "待审批",
        PENDING: "待审批", // 兼容历史数据/数据库默认值
        APPROVED: "已批准",
        REJECTED: "已拒绝",
      };
      return statuses[status] || status;
    },

    getStatusType(status) {
      const types = {
        DRAFT: "",
        SUBMITTED: "warning",
        PENDING: "warning", // 兼容历史数据
        APPROVED: "success",
        REJECTED: "danger",
      };
      return types[status] || "info";
    },
  },
};
</script>
