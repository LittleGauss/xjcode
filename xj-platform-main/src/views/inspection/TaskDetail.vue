<template>
  <div class="task-detail-container">
    <el-row>
      <el-col :span="12">
        <el-button type="primary" @click="goBack">返回</el-button>
      </el-col>
    </el-row>
    <el-card title="任务详情">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="任务标题">{{
          taskDetail.taskTitle
        }}</el-descriptions-item>
        <el-descriptions-item label="任务ID">{{
          taskDetail.id
        }}</el-descriptions-item>
        <el-descriptions-item label="任务描述">{{
          taskDetail.taskDesc
        }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{
          taskDetail.initiatorName
        }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">
          {{
            taskDetail.deadline
              ? new Date(taskDetail.deadline).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="任务状态">
          <el-tag v-if="taskDetail.status == 'IN_PROGRESS'" type="warning"
            >进行中</el-tag
          >
          <el-tag v-if="taskDetail.status == 'COMPLETED'" type="success"
            >已完成</el-tag
          >
          <el-tag v-if="taskDetail.status == 'CANCELLED'" type="danger"
            >已终止</el-tag
          >
          <el-tag v-if="taskDetail.status == 'OVERDUE'" type="danger"
            >逾期终止</el-tag
          >
        </el-descriptions-item>
        <el-descriptions-item label="是否逾期">
          <el-tag v-if="taskDetail.overdueFlag == 'YES'" type="danger"
            >已逾期</el-tag
          >
          <el-tag v-else type="success">未逾期</el-tag>
          截止时间：{{
            taskDetail.deadline
              ? new Date(taskDetail.deadline).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="完成率">
          {{
            taskDetail.completionRate ? taskDetail.completionRate + "%" : "0%"
          }}
        </el-descriptions-item>
        <el-descriptions-item label="剩余时间">
          <el-tag
            v-if="taskDetail.status == 'IN_PROGRESS'"
            :type="remainingTimeType"
          >
            {{ remainingTimeText }}
          </el-tag>
          <el-tag v-else> 任务未在进行中(已完成|已终止|已逾期) </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{
            taskDetail.createTime
              ? new Date(taskDetail.createTime).toLocaleString()
              : "-"
          }}
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">检查员分配列表</el-divider>
      <el-table :data="assignments" border stripe>
        <el-table-column label="检查员姓名" width="120px"
          ><template slot-scope="scope">
            <!-- 必须通过template和slot-scope获取行数据 -->
            {{ getInspectorNickname(scope.row.inspectorId) }}
          </template></el-table-column
        >
        <el-table-column prop="taskStatus" label="任务状态 " width="80px">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.taskStatus == 'PENDING'" type="danger"
              >待接收</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'RECEIVED'" type="warning"
              >已接收</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'SUBMIT'" type="success"
              >已提交</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'COMPLETED'" type="success"
              >已完成</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'REJECTED'" type="danger"
              >已驳回</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'REDO'" type="danger"
              >需重做</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'OVERDUE'" type="danger"
              >已逾期</el-tag
            >
            <el-tag v-if="scope.row.taskStatus == 'CANCELLED'" type="danger"
              >已终止</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column prop="filledFormUrl" label="已提交文件" width="120">
          <template slot-scope="scope">
            <el-button
              type="danger"
              size="mini"
              @click="downloadTemplate(scope.row)"
              class="file-name-btn"
            >
              {{
                getFileNameFromPath(scope.row.filledFormUrl)
                  ? getFileNameFromPath(scope.row.filledFormUrl)
                  : "尚未上传"
              }}
            </el-button>
          </template>
        </el-table-column>
        <el-table-column
          prop="redoCount"
          label="重做次数"
          width="60px"
        ></el-table-column>
        <el-table-column
          prop="rejectReason"
          label="驳回原因"
          min-width="100"
        ></el-table-column>

        <el-table-column prop="updateTime" label="更新时间" width="160px">
          <template slot-scope="scope">
            {{
              scope.row.updateTime
                ? new Date(scope.row.updateTime).toLocaleString()
                : "-"
            }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <!-- 当任务状态为已提交或已重提时，显示审核按钮 -->
            <template v-if="scope.row.taskStatus == 'SUBMIT'">
              <el-button type="success" size="mini" @click="passTask(scope.row)"
                >通过</el-button
              >
              <el-button
                type="danger"
                size="mini"
                @click="showRejectForm(scope.row)"
              >
                驳回
              </el-button>
            </template>
            <!-- 其他状态时，显示提示文本 -->
            <template v-else>
              <span style="color: #999; font-size: 12px"
                >尚未提交|已完成|已终止，无法审核</span
              >
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog
      title="驳回任务"
      :visible.sync="auditDialogVisible"
      :close-on-click-modal="true"
      width="800px"
    >
      <!-- 驳回原因表单 -->
      <el-form label-width="100px" style="margin-top: 20px">
        <el-form-item label="驳回原因">
          <el-input
            type="textarea"
            v-model="rejectReason"
            placeholder="请输入驳回原因"
            rows="3"
          ></el-input>
        </el-form-item>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
        <el-button
          @click="
            currentRejectAssignment = null;
            auditDialogVisible = false;
          "
          >取消</el-button
        >
      </el-form>
    </el-dialog>
  </div>
</template>

<script>
import { inspectionApi, templateApi } from "@/api/inspection";
import { userApi } from "@/api/usermag"; // 必须引入，否则会报未定义错误
export default {
  name: "TaskDetail",
  data() {
    return {
      taskDetail: {},
      assignments: [],
      initiatorId: this.login_user.id, // 当前登录负责人ID
      // 定时器用于实时更新剩余时间
      remainingTimer: null,
      auditDialogVisible: false,
      currentRejectAssignment: null,
      rejectReason: "",
      allInspectors: [], // 存储所有检查员（含nickname）
    };
  },
  props: ["login_user", "userRoles", "userPermissions"],

  created() {
    this.loadTaskDetail();
    this.loadAssignments();
    this.loadAllInspectors(); // 加载检查员数据
  },
  computed: {
    // 剩余时间文本
    remainingTimeText() {
      if (!this.taskDetail.deadline) return "-";

      const deadline = new Date(this.taskDetail.deadline);
      const now = new Date();
      const diffMs = deadline - now;

      if (diffMs <= 0) {
        return "已过期";
      }

      return this.formatRemainingTime(diffMs);
    },

    // 根据剩余时间设置标签类型
    remainingTimeType() {
      if (!this.taskDetail.deadline) return "info";

      const deadline = new Date(this.taskDetail.deadline);
      const now = new Date();
      const diffMs = deadline - now;
      const diffHours = diffMs / (1000 * 60 * 60);

      if (diffMs <= 0) {
        return "danger"; // 已过期
      } else if (diffHours < 24) {
        return "warning"; // 24小时内
      } else {
        return "primary"; // 超过24小时
      }
    },
  },
  methods: {
    // 新增：加载所有检查员（通过角色筛选）
    async loadAllInspectors() {
      try {
        const res = await userApi.getUsersByRole([
          "SUPERVISION",
          "SUPERVISION_MANAGER",
        ]);
        this.allInspectors = res.data; // 数据格式：[{id: 'xxx', nickname: 'xxx', ...}]
      } catch (err) {
        this.$message.error("加载检查员信息失败");
      }
    },
    // 新增：根据inspectorId获取对应的nickname
    getInspectorNickname(inspectorId) {
      // 从allInspectors中匹配ID对应的nickname
      const inspector = this.allInspectors.find(
        (item) => item.id === inspectorId
      );
      // 兜底：若未找到，显示原inspectorName或“未知”
      return (
        inspector?.nickname ||
        this.assignments.find((item) => item.inspectorId === inspectorId)
          ?.inspectorName ||
        "未知人员"
      );
    },
    // 加载任务详情
    async loadTaskDetail() {
      const taskId = this.$route.params.taskId;
      try {
        const [detailRes, rateRes, statusRes] = await Promise.all([
          inspectionApi.getTaskDetail(taskId),
          inspectionApi.calculateCompletionRate(taskId),
          inspectionApi.getProcessStatus(taskId),
        ]);

        // 一次性构建完整对象
        this.taskDetail = {
          ...detailRes.data,
          completionRate: rateRes.data,
          processStatus: statusRes.data,
        };
      } catch (err) {
        this.$message.error("加载任务详情失败：" + err.message);
      }
    },

    // 审核通过
    async passTask(row) {
      try {
        await inspectionApi.auditTask(row.id, this.initiatorId, "PASS", "同意");
        this.$message.success("审核通过！");
        this.loadTaskDetail();
        this.loadAssignments();
      } catch (err) {
        this.$message.error("审核失败：" + err.message);
      }
    },

    // 显示驳回表单
    showRejectForm(row) {
      this.currentRejectAssignment = row;
      this.auditDialogVisible = true;
    },

    // 确认驳回
    async confirmReject() {
      if (!this.rejectReason) {
        this.$message.warning("请输入驳回原因");
        return;
      }
      try {
        await inspectionApi.auditTask(
          this.currentRejectAssignment.id,
          this.initiatorId,
          "REJECT",
          this.rejectReason
        );
        this.$message.success("驳回成功！");
        this.auditDialogVisible = false;
        this.currentRejectAssignment = null;
        this.rejectReason = "";
        this.loadTaskDetail();
        this.loadAssignments();
      } catch (err) {
        this.auditDialogVisible = false;
        this.$message.error("驳回失败：" + err.message);
      }
    },

    // 加载分配列表
    async loadAssignments() {
      const taskId = this.$route.params.taskId;
      try {
        const res = await inspectionApi.getAssignmentsByTaskId(taskId);
        this.assignments = res.data;
      } catch (err) {
        this.$message.error("加载分配列表失败：" + err.message);
      }
    },
    // 格式化剩余时间
    formatRemainingTime(milliseconds) {
      const seconds = Math.floor(milliseconds / 1000);
      const minutes = Math.floor(seconds / 60);
      const hours = Math.floor(minutes / 60);
      const days = Math.floor(hours / 24);

      if (days > 0) {
        return `${days}天${hours % 24}小时`;
      } else if (hours > 0) {
        return `${hours}小时${minutes % 60}分钟`;
      } else if (minutes > 0) {
        return `${minutes}分钟${seconds % 60}秒`;
      } else {
        return `${seconds}秒`;
      }
    },

    // 启动剩余时间更新定时器
    startRemainingTimeTimer() {
      if (this.remainingTimer) {
        clearInterval(this.remainingTimer);
      }

      this.remainingTimer = setInterval(() => {
        // 触发计算属性重新计算
        this.$forceUpdate();
      }, 1000); // 每秒更新一次
    },
    // 下载模板
    async downloadTemplate(row) {
      try {
        if (row == null || row.filledFormUrl == null) {
          return;
        }
        const res = await templateApi.downloadTemplateWithPath(
          row.filledFormUrl
        );
        if (res.code == 200) {
          // 1. 获取预签名链接
          const downloadUrl = res.data;
          // 1. 通过 fetch 获取文件流（避免直接访问 MinIO 链接的签名问题）
          const response = await fetch(downloadUrl);
          console.log("response", response);
          if (!response.ok) {
            throw new Error("文件下载链接无效");
          }
          // 2. 转换为 Blob 流
          const blob = await response.blob();
          // 3. 手动创建下载链接，强制指定原始文件名
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.href = url;
          a.download = this.getFileNameFromPath(row.filledFormUrl); // 文件名字
          a.click();
          // 4. 释放临时资源
          window.URL.revokeObjectURL(url);
        } else {
          this.$message.error(res.msg || "生成下载链接失败");
        }
      } catch (err) {
        this.$message.error("模板下载失败：" + err.message);
      }
    },
    getFileNameFromPath(filePath) {
      // 这里直接调用工具函数
      return inspectionApi.extractFileName(filePath);
    },
    goBack() {
      this.$router.push("/inspection/task-monitor");
    },
  },
  mounted() {
    this.startRemainingTimeTimer();
  },
  beforeDestroy() {
    if (this.remainingTimer) {
      clearInterval(this.remainingTimer);
    }
  },
};
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}
::v-deep .file-name-btn {
  white-space: normal !important; /* 取消默认的文字不换行（nowrap） */
  word-wrap: break-word; /* 长单词/长字符串自动换行 */
  word-break: break-all; /* 强制换行（适配中文） */
  width: 100%; /* 按钮宽度撑满列（也可设固定值如100px） */
  line-height: 1.2; /* 调整行高，优化换行显示效果 */
  padding: 4px 2px; /* 微调内边距，避免文字溢出 */
}
</style>
