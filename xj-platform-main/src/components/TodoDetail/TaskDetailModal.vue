<template>
  <!-- 详情弹框组件 -->
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogVisible"
    :close-on-click-modal="true"
    width="800px"
    :before-close="handleBeforeClose"
    center
    @closed="handleDialogClosed"
  >
    <!-- 弹框内容区域 -->
    <div class="task-detail-content" v-if="currentTask">
      <!-- 流程信息卡片 -->
      <el-card class="process-info-card">
        <div slot="header" class="clearfix">
          <span class="card-title">流程信息</span>
        </div>
        <el-descriptions :column="2" border size="medium">
          <el-descriptions-item label="任务名称">{{
            currentTask.taskName
          }}</el-descriptions-item>
          <el-descriptions-item label="任务ID">{{
            currentTask.taskId
          }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{
            formatBusinessType(currentTask.businessType)
          }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{
            formatDateTime(currentTask.createTime)
          }}</el-descriptions-item>
          <el-descriptions-item label="办理人">{{
            formatAssignee()
          }}</el-descriptions-item>
          <el-descriptions-item label="业务ID">{{
            currentTask.businessKey
          }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 业务详情卡片 -->
      <el-card class="business-detail-card">
        <div slot="header" class="clearfix">
          <span class="card-title">{{ businessConfig.title }}</span>
        </div>

        <!-- 根据业务类型显示不同的详情 -->
        <div v-if="currentTask.businessType === 'leaveAndCancelProcess'">
          <leave-detail :data="currentTask.businessData" />
        </div>
        <div v-else-if="currentTask.businessType === 'contractReviewProcess'">
          <contract-detail :data="currentTask.businessData" />
        </div>
        <div
          v-else-if="
            currentTask.businessType === 'consumableApplicationProcess'
          "
        >
          <consumable-detail :data="currentTask.businessData" />
        </div>
        <div v-else>
          <!-- 默认展示所有字段 -->
          <el-descriptions :column="1" border size="medium">
            <el-descriptions-item
              v-for="(value, key) in currentTask.businessData"
              :key="key"
              :label="key"
            >
              {{ formatValue(value, key) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 流程变量卡片 -->
      <el-card class="variables-card" v-if="currentTask.variables">
        <div slot="header" class="clearfix">
          <span class="card-title">流程变量</span>
        </div>
        <el-descriptions :column="2" border size="medium">
          <el-descriptions-item
            v-for="(value, key) in currentTask.variables"
            :key="'var-' + key"
            :label="key"
          >
            {{ formatVariableValue(value, key) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    <div v-else class="loading-placeholder">
      <p>加载中...</p>
    </div>

    <!-- 弹框底部按钮 -->
    <span slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">关 闭</el-button>
      <el-button type="primary" @click="handleApprove" v-if="showActionButtons">
        审 批
      </el-button>
    </span>
  </el-dialog>
</template>

<script>
/**
 * 引入业务详情组件
 */
import LeaveDetail from "@/components/TodoDetail/LeaveDetail.vue";
import ContractDetail from "@/components/TodoDetail/ContractDetail.vue";
import ConsumableDetail from "@/components/TodoDetail/ConsumableDetail.vue";

export default {
  name: "TaskDetailModal",

  components: {
    LeaveDetail,
    ContractDetail,
    ConsumableDetail,
  },

  props: {
    // 是否显示审批按钮（可选，默认不显示）
    showActionButtons: {
      type: Boolean,
      default: false,
    },
    // 可选的用户列表，用于把办理人ID解析为姓名
    users: {
      type: Array,
      default: () => [],
    },
  },

  data() {
    return {
      dialogVisible: false, // 控制弹框显示/隐藏
      currentTask: null, // 当前任务数据
      // 业务类型配置映射表
      businessConfigMap: {
        leaveAndCancelProcess: {
          title: "请假申请详情",
          component: "leave-detail",
        },
        contractReviewProcess: {
          title: "合同评审详情",
          component: "contract-detail",
        },
        consumableApplicationProcess: {
          title: "耗材申请详情",
          component: "consumable-detail",
        },
      },
      // 业务类型中文名称映射
      businessTypeMap: {
        leaveAndCancelProcess: "请假流程",
        contractReviewProcess: "合同评审",
        consumableApplicationProcess: "耗材申请",
      },
    };
  },

  computed: {
    /**
     * 弹框标题
     */
    dialogTitle() {
      if (!this.currentTask) return "任务详情";
      return `${this.formatBusinessType(this.currentTask.businessType)} - ${
        this.currentTask.taskName
      }`;
    },

    /**
     * 当前业务的配置
     */
    businessConfig() {
      if (!this.currentTask || !this.currentTask.businessType) {
        return { title: "业务详情" };
      }
      return (
        this.businessConfigMap[this.currentTask.businessType] || {
          title: "业务详情",
        }
      );
    },
  },

  methods: {
    /**
     * 打开弹框
     * @param {Object} taskData - 任务数据
     * 使用方法：this.$refs.taskDetailModal.show(taskData)
     */
    show(taskData) {
      console.log("打开弹框，任务数据:", taskData);
      this.currentTask = taskData;
      // 确保 businessData 存在
      if (!this.currentTask.businessData) {
        this.currentTask.businessData = {};
      }
      // 确保 variables 存在
      if (!this.currentTask.variables) {
        this.currentTask.variables = {};
      }
      this.dialogVisible = true;
    },

    /**
     * 弹框关闭前的回调（用于处理右上角关闭按钮和遮罩层关闭）
     */
    handleBeforeClose(done) {
      console.log("弹框即将关闭");
      this.$emit("close");
      done(); // 必须调用 done() 才能关闭弹框
    },

    /**
     * 弹框完全关闭后的回调
     */
    handleDialogClosed() {
      console.log("弹框已完全关闭");
      this.currentTask = null;
    },

    /**
     * 关闭按钮点击事件
     */
    handleCancel() {
      console.log("点击关闭按钮");
      this.dialogVisible = false;
      this.$emit("close");
    },

    /**
     * 处理审批操作
     */
    handleApprove() {
      // 触发审批事件，父组件可以监听并处理
      this.$emit("approve", this.currentTask);
    },

    /**
     * 格式化业务类型
     * @param {String} type - 业务类型英文
     * @returns {String} 业务类型中文
     */
    formatBusinessType(type) {
      return this.businessTypeMap[type] || type;
    },

    /**
     * 格式化日期时间
     * @param {String} dateStr - 日期字符串
     * @returns {String} 格式化后的日期
     */
    formatDateTime(dateStr) {
      if (!dateStr) return "-";
      try {
        const date = new Date(dateStr);
        return date.toLocaleString("zh-CN", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
          hour: "2-digit",
          minute: "2-digit",
          second: "2-digit",
        });
      } catch (e) {
        console.error("日期格式错误:", dateStr);
        return dateStr;
      }
    },

    /**
     * 格式化业务数据值
     * @param {*} value - 原始值
     * @param {String} key - 字段名
     * @returns {String} 格式化后的值
     */
    formatValue(value, key) {
      if (value === null || value === undefined) return "-";

      // 如果是日期字段，进行格式化
      if (
        key.includes("Date") ||
        key.includes("Time") ||
        key.includes("create") ||
        key.includes("update")
      ) {
        return this.formatDateTime(value);
      }

      // 如果是布尔值，转换为中文
      if (typeof value === "boolean") {
        return value ? "是" : "否";
      }

      // 如果是对象或数组，转换为JSON字符串
      if (typeof value === "object") {
        try {
          return JSON.stringify(value);
        } catch {
          return String(value);
        }
      }

      return String(value);
    },

    /**
     * 格式化流程变量值
     * @param {*} value - 变量值
     * @param {String} key - 变量名（用于判断是否为用户相关字段）
     * @returns {String} 格式化后的值（优先将用户 id/username 映射为 username）
     */
    formatVariableValue(value, key) {
      if (value === null || value === undefined) return "-";

      // 布尔值显示中文
      if (typeof value === "boolean") return value ? "是" : "否";

      // 对象类型直接 JSON 化展示（如果是用户对象，会优先尝试取 username）
      if (typeof value === "object") {
        if (value && (value.username || value.nickname || value.name)) {
          return (
            value.username ||
            value.nickname ||
            value.name ||
            JSON.stringify(value)
          );
        }
        try {
          return JSON.stringify(value);
        } catch {
          return String(value);
        }
      }

      const str = String(value);

      // 决策：当 key 看起来像用户相关字段，或值明显是用户 id 时，尝试解析为 username
      const userKeyRegex =
        /starter|requester|applicant|applicantName|applicantId|deptManager|adminUser|mainLeader|viceLeader|assignee|approver|user|manager|leader|owner/i;
      const looksLikeUserKey = key && userKeyRegex.test(key);
      const looksLikeId = /^\d+$/.test(str);

      if (
        Array.isArray(this.users) &&
        this.users.length > 0 &&
        (looksLikeUserKey || looksLikeId)
      ) {
        const users = this.users;
        const found = users.find((u) => {
          try {
            return (
              String(u.id) === str ||
              String(u.userId) === str ||
              String(u.username) === str ||
              String(u.nickname) === str ||
              String(u.name) === str
            );
          } catch (e) {
            return false;
          }
        });
        if (found) return found.username || found.nickname || found.name || str;
      }

      return str;
    },

    // 将 currentTask.assignee（可能是 id）解析为可显示的姓名
    formatAssignee() {
      if (!this.currentTask) return "-";
      const assignee = this.currentTask.assignee;
      if (assignee === null || assignee === undefined || assignee === "")
        return "-";

      // 如果已经是对象，尝试取常见姓名字段
      if (typeof assignee === "object") {
        return (
          assignee.name ||
          assignee.realName ||
          assignee.username ||
          JSON.stringify(assignee)
        );
      }

      const s = String(assignee);
      const users = Array.isArray(this.users) ? this.users : [];
      const found = users.find(
        (u) =>
          String(u.id) === s ||
          String(u.userId) === s ||
          String(u.username) === s ||
          String(u.nickname) === s
      );
      if (found) return found.username || found.nickname || found.name || s;

      // 看起来不是 uid 的话直接返回原始字符串
      return s;
    },
  },
};
</script>

<style scoped>
/* 组件样式 */
.task-detail-content {
  max-height: 60vh;
  overflow-y: auto;
  padding: 0 10px;
}

/* 卡片样式 */
.process-info-card,
.business-detail-card,
.variables-card {
  margin-bottom: 20px;
}

/* 卡片标题样式 */
.card-title {
  font-size: 16px;
  font-weight: bold;
}

/* 清除浮动 */
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

/* 弹框底部按钮样式 */
.dialog-footer {
  text-align: center;
}
</style>
